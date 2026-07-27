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

package controllers.actions

import controllers.auth.AuthController
import play.api.mvc.*
import uk.gov.hmrc.internalauth.client.{
  FrontendAuthComponents,
  IAAction,
  Predicate,
  Resource,
  ResourceLocation,
  ResourceType
}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait InternalAuthAction
    extends ActionBuilder[Request, AnyContent]
    with ActionFunction[Request, Request]

class DefaultInternalAuthAction @Inject() (
    auth: FrontendAuthComponents,
    val parser: BodyParsers.Default
)(implicit val executionContext: ExecutionContext)
    extends InternalAuthAction {

  import DefaultInternalAuthAction.*

  override def invokeBlock[A](
      request: Request[A],
      block: Request[A] => Future[Result]
  ): Future[Result] =
    auth
      .authorizedAction(
        continueUrl = AuthController.continueUrl(Call("GET", request.uri)),
        predicate = readPermission
      )
      .invokeBlock[A](
        request,
        authenticatedRequest => block(authenticatedRequest.request)
      )
}

object DefaultInternalAuthAction {

  val readPermission: Predicate =
    Predicate.Permission(
      Resource(
        ResourceType("operational-metrics-frontend"),
        ResourceLocation("*")
      ),
      IAAction("READ")
    )
}
