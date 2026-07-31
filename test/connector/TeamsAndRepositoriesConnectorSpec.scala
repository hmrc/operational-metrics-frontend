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
import play.api.Configuration
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.test.{HttpClientV2Support, WireMockSupport}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

class TeamsAndRepositoriesConnectorSpec
    extends BaseSpec
    with ScalaFutures
    with IntegrationPatience
    with HttpClientV2Support
    with WireMockSupport {

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

  private val connector =
    new TeamsAndRepositoriesConnector(
      httpClient = httpClientV2,
      servicesConfig = servicesConfig
    )(scala.concurrent.ExecutionContext.global)

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

      val result = connector.getRepositoryOwnership().futureValue

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

      val result = connector.getRepositoryOwnership().futureValue

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

      val result = connector.getRepositoryOwnership().futureValue

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

      val result = connector.getRepositoryOwnership().futureValue

      result mustBe Map("service-one" -> Seq("MDTP", "Platform Engineering", "PlatOps"))
    }

    "propagate non-success HTTP responses as a failed Future" in {
      stubFor(
        get(urlEqualTo("/api/v2/repositories"))
          .willReturn(aResponse().withStatus(500))
      )

      val result = connector.getRepositoryOwnership()

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

      val result = connector.getRepositoryOwnership()

      result.failed.futureValue mustBe an[Exception]
    }
  }
}
