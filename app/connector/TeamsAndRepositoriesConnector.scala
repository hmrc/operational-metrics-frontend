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

import models.RepositoryOwnership
import play.api.cache.AsyncCacheApi
import play.api.{Configuration, Logging}
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.*
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TeamsAndRepositoriesConnector @Inject() (
    httpClient: HttpClientV2,
    servicesConfig: ServicesConfig,
    cache: AsyncCacheApi,
    configuration: Configuration
)(implicit ec: ExecutionContext) extends Logging {

  private val baseUrl: String =
    servicesConfig.baseUrl("teams-and-repositories")

  private val ownershipCacheKey = "teams-and-repositories.repository-ownership"

  private val ownershipCacheTtl: FiniteDuration =
    configuration
      .getOptional[scala.concurrent.duration.Duration]("cache.repository-ownership.ttl")
      .collect { case d: FiniteDuration => d }
      .getOrElse(2.minutes)

  def getRepositoryOwnership()(implicit hc: HeaderCarrier): Future[Map[String, Seq[String]]] =
    cache.getOrElseUpdate(ownershipCacheKey, ownershipCacheTtl) {
      logger.info(s"Fetching repository ownership from teams-and-repositories (cache miss)")
      httpClient
        .get(url"$baseUrl/api/v2/repositories")
        .execute[Seq[RepositoryOwnership]]
        .map(
          _.map(repository =>
            repository.name -> repository.owningTeams.map(_.trim).filter(_.nonEmpty).distinct.sortBy(_.toLowerCase(java.util.Locale.ROOT))
          ).toMap
        )
    }
}
