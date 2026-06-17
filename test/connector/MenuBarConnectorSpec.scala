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

import base.SpecBase
import com.github.tomakehurst.wiremock.client.WireMock._
import play.api.Configuration
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.test.{HttpClientV2Support, WireMockSupport}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

class MenuBarConnectorSpec
    extends SpecBase
    with HttpClientV2Support
    with WireMockSupport {

  private val servicesConfig =
    new ServicesConfig(
      Configuration.from(
        Map(
          "microservice.services.menu-bar.protocol" -> "http",
          "microservice.services.menu-bar.host"     -> wireMockHost,
          "microservice.services.menu-bar.port"     -> wireMockPort
        )
      )
    )

  private val connector =
    new MenuBarConnector(
      httpClient     = httpClientV2,
      servicesConfig = servicesConfig
    )(scala.concurrent.ExecutionContext.global)

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  "getMenu" - {

    "must call menu-bar and decode the response" in {
      stubFor(
        get(urlEqualTo("/menu-bar/menu"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody(
                """
                  |{
                  |  "brand": { "id": "brand", "text": "MDTP", "href": "/" },
                  |  "topLevelLinks": [
                  |    { "id": "catalogue", "text": "Catalogue", "href": "/catalogue" }
                  |  ],
                  |  "dropdowns": []
                  |}
                  |""".stripMargin
              )
          )
      )

      val result = connector.getMenu().futureValue

      result.brand.text mustBe "MDTP"
      result.topLevelLinks.size mustBe 1
      result.topLevelLinks.head.id mustBe "catalogue"
      result.dropdowns mustBe empty

      verify(getRequestedFor(urlEqualTo("/menu-bar/menu")))
    }
  }

  "search" - {

    "must call menu-bar quicksearch and decode the response" in {
      stubFor(
        get(urlPathEqualTo("/menu-bar/quicksearch"))
          .withQueryParam("query", equalTo("platform"))
          .withQueryParam("limit", equalTo("20"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody(
                """
                  |[
                  |  {
                  |    "linkType": "service",
                  |    "name": "platform-service",
                  |    "href": "/catalogue/service/platform-service",
                  |    "weight": 0.8,
                  |    "hints": ["platform"],
                  |    "openInNewWindow": false
                  |  }
                  |]
                  |""".stripMargin
              )
          )
      )

      val result = connector.search("platform").futureValue

      result.size mustBe 1
      result.head.linkType mustBe "service"
      result.head.name mustBe "platform-service"
      result.head.href mustBe "/catalogue/service/platform-service"
      result.head.openInNewWindow mustBe false

      verify(getRequestedFor(urlPathEqualTo("/menu-bar/quicksearch"))
        .withQueryParam("query", equalTo("platform"))
        .withQueryParam("limit", equalTo("20")))
    }

    "must return an empty list when no results are found" in {
      stubFor(
        get(urlPathEqualTo("/menu-bar/quicksearch"))
          .withQueryParam("query", equalTo("zzznomatch"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody("[]")
          )
      )

      val result = connector.search("zzznomatch").futureValue

      result mustBe empty

      verify(getRequestedFor(urlPathEqualTo("/menu-bar/quicksearch"))
        .withQueryParam("query", equalTo("zzznomatch")))
    }
  }
}
