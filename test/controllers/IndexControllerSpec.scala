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

import base.{ApplicationTestSupport, BaseSpec, CatalogueNavigationStubs, MetricsTestData}
import connector.{OperationalMetricsConnector, TeamsAndRepositoriesConnector}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.test.WireMockSupport

import scala.concurrent.Future

class IndexControllerSpec
    extends BaseSpec
    with MetricsTestData
    with ApplicationTestSupport
    with MockitoSugar
    with WireMockSupport
    with CatalogueNavigationStubs:

  private val sampleOwnership: Map[String, Seq[String]] =
    Map(
      "test-service-one" -> Seq("PlatOps"),
      "test-service-two" -> Seq("MDTP")
    )

  private def applicationWith(
      mockOperationalMetricsConnector: OperationalMetricsConnector,
      mockTeamsAndRepositoriesConnector: TeamsAndRepositoriesConnector
  ) =
    applicationBuilder()
      .configure(
        "microservice.services.menu-bar.protocol" -> "http",
        "microservice.services.menu-bar.host"     -> wireMockHost,
        "microservice.services.menu-bar.port"     -> wireMockPort
      )
      .overrides(
        bind[OperationalMetricsConnector].toInstance(mockOperationalMetricsConnector),
        bind[TeamsAndRepositoriesConnector].toInstance(mockTeamsAndRepositoriesConnector)
      )
      .build()

  "IndexController.onPageLoad" should {

    "return OK and render service lead time data with ownership" in {
      val mockOperationalMetricsConnector = mock[OperationalMetricsConnector]
      val mockTeamsAndRepositoriesConnector = mock[TeamsAndRepositoriesConnector]

      when(mockOperationalMetricsConnector.getServiceLeadTimes()(any[HeaderCarrier]))
        .thenReturn(Future.successful(serviceLeadTimes))

      when(mockTeamsAndRepositoriesConnector.getRepositoryOwnership()(any[HeaderCarrier]))
        .thenReturn(Future.successful(sampleOwnership))

      stubNavigation()

      val application =
        applicationWith(mockOperationalMetricsConnector, mockTeamsAndRepositoriesConnector)

      running(application) {
        val request =
          FakeRequest(GET, routes.IndexController.onPageLoad().url)

        val result =
          route(application, request).value

        status(result) mustBe OK
        contentAsString(result) must include("Operational Metrics")
        contentAsString(result) must include("test-service-one")
        contentAsString(result) must include("test-service-two")
        contentAsString(result) must include(controllers.auth.routes.AuthController.signOut().url)
      }
    }

    "render multiple owning teams for a service" in {
      val mockOperationalMetricsConnector = mock[OperationalMetricsConnector]
      val mockTeamsAndRepositoriesConnector = mock[TeamsAndRepositoriesConnector]

      when(mockOperationalMetricsConnector.getServiceLeadTimes()(any[HeaderCarrier]))
        .thenReturn(Future.successful(serviceLeadTimes))

      when(mockTeamsAndRepositoriesConnector.getRepositoryOwnership()(any[HeaderCarrier]))
        .thenReturn(Future.successful(Map("test-service-one" -> Seq("Platform Engineering", "PlatOps"))))

      stubNavigation()

      val application =
        applicationWith(mockOperationalMetricsConnector, mockTeamsAndRepositoriesConnector)

      running(application) {
        val request =
          FakeRequest(GET, routes.IndexController.onPageLoad().url)

        val result =
          route(application, request).value

        status(result) mustBe OK
        contentAsString(result) must include("Platform Engineering")
        contentAsString(result) must include("PlatOps")
      }
    }

    "pass the selected team query parameter into the view model" in {
      val mockOperationalMetricsConnector = mock[OperationalMetricsConnector]
      val mockTeamsAndRepositoriesConnector = mock[TeamsAndRepositoriesConnector]

      when(mockOperationalMetricsConnector.getServiceLeadTimes()(any[HeaderCarrier]))
        .thenReturn(Future.successful(serviceLeadTimes))

      when(mockTeamsAndRepositoriesConnector.getRepositoryOwnership()(any[HeaderCarrier]))
        .thenReturn(Future.successful(sampleOwnership))

      stubNavigation()

      val application =
        applicationWith(mockOperationalMetricsConnector, mockTeamsAndRepositoriesConnector)

      running(application) {
        val request =
          FakeRequest(GET, routes.IndexController.onPageLoad().url + "?team=PlatOps")

        val result =
          route(application, request).value

        status(result) mustBe OK
        contentAsString(result) must include("test-service-one")
        contentAsString(result) must not include "test-service-two"
        contentAsString(result) must include(controllers.auth.routes.AuthController.signOut().url)
      }
    }

    "return OK and show Unknown when teams-and-repositories fails" in {
      val mockOperationalMetricsConnector = mock[OperationalMetricsConnector]
      val mockTeamsAndRepositoriesConnector = mock[TeamsAndRepositoriesConnector]

      when(mockOperationalMetricsConnector.getServiceLeadTimes()(any[HeaderCarrier]))
        .thenReturn(Future.successful(serviceLeadTimes))

      when(mockTeamsAndRepositoriesConnector.getRepositoryOwnership()(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("teams-and-repositories unavailable")))

      stubNavigation()

      val application =
        applicationWith(mockOperationalMetricsConnector, mockTeamsAndRepositoriesConnector)

      running(application) {
        val request =
          FakeRequest(GET, routes.IndexController.onPageLoad().url)

        val result =
          route(application, request).value

        status(result) mustBe OK
        contentAsString(result) must include("Unknown")
        contentAsString(result) must include("test-service-one")
      }
    }

    "propagate failures from operational-metrics as a failed response" in {
      val mockOperationalMetricsConnector = mock[OperationalMetricsConnector]
      val mockTeamsAndRepositoriesConnector = mock[TeamsAndRepositoriesConnector]

      when(mockOperationalMetricsConnector.getServiceLeadTimes()(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("operational-metrics unavailable")))

      when(mockTeamsAndRepositoriesConnector.getRepositoryOwnership()(any[HeaderCarrier]))
        .thenReturn(Future.successful(sampleOwnership))

      stubNavigation()

      val application =
        applicationWith(mockOperationalMetricsConnector, mockTeamsAndRepositoriesConnector)

      running(application) {
        val request =
          FakeRequest(GET, routes.IndexController.onPageLoad().url)

        val result =
          route(application, request).value

        intercept[RuntimeException] {
          status(result)
        }.getMessage mustBe "operational-metrics unavailable"
      }
    }
  }

