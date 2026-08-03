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
import org.scalatest.LoneElement

class ServiceLeadTimesViewModelSpec extends BaseSpec with MetricsTestData with LoneElement {

  private val singleOwnership: Map[String, Seq[String]] =
    Map(
      "test-service-one" -> Seq("PlatOps"),
      "test-service-two" -> Seq("MDTP")
    )

  "ServiceLeadTimesViewModel.from" should {

    "flatten service lead times into table rows with one owner" in {
      val result =
        ServiceLeadTimesViewModel.from(
          serviceLeadTimes = Seq(ServiceLeadTimes("test-service-one", Seq(sampleLeadTime))),
          repositoryOwnership = Map("test-service-one" -> Seq("PlatOps")),
          selectedTeam = None
        )

      val row = result.rows.loneElement

      result.selectedTeam mustBe None
      result.teams mustBe Seq("PlatOps")
      row.serviceName mustBe "test-service-one"
      row.teams mustBe Seq("PlatOps")
      row.environment mustBe "Production"
      row.version mustBe "1.2.3"
      row.days mustBe 2
    }

    "include all owning teams when a repository has multiple owners" in {
      val result =
        ServiceLeadTimesViewModel.from(
          serviceLeadTimes = Seq(ServiceLeadTimes("test-service-one", Seq(sampleLeadTime))),
          repositoryOwnership = Map("test-service-one" -> Seq("Platform Engineering", "PlatOps")),
          selectedTeam = None
        )

      result.rows.loneElement.teams mustBe Seq("Platform Engineering", "PlatOps")
    }

    "include a row when filtering by the first of its owning teams" in {
      val result =
        ServiceLeadTimesViewModel.from(
          serviceLeadTimes = Seq(ServiceLeadTimes("test-service-one", Seq(sampleLeadTime))),
          repositoryOwnership = Map("test-service-one" -> Seq("Platform Engineering", "PlatOps")),
          selectedTeam = Some("Platform Engineering")
        )

      result.rows must not be empty
      result.rows.loneElement.serviceName mustBe "test-service-one"
    }

    "include a row when filtering by the second of its owning teams" in {
      val result =
        ServiceLeadTimesViewModel.from(
          serviceLeadTimes = Seq(ServiceLeadTimes("test-service-one", Seq(sampleLeadTime))),
          repositoryOwnership = Map("test-service-one" -> Seq("Platform Engineering", "PlatOps")),
          selectedTeam = Some("PlatOps")
        )

      result.rows must not be empty
      result.rows.loneElement.serviceName mustBe "test-service-one"
    }

    "exclude a row when the selected team is not one of its owners" in {
      val result =
        ServiceLeadTimesViewModel.from(
          serviceLeadTimes = Seq(ServiceLeadTimes("test-service-one", Seq(sampleLeadTime))),
          repositoryOwnership = Map("test-service-one" -> Seq("PlatOps")),
          selectedTeam = Some("MDTP")
        )

      result.rows mustBe empty
    }

    "expose all distinct teams in sorted order across all rows" in {
      val result =
        ServiceLeadTimesViewModel.from(
          serviceLeadTimes = serviceLeadTimes,
          repositoryOwnership = singleOwnership,
          selectedTeam = None
        )

      result.teams mustBe Seq("MDTP", "PlatOps")
    }

    "filter rows by selected team" in {
      val result =
        ServiceLeadTimesViewModel.from(
          serviceLeadTimes = serviceLeadTimes,
          repositoryOwnership = singleOwnership,
          selectedTeam = Some("PlatOps")
        )

      result.selectedTeam mustBe Some("PlatOps")
      result.rows.map(_.serviceName) mustBe Seq("test-service-one")
      result.rows.flatMap(_.teams).distinct mustBe Seq("PlatOps")
    }

    "treat an empty selected team as no filter" in {
      val result =
        ServiceLeadTimesViewModel.from(
          serviceLeadTimes = serviceLeadTimes,
          repositoryOwnership = singleOwnership,
          selectedTeam = Some("")
        )

      result.selectedTeam mustBe None
      result.rows.map(_.serviceName) must contain theSameElementsAs Seq(
        "test-service-one",
        "test-service-two"
      )
    }

    "produce Unknown when no repository matches the service name" in {
      val result =
        ServiceLeadTimesViewModel.from(
          serviceLeadTimes = Seq(ServiceLeadTimes("unmapped-service", Seq(sampleLeadTime))),
          repositoryOwnership = Map.empty,
          selectedTeam = None
        )

      result.teams mustBe Seq("Unknown")
      result.rows.loneElement.teams mustBe Seq("Unknown")
    }

    "produce Unknown when owningTeams is empty" in {
      val result =
        ServiceLeadTimesViewModel.from(
          serviceLeadTimes = Seq(ServiceLeadTimes("test-service-one", Seq(sampleLeadTime))),
          repositoryOwnership = Map("test-service-one" -> Seq.empty),
          selectedTeam = None
        )

      result.teams mustBe Seq("Unknown")
      result.rows.loneElement.teams mustBe Seq("Unknown")
    }

    "not display duplicate owning teams" in {
      val result =
        ServiceLeadTimesViewModel.from(
          serviceLeadTimes = Seq(ServiceLeadTimes("test-service-one", Seq(sampleLeadTime))),
          repositoryOwnership = Map("test-service-one" -> Seq("PlatOps", "PlatOps")),
          selectedTeam = None
        )

      result.rows.loneElement.teams mustBe Seq("PlatOps")
    }

    "discard blank team names and fall back to Unknown when all are blank" in {
      val result =
        ServiceLeadTimesViewModel.from(
          serviceLeadTimes = Seq(ServiceLeadTimes("test-service-one", Seq(sampleLeadTime))),
          repositoryOwnership = Map("test-service-one" -> Seq("", " ", "")),
          selectedTeam = None
        )

      result.rows.loneElement.teams mustBe Seq("Unknown")
    }

    "trim surrounding whitespace from team names before deduplication" in {
      val result =
        ServiceLeadTimesViewModel.from(
          serviceLeadTimes = Seq(ServiceLeadTimes("test-service-one", Seq(sampleLeadTime))),
          repositoryOwnership = Map("test-service-one" -> Seq("PlatOps", " PlatOps ")),
          selectedTeam = None
        )

      result.rows.loneElement.teams mustBe Seq("PlatOps")
    }

    "include all distinct owning teams in the filter options" in {
      val ownership =
        Map(
          "test-service-one" -> Seq("PlatOps", "Platform Engineering"),
          "test-service-two" -> Seq("MDTP")
        )

      val result =
        ServiceLeadTimesViewModel.from(
          serviceLeadTimes = serviceLeadTimes,
          repositoryOwnership = ownership,
          selectedTeam = None
        )

      result.teams mustBe Seq("MDTP", "Platform Engineering", "PlatOps")
    }
  }
}

