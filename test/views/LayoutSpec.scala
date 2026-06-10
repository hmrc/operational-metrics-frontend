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

package views

import base.SpecBase
import controllers.routes
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import views.html.templates.Layout

class LayoutSpec extends SpecBase {

  "Layout" - {

    "must render Catalogue navbar links using the configured Catalogue base URL" in {
      val application = applicationBuilder().build()

      running(application) {
        val layout = application.injector.instanceOf[Layout]
        val request = FakeRequest(GET, routes.IndexController.onPageLoad().url)

        val html = layout(pageTitle = "Operational Metrics")(Html("content"))(request, messages(application)).body

        html must include("href=\"http://localhost:9017/\"")
        html must include("href=\"http://localhost:9017/users\"")
        html must include("href=\"http://localhost:9017/teams\"")
        html must include("href=\"http://localhost:9017/repositories\"")
        html must include("href=\"http://localhost:9017/deploy-service\"")
        html must include("href=\"http://localhost:9017/service-metrics\"")
      }
    }

    "must keep Operational Metrics and Sign out links local to this frontend" in {
      val application = applicationBuilder().build()

      running(application) {
        val layout = application.injector.instanceOf[Layout]
        val request = FakeRequest(GET, routes.IndexController.onPageLoad().url)

        val html = layout(pageTitle = "Operational Metrics")(Html("content"))(request, messages(application)).body

        html must include(s"href=\"${routes.IndexController.onPageLoad().url}\"")
        html must include(s"href=\"${controllers.auth.routes.AuthController.signOutNoSurvey().url}\"")
      }
    }

    "must not contain a duplicated opening nav tag" in {
      val application = applicationBuilder().build()

      running(application) {
        val layout = application.injector.instanceOf[Layout]
        val request = FakeRequest(GET, routes.IndexController.onPageLoad().url)

        val html = layout(pageTitle = "Operational Metrics")(Html("content"))(request, messages(application)).body

        html must not include "<nav class=\"navbar navbar-expand-md navbar-dark bg-black\"><nav class=\"navbar navbar-expand-md navbar-dark bg-black\">"
      }
    }
  }
}
