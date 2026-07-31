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
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TeamsAndRepositoriesConnector @Inject() (
    httpClient: HttpClientV2,
    servicesConfig: ServicesConfig
)(implicit ec: ExecutionContext) {

  private val baseUrl: String =
    servicesConfig.baseUrl("teams-and-repositories")

  def getRepositoryOwnership()(implicit hc: HeaderCarrier): Future[Map[String, Seq[String]]] =
    httpClient
      .get(url"$baseUrl/api/v2/repositories")
      .execute[Seq[RepositoryOwnership]]
      .map(
        _.map(repository =>
          repository.name -> repository.owningTeams.filter(_.trim.nonEmpty).distinct.sortBy(_.toLowerCase)
        ).toMap
      )
}
