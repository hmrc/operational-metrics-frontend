/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package connector

import base.BaseSpec
import com.github.tomakehurst.wiremock.client.WireMock.*
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import play.api.cache.AsyncCacheApi
import play.api.Configuration
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.test.{HttpClientV2Support, WireMockSupport}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

import org.apache.pekko.Done

/** Minimal in-memory AsyncCacheApi for tests. Caches forever (TTL ignored). */
class InMemoryCacheApi(implicit ec: ExecutionContext) extends AsyncCacheApi {
  private val store = scala.collection.concurrent.TrieMap.empty[String, Any]

  def set(key: String, value: Any, expiration: Duration): Future[Done] =
    Future.successful { store.put(key, value); Done }

  def remove(key: String): Future[Done] =
    Future.successful { store.remove(key); Done }

  def getOrElseUpdate[A: ClassTag](key: String, expiration: Duration)(orElse: => Future[A]): Future[A] =
    store.get(key) match {
      case Some(v: A) => Future.successful(v)
      case _          => orElse.map { v => store.put(key, v); v }
    }

  def get[T: ClassTag](key: String): Future[Option[T]] =
    Future.successful(store.get(key).collect { case v: T => v })

  def removeAll(): Future[Done] = Future.successful { store.clear(); Done }
}

class TeamsAndRepositoriesConnectorSpec
    extends BaseSpec
    with ScalaFutures
    with IntegrationPatience
    with HttpClientV2Support
    with WireMockSupport {

  private implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  private val servicesConfig =
    new ServicesConfig(
      Configuration.from(
        Map(
          "microservice.services.teams-and-repositories.protocol" -> "http",
          "microservice.services.teams-and-repositories.host"     -> wireMockHost,
          "microservice.services.teams-and-repositories.port"     -> wireMockPort
        )
      )
    )

  private def freshConnector(extraConfig: Map[String, Any] = Map.empty): TeamsAndRepositoriesConnector =
    new TeamsAndRepositoriesConnector(
      httpClient     = httpClientV2,
      servicesConfig = servicesConfig,
      cache          = new InMemoryCacheApi,
      configuration  = Configuration.from(extraConfig)
    )

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  "getRepositoryOwnership" should {

    "call GET /api/v2/repositories and decode name and owningTeams" in {
      stubFor(
        get(urlEqualTo("/api/v2/repositories"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody(
                """
                  |[
                  |  {
                  |    "name": "service-one",
                  |    "owningTeams": ["PlatOps"]
                  |  }
                  |]
                  |""".stripMargin
              )
          )
      )

      val result = freshConnector().getRepositoryOwnership().futureValue

      result mustBe Map("service-one" -> Seq("PlatOps"))
      verify(getRequestedFor(urlEqualTo("/api/v2/repositories")))
    }

    "ignore additional fields such as teamNames" in {
      stubFor(
        get(urlEqualTo("/api/v2/repositories"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody(
                """
                  |[
                  |  {
                  |    "name": "service-one",
                  |    "owningTeams": ["PlatOps"],
                  |    "teamNames": ["PlatOps", "Other Team"]
                  |  }
                  |]
                  |""".stripMargin
              )
          )
      )

      val result = freshConnector().getRepositoryOwnership().futureValue

      result mustBe Map("service-one" -> Seq("PlatOps"))
    }

    "deduplicate owning teams" in {
      stubFor(
        get(urlEqualTo("/api/v2/repositories"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody(
                """
                  |[
                  |  {
                  |    "name": "service-one",
                  |    "owningTeams": ["PlatOps", "PlatOps", "MDTP"]
                  |  }
                  |]
                  |""".stripMargin
              )
          )
      )

      val result = freshConnector().getRepositoryOwnership().futureValue

      result mustBe Map("service-one" -> Seq("MDTP", "PlatOps"))
    }

    "return owning teams in deterministic sorted order" in {
      stubFor(
        get(urlEqualTo("/api/v2/repositories"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody(
                """
                  |[
                  |  {
                  |    "name": "service-one",
                  |    "owningTeams": ["PlatOps", "MDTP", "Platform Engineering"]
                  |  }
                  |]
                  |""".stripMargin
              )
          )
      )

      val result = freshConnector().getRepositoryOwnership().futureValue

      result mustBe Map("service-one" -> Seq("MDTP", "Platform Engineering", "PlatOps"))
    }

    "trim surrounding whitespace from team names before deduplication" in {
      stubFor(
        get(urlEqualTo("/api/v2/repositories"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody(
                """
                  |[
                  |  {
                  |    "name": "service-one",
                  |    "owningTeams": ["PlatOps", " PlatOps "]
                  |  }
                  |]
                  |""".stripMargin
              )
          )
      )

      val result = freshConnector().getRepositoryOwnership().futureValue

      result mustBe Map("service-one" -> Seq("PlatOps"))
    }

    "propagate non-success HTTP responses as a failed Future" in {
      stubFor(
        get(urlEqualTo("/api/v2/repositories"))
          .willReturn(aResponse().withStatus(500))
      )

      val result = freshConnector().getRepositoryOwnership()

      result.failed.futureValue mustBe a[uk.gov.hmrc.http.UpstreamErrorResponse]
    }

    "propagate invalid JSON as a failed Future" in {
      stubFor(
        get(urlEqualTo("/api/v2/repositories"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody("not-valid-json")
          )
      )

      val result = freshConnector().getRepositoryOwnership()

      result.failed.futureValue mustBe an[Exception]
    }

    "cache repository ownership between calls within TTL" in {
      val cachedConnector = freshConnector()

      stubFor(
        get(urlEqualTo("/api/v2/repositories"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody(
                """
                  |[
                  |  { "name": "service-one", "owningTeams": ["PlatOps"] }
                  |]
                  |""".stripMargin
              )
          )
      )

      val first  = cachedConnector.getRepositoryOwnership().futureValue
      val second = cachedConnector.getRepositoryOwnership().futureValue

      first  mustBe Map("service-one" -> Seq("PlatOps"))
      second mustBe first

      verify(1, getRequestedFor(urlEqualTo("/api/v2/repositories")))
    }
  }
}
