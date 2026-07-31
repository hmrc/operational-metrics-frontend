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

import models.ServiceLeadTimes

import java.time.Instant

final case class ServiceLeadTimesViewModel(
    selectedTeam: Option[String],
    teams: Seq[String],
    rows: Seq[ServiceLeadTimeRow]
)

final case class ServiceLeadTimeRow(
    serviceName: String,
    teams: Seq[String],
    environment: String,
    version: String,
    slugCreatedAt: Instant,
    firstDeployedAt: Instant,
    days: Int
)

object ServiceLeadTimesViewModel {

  def from(
      serviceLeadTimes: Seq[ServiceLeadTimes],
      repositoryOwnership: Map[String, Seq[String]],
      selectedTeam: Option[String]
  ): ServiceLeadTimesViewModel = {

    val allRows =
      serviceLeadTimes.flatMap { service =>
        val owners =
          repositoryOwnership
            .getOrElse(service.serviceName, Seq.empty)
            .filter(_.trim.nonEmpty)
            .distinct
            .sortBy(_.toLowerCase)

        val teams = if (owners.isEmpty) Seq("Unknown") else owners

        service.leadTimes.map { leadTime =>
          ServiceLeadTimeRow(
            serviceName = service.serviceName,
            teams = teams,
            environment = leadTime.environment,
            version = leadTime.version,
            slugCreatedAt = leadTime.slugCreatedAt,
            firstDeployedAt = leadTime.firstDeployedAt,
            days = leadTime.days
          )
        }
      }

    val teams =
      allRows
        .flatMap(_.teams)
        .distinct
        .sortBy(_.toLowerCase)

    val filteredRows =
      selectedTeam
        .filter(_.nonEmpty)
        .fold(allRows)(team => allRows.filter(_.teams.contains(team)))

    ServiceLeadTimesViewModel(
      selectedTeam = selectedTeam.filter(_.nonEmpty),
      teams = teams,
      rows = filteredRows.sortBy(row => (row.teams.headOption.getOrElse(""), row.serviceName, row.environment, row.version))
    )
  }
}
