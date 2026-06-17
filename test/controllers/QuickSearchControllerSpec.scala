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
import connector.MenuBarConnector
import models.SearchTerm
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.when
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class QuickSearchControllerSpec extends SpecBase {

  "QuickSearchController.search" - {

    "must return OK with JSON search results" in {
      val results = Seq(
        SearchTerm(
          linkType       = "service",
          name           = "catalogue-frontend",
          href           = "/catalogue/service/catalogue-frontend",
          openInNewWindow = false
        )
      )

      val mockConnector = mock[MenuBarConnector]
      when(mockConnector.search(eqTo("catalogue"), eqTo(20))(any[HeaderCarrier]))
        .thenReturn(Future.successful(results))

      val application =
        applicationBuilder()
          .overrides(bind[MenuBarConnector].toInstance(mockConnector))
          .build()

      running(application) {
        val request = FakeRequest(GET, routes.QuickSearchController.search("catalogue").url)
        val result  = route(application, request).value

        status(result) mustBe OK
        contentType(result).value must include("application/json")
        contentAsString(result) must include("catalogue-frontend")
        contentAsString(result) must include("service")
      }
    }

    "must return OK with an empty array when no results are found" in {
      val mockConnector = mock[MenuBarConnector]
      when(mockConnector.search(eqTo("zzznomatch"), eqTo(20))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Seq.empty))

      val application =
        applicationBuilder()
          .overrides(bind[MenuBarConnector].toInstance(mockConnector))
          .build()

      running(application) {
        val request = FakeRequest(GET, routes.QuickSearchController.search("zzznomatch").url)
        val result  = route(application, request).value

        status(result) mustBe OK
        contentAsString(result) mustBe "[]"
      }
    }

    "must respect the limit query parameter" in {
      val mockConnector = mock[MenuBarConnector]
      when(mockConnector.search(eqTo("platform"), eqTo(5))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Seq.empty))

      val application =
        applicationBuilder()
          .overrides(bind[MenuBarConnector].toInstance(mockConnector))
          .build()

      running(application) {
        val request = FakeRequest(GET, routes.QuickSearchController.search("platform", 5).url)
        val result  = route(application, request).value

        status(result) mustBe OK
      }
    }
  }
}
