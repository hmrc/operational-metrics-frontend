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

package connector

import base.BaseSpec
import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.LoneElement
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import play.api.Configuration
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.test.{HttpClientV2Support, WireMockSupport}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

class OperationalMetricsConnectorSpec
    extends BaseSpec
    with ScalaFutures
    with IntegrationPatience
    with LoneElement
    with HttpClientV2Support
    with WireMockSupport {

  private val servicesConfig =
    new ServicesConfig(
      Configuration.from(
        Map(
          "microservice.services.operational-metrics.protocol" -> "http",
          "microservice.services.operational-metrics.host"     -> wireMockHost,
          "microservice.services.operational-metrics.port"     -> wireMockPort
        )
      )
    )

  private val connector =
    new OperationalMetricsConnector(
      httpClient     = httpClientV2,
      servicesConfig = servicesConfig
    )(scala.concurrent.ExecutionContext.global)

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  "getServiceLeadTimes" should {

    "call operational-metrics and decode the response" in {
      stubFor(
        get(urlEqualTo("/operational-metrics/service-lead-times"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody(
                """
                  |[
                  |  {
                  |    "serviceName": "test-service-one",
                  |    "leadTimes": [
                  |      {
                  |        "environment": "Production",
                  |        "version": "1.2.3",
                  |        "slugCreatedAt": "2026-06-01T09:00:00Z",
                  |        "firstDeployedAt": "2026-06-03T11:00:00Z",
                  |        "days": 2
                  |      }
                  |    ]
                  |  }
                  |]
                  |""".stripMargin
              )
          )
      )

      val result = connector.getServiceLeadTimes().futureValue

      val service = result.loneElement
      service.serviceName mustBe "test-service-one"

      val leadTime = service.leadTimes.loneElement
      leadTime.version mustBe "1.2.3"
      leadTime.days mustBe 2

      verify(getRequestedFor(urlEqualTo("/operational-metrics/service-lead-times")))
    }
  }
}
