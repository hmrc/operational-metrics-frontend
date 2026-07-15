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

package models

import base.{BaseSpec, MetricsTestData}
import play.api.libs.json.{Json, JsSuccess}

class ServiceLeadTimesSpec extends BaseSpec with MetricsTestData {

  "ServiceLeadTimes JSON format" should {

    "read backend JSON" in {
      val json = Json.parse(
        """
          |{
          |  "serviceName": "test-service-one",
          |  "leadTimes": [
          |    {
          |      "environment": "Production",
          |      "version": "1.2.3",
          |      "slugCreatedAt": "2026-06-01T09:00:00Z",
          |      "firstDeployedAt": "2026-06-03T11:00:00Z",
          |      "days": 2
          |    }
          |  ]
          |}
          |""".stripMargin
      )

      val expected = ServiceLeadTimes(
        serviceName = "test-service-one",
        leadTimes = Seq(sampleLeadTime)
      )

      json.validate[ServiceLeadTimes] mustBe JsSuccess(expected)
    }

    "write backend JSON" in {
      val expectedJson = Json.obj(
        "serviceName" -> "test-service-one",
        "leadTimes" -> Json.arr(
          Json.obj(
            "environment" -> "Production",
            "version" -> "1.2.3",
            "slugCreatedAt" -> "2026-06-01T09:00:00Z",
            "firstDeployedAt" -> "2026-06-03T11:00:00Z",
            "days" -> 2
          )
        )
      )

      Json.toJson(ServiceLeadTimes("test-service-one", Seq(sampleLeadTime))) mustBe expectedJson
    }
  }
}
