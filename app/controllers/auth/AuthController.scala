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

package controllers.auth

import controllers.routes as appRoutes
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.internalauth.client.FrontendAuthComponents
import uk.gov.hmrc.play.bootstrap.binders.{OnlyRelative, RedirectUrl}
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class AuthController @Inject()(
    val controllerComponents: MessagesControllerComponents,
    auth: FrontendAuthComponents
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  import AuthController._

  def signIn(targetUrl: Option[RedirectUrl]): Action[AnyContent] =
    auth.authenticatedAction(
      continueUrl = routes.AuthController.postSignIn(sanitize(targetUrl))
    )() {
      Redirect(routes.AuthController.postSignIn(sanitize(targetUrl)))
    }

  def postSignIn(targetUrl: Option[RedirectUrl]): Action[AnyContent] =
    auth.authenticatedAction(
      continueUrl = routes.AuthController.signIn(sanitize(targetUrl)),
    )() { 
      Redirect(
        targetUrl
          .flatMap(_.getEither(OnlyRelative).toOption)
          .fold(appRoutes.IndexController.onPageLoad().url)(_.url)
      )
  }

  def signOut(): Action[AnyContent] =
    Action {
      Redirect(routes.SignedOutController.onPageLoad()).withNewSession
    }

}

object AuthController {

  private[auth] def sanitize(targetUrl: Option[RedirectUrl]): Option[RedirectUrl] = {
    val avoid =
      List(
        routes.AuthController.signIn(None),
        routes.AuthController.postSignIn(None)
      )

    targetUrl.filter { redirectUrl =>
      !avoid.exists(call => redirectUrl.unsafeValue.startsWith(call.url))
    }
  }

  def continueUrl(targetUrl: Call): Call =
    routes.AuthController.postSignIn(
      sanitize(
        Some(targetUrl.url)
          .filterNot(_ == "/")
          .map(RedirectUrl.apply)
      )
    )
}
