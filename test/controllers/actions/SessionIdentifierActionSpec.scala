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

import base.SpecBase
import play.api.mvc.{Action, AnyContent, BodyParsers, Results}
import uk.gov.hmrc.http.SessionKeys
import play.api.test.FakeRequest
import play.api.test.Helpers._
import scala.concurrent.ExecutionContext.Implicits.global

class SessionIdentifierActionSpec extends SpecBase {

  private class Harness(identifierAction: IdentifierAction) {
    def onPageLoad(): Action[AnyContent] = identifierAction { _ =>
      Results.Ok
    }
  }

  "SessionIdentifierAction" - {

    "must redirect to unauthorised when there is no active session" in {
      val application = applicationBuilder().build()

      running(application) {
        val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
        val action = new SessionIdentifierAction(bodyParsers)
        val controller = new Harness(action)

        val result = controller.onPageLoad()(FakeRequest(GET, "/"))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe controllers.routes.UnauthorisedController.onPageLoad().url
      }
    }

    "must perform the action when there is an active session" in {
      val application = applicationBuilder().build()

      running(application) {
        val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
        val action = new SessionIdentifierAction(bodyParsers)
        val controller = new Harness(action)

        val request = FakeRequest(GET, "/").withSession(SessionKeys.sessionId -> "session-id")
        val result = controller.onPageLoad()(request)

        status(result) mustBe OK
      }
    }
  }
}
