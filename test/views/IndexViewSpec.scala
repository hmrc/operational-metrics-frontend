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

import base.{ApplicationTestSupport, BaseSpec, MetricsTestData}
import controllers.routes
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import viewmodels.ServiceLeadTimesViewModel
import views.html.IndexView

class IndexViewSpec extends BaseSpec with MetricsTestData with ApplicationTestSupport {

  private val singleOwnership: Map[String, Seq[String]] =
    Map(
      "test-service-one" -> Seq("PlatOps"),
      "test-service-two" -> Seq("MDTP")
    )

  "IndexView" should {

    "render the operational metrics table with Team(s) heading" in {
      val application = applicationBuilder().build()

      running(application) {
        val view = application.injector.instanceOf[IndexView]
        val request = FakeRequest(GET, routes.IndexController.onPageLoad().url)
        val viewModel = ServiceLeadTimesViewModel.from(serviceLeadTimes, singleOwnership, None)

        val html = view(viewModel)(request, messages(application)).body

        html must include("Operational Metrics")
        html must include("Service lead times")
        html must include("Team(s)")
        html must include("test-service-one")
        html must include("test-service-two")
        html must include("1.2.3")
        html must include("2.4.0")
        html must include("Lead time days")
      }
    }

    "render multiple owning teams comma-separated" in {
      val application = applicationBuilder().build()

      running(application) {
        val view = application.injector.instanceOf[IndexView]
        val request = FakeRequest(GET, routes.IndexController.onPageLoad().url)
        val ownership = Map("test-service-one" -> Seq("Platform Engineering", "PlatOps"))
        val viewModel = ServiceLeadTimesViewModel.from(serviceLeadTimes, ownership, None)

        val html = view(viewModel)(request, messages(application)).body

        html must include("Platform Engineering, PlatOps")
      }
    }

    "URL-encode team names containing spaces in filter hrefs" in {
      val application = applicationBuilder().build()

      running(application) {
        val view = application.injector.instanceOf[IndexView]
        val request = FakeRequest(GET, routes.IndexController.onPageLoad().url)
        val ownership = Map("test-service-one" -> Seq("Platform Engineering"))
        val viewModel = ServiceLeadTimesViewModel.from(serviceLeadTimes, ownership, None)

        val html = view(viewModel)(request, messages(application)).body

        html must include("?team=Platform+Engineering")
        html must not include "?team=Platform Engineering"
      }
    }

    "show the selected team and clear filters link when filtered by first owner" in {
      val application = applicationBuilder().build()

      running(application) {
        val view = application.injector.instanceOf[IndexView]
        val request = FakeRequest(GET, routes.IndexController.onPageLoad().url + "?team=PlatOps")
        val viewModel = ServiceLeadTimesViewModel.from(serviceLeadTimes, singleOwnership, Some("PlatOps"))

        val html = view(viewModel)(request, messages(application)).body

        html must include("PlatOps")
        html must include("Clear filters")
        html must include("test-service-one")
        html must not include "test-service-two"
      }
    }

    "display a service when filtering by either of its owning teams" in {
      val application = applicationBuilder().build()

      running(application) {
        val view = application.injector.instanceOf[IndexView]
        val ownership = Map("test-service-one" -> Seq("Platform Engineering", "PlatOps"))

        val htmlFilteredByFirst =
          view(
            ServiceLeadTimesViewModel.from(serviceLeadTimes, ownership, Some("Platform Engineering"))
          )(FakeRequest(GET, routes.IndexController.onPageLoad().url), messages(application)).body

        val htmlFilteredBySecond =
          view(
            ServiceLeadTimesViewModel.from(serviceLeadTimes, ownership, Some("PlatOps"))
          )(FakeRequest(GET, routes.IndexController.onPageLoad().url), messages(application)).body

        htmlFilteredByFirst must include("test-service-one")
        htmlFilteredBySecond must include("test-service-one")
      }
    }
  }
}

