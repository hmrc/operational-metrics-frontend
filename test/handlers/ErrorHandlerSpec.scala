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

import base.{ApplicationTestSupport, BaseSpec, CatalogueNavigationStubs}
import com.github.tomakehurst.wiremock.client.WireMock.*
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.http.test.WireMockSupport

class ErrorHandlerSpec
    extends BaseSpec
    with ApplicationTestSupport
    with ScalaFutures
    with IntegrationPatience
    with WireMockSupport
    with CatalogueNavigationStubs:

  private def applicationWithMenuBar() =
    applicationBuilder()
      .configure(
        "microservice.services.menu-bar.protocol" -> "http",
        "microservice.services.menu-bar.host"     -> wireMockHost,
        "microservice.services.menu-bar.port"     -> wireMockPort
      )
      .build()

  "ErrorHandler.standardErrorTemplate" should {

    "produce a complete wrapped error page when navigation is available" in {
      stubNavigation()

      val app = applicationWithMenuBar()

      running(app) {
        val handler          = app.injector.instanceOf[ErrorHandler]
        implicit val request = FakeRequest()
        val result           = handler
          .standardErrorTemplate("error.heading", "error.heading", "error.message")
          .futureValue

        result.body must include("<!DOCTYPE html>")
        result.body must include("Stub navigation item")
        result.body must include("error.heading")

        verify(getRequestedFor(urlEqualTo("/catalogue-config/menu-bar/menu")))
        verify(getRequestedFor(urlEqualTo("/catalogue-config/menu-bar/search-index")))
      }
    }

    "produce a complete wrapped error page with empty navigation when the menu backend is unavailable" in {
      stubNavigationUnavailable()

      val app = applicationWithMenuBar()

      running(app) {
        val handler          = app.injector.instanceOf[ErrorHandler]
        implicit val request = FakeRequest()
        val result           = handler
          .standardErrorTemplate("error.heading", "error.heading", "error.message")
          .futureValue

        result.body must include("<!DOCTYPE html>")
        result.body must include("MDTP")
        result.body must not include "Stub navigation item"
        result.body must include("error.heading")

        verify(getRequestedFor(urlEqualTo("/catalogue-config/menu-bar/menu")))
        verify(getRequestedFor(urlEqualTo("/catalogue-config/menu-bar/search-index")))
      }
    }
  }
