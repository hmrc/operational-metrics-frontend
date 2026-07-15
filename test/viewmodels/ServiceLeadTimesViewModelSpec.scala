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

package viewmodels

import base.{BaseSpec, MetricsTestData}
import models.ServiceLeadTimes

class ServiceLeadTimesViewModelSpec extends BaseSpec with MetricsTestData {

  "ServiceLeadTimesViewModel.from" should {

    "flatten service lead times into table rows" in {
      val result =
        ServiceLeadTimesViewModel.from(
          serviceLeadTimes = Seq(ServiceLeadTimes("test-service-one", Seq(sampleLeadTime))),
          selectedTeam     = None
        )

      result.selectedTeam mustBe None
      result.teams mustBe Seq("PlatOps")
      result.rows.size mustBe 1
      result.rows.head.serviceName mustBe "test-service-one"
      result.rows.head.team mustBe "PlatOps"
      result.rows.head.environment mustBe "Production"
      result.rows.head.version mustBe "1.2.3"
      result.rows.head.days mustBe 2
    }

    "expose all distinct teams in sorted order" in {
      val result =
        ServiceLeadTimesViewModel.from(
          serviceLeadTimes = serviceLeadTimes,
          selectedTeam     = None
        )

      result.teams mustBe Seq("MDTP", "PlatOps")
    }

    "filter rows by selected team" in {
      val result =
        ServiceLeadTimesViewModel.from(
          serviceLeadTimes = serviceLeadTimes,
          selectedTeam     = Some("PlatOps")
        )

      result.selectedTeam mustBe Some("PlatOps")
      result.rows.map(_.serviceName) mustBe Seq("test-service-one")
      result.rows.map(_.team).distinct mustBe Seq("PlatOps")
    }

    "treat an empty selected team as no filter" in {
      val result =
        ServiceLeadTimesViewModel.from(
          serviceLeadTimes = serviceLeadTimes,
          selectedTeam     = Some("")
        )

      result.selectedTeam mustBe None
      result.rows.map(_.serviceName) must contain theSameElementsAs Seq("test-service-one", "test-service-two")
    }

    "put unmapped services into Unknown" in {
      val result =
        ServiceLeadTimesViewModel.from(
          serviceLeadTimes = Seq(ServiceLeadTimes("unmapped-service", Seq(sampleLeadTime))),
          selectedTeam     = None
        )

      result.teams mustBe Seq("Unknown")
      result.rows.head.team mustBe "Unknown"
    }
  }
}
