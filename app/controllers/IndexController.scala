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

package controllers

import connector.{OperationalMetricsConnector, TeamsAndRepositoriesConnector}
import controllers.actions.InternalAuthAction
import javax.inject.Inject
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.cataloguewrapper.services.CatalogueWrapperService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import viewmodels.ServiceLeadTimesViewModel
import views.html.IndexView

import scala.concurrent.{ExecutionContext, Future}

class IndexController @Inject() (
    val controllerComponents: MessagesControllerComponents,
    internalAuth: InternalAuthAction,
    operationalMetricsConnector: OperationalMetricsConnector,
    teamsAndRepositoriesConnector: TeamsAndRepositoriesConnector,
    catalogueWrapperService: CatalogueWrapperService,
    view: IndexView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val logger = Logger(getClass)

  def onPageLoad(): Action[AnyContent] =
    internalAuth.async { implicit request =>
      implicit val hc: HeaderCarrier =
        HeaderCarrierConverter.fromRequestAndSession(request, request.session)

      val selectedTeam: Option[String] =
        request.getQueryString("team").filter(_.nonEmpty)

      val serviceLeadTimesF =
        operationalMetricsConnector.getServiceLeadTimes()

      val repositoryOwnershipF =
        teamsAndRepositoriesConnector
          .getRepositoryOwnership()
          .recover { case ex =>
            logger.warn(s"Failed to fetch repository ownership from teams-and-repositories: ${ex.getMessage}")
            Map.empty[String, Seq[String]]
          }

      for {
        serviceLeadTimes    <- serviceLeadTimesF
        repositoryOwnership <- repositoryOwnershipF
        pageContent = view(ServiceLeadTimesViewModel.from(serviceLeadTimes, repositoryOwnership, selectedTeam))
        html <- catalogueWrapperService.standardCatalogueLayout(
          content = pageContent,
          pageTitle = Some("Operational Metrics"),
          activeItemId = Some("operational-metrics"),
          fullWidth = false,
          signOutUrl = Some(controllers.auth.routes.AuthController.signOut().url)
        )
      } yield {
        Ok(html)
      }
    }
}
