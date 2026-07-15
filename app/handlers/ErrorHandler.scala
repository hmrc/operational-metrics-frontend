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

package handlers

import javax.inject.{Inject, Singleton}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.RequestHeader
import play.twirl.api.Html
import uk.gov.hmrc.cataloguewrapper.services.CatalogueWrapperService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.http.FrontendErrorHandler
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import views.html.ErrorTemplate

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ErrorHandler @Inject()(
    val messagesApi: MessagesApi,
    catalogueWrapperService: CatalogueWrapperService,
    view: ErrorTemplate
)(override implicit val ec: ExecutionContext)
    extends FrontendErrorHandler
    with I18nSupport {

  override def standardErrorTemplate(
      pageTitle: String,
      heading: String,
      message: String
  )(implicit rh: RequestHeader): Future[Html] = {
    implicit val messages: Messages =
      messagesApi.preferred(rh)

    implicit val hc: HeaderCarrier =
      HeaderCarrierConverter.fromRequestAndSession(rh, rh.session)

    val content =
      view(pageTitle, heading, message)

    catalogueWrapperService
      .standardCatalogueLayout(
        content      = content,
        pageTitle    = Some(messages(pageTitle)),
        activeItemId = None,
        fullWidth    = false,
        signOutUrl   = None
      )
  }
}
