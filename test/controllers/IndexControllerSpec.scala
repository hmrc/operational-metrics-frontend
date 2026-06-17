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

import base.SpecBase
import connector.{MenuBarConnector, OperationalMetricsConnector}
import models.{BannerMenu, MenuLink}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class IndexControllerSpec extends SpecBase {

  private val emptyMenu = BannerMenu(
    brand         = MenuLink("brand", "MDTP", "/"),
    topLevelLinks = Seq.empty,
    dropdowns     = Seq.empty
  )

  "IndexController.onPageLoad" - {

    "must return OK and render service lead time data" in {
      val mockOperationalMetricsConnector = mock[OperationalMetricsConnector]
      when(mockOperationalMetricsConnector.getServiceLeadTimes()(any[HeaderCarrier])) thenReturn Future.successful(serviceLeadTimes)

      val mockMenuBarConnector = mock[MenuBarConnector]
      when(mockMenuBarConnector.getMenu()(any[HeaderCarrier])) thenReturn Future.successful(emptyMenu)

      val application =
        applicationBuilder()
          .overrides(
            bind[OperationalMetricsConnector].toInstance(mockOperationalMetricsConnector),
            bind[MenuBarConnector].toInstance(mockMenuBarConnector)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, routes.IndexController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustBe OK
        contentAsString(result) must include("Operational Metrics")
        contentAsString(result) must include("test-service-one")
        contentAsString(result) must include("test-service-two")
      }
    }

    "must pass the selected team query parameter into the view model" in {
      val mockOperationalMetricsConnector = mock[OperationalMetricsConnector]
      when(mockOperationalMetricsConnector.getServiceLeadTimes()(any[HeaderCarrier])) thenReturn Future.successful(serviceLeadTimes)

      val mockMenuBarConnector = mock[MenuBarConnector]
      when(mockMenuBarConnector.getMenu()(any[HeaderCarrier])) thenReturn Future.successful(emptyMenu)

      val application =
        applicationBuilder()
          .overrides(
            bind[OperationalMetricsConnector].toInstance(mockOperationalMetricsConnector),
            bind[MenuBarConnector].toInstance(mockMenuBarConnector)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, routes.IndexController.onPageLoad().url + "?team=PlatOps")
        val result = route(application, request).value

        status(result) mustBe OK
        contentAsString(result) must include("test-service-one")
        contentAsString(result) must not include "test-service-two"
      }
    }
  }
}
