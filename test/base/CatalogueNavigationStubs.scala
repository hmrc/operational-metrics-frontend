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

package base

import com.github.tomakehurst.wiremock.client.WireMock.*
import uk.gov.hmrc.http.test.WireMockSupport

trait CatalogueNavigationStubs:
  self: WireMockSupport =>

  protected def stubNavigation(): Unit =
    stubFor(
      get(urlEqualTo("/catalogue-config/menu-bar/menu"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(
              """
                |{
                |  "brand": {
                |    "name": "MDTP",
                |    "id": "brand",
                |    "href": "/",
                |    "external": false,
                |    "_type": "TopMenu"
                |  },
                |  "topLevelLinks": [],
                |  "dropdowns": []
                |}
                |""".stripMargin
            )
        )
    )

    stubFor(
      get(urlEqualTo("/catalogue-config/menu-bar/search-index"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("[]")
        )
    )

  protected def stubNavigationUnavailable(): Unit =
    stubFor(
      get(urlEqualTo("/catalogue-config/menu-bar/menu"))
        .willReturn(aResponse().withStatus(503))
    )

    stubFor(
      get(urlEqualTo("/catalogue-config/menu-bar/search-index"))
        .willReturn(aResponse().withStatus(503))
    )
