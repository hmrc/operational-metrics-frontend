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

import base.{ApplicationTestSupport, BaseSpec}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl

class AuthControllerSpec extends BaseSpec with ApplicationTestSupport {

  "AuthController.signOut" should {

    "clear session state and redirect to the signed out page" in {
      val application =
        applicationBuilder().build()

      running(application) {
        val request =
          FakeRequest(GET, routes.AuthController.signOut().url)
            .withSession("some-session-key" -> "some-session-value")

        val result =
          route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.SignedOutController.onPageLoad().url
        session(result).data mustBe empty
      }
    }
  }

  "AuthController.continueUrl" should {

    "route through post sign in for a relative target URL" in {
      val result =
        AuthController.continueUrl(controllers.routes.IndexController.onPageLoad())

      result.url must include(routes.AuthController.postSignIn(None).url)
      result.url must include("targetUrl=")
    }

    "not preserve the root URL as a target" in {
      val result =
        AuthController.continueUrl(play.api.mvc.Call("GET", "/"))

      result mustBe routes.AuthController.postSignIn(None)
    }
  }

  "AuthController.sanitize" should {

    "remove sign-in URLs to avoid redirect loops" in {
      val result =
        AuthController.sanitize(Some(RedirectUrl(routes.AuthController.signIn(None).url)))

      result mustBe None
    }

    "remove post-sign-in URLs to avoid redirect loops" in {
      val result =
        AuthController.sanitize(Some(RedirectUrl(routes.AuthController.postSignIn(None).url)))

      result mustBe None
    }

    "preserve a normal relative target URL" in {
      val result =
        AuthController.sanitize(
          Some(RedirectUrl(controllers.routes.IndexController.onPageLoad().url))
        )

      result.map(_.unsafeValue) mustBe Some(controllers.routes.IndexController.onPageLoad().url)
    }
  }
}
