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

import models.{LeadTimeMeasurement, ServiceLeadTimes}

import java.time.Instant

trait MetricsTestData {

  protected val sampleLeadTime: LeadTimeMeasurement =
    LeadTimeMeasurement(
      environment     = "Production",
      version         = "1.2.3",
      slugCreatedAt   = Instant.parse("2026-06-01T09:00:00Z"),
      firstDeployedAt = Instant.parse("2026-06-03T11:00:00Z"),
      days            = 2
    )

  protected val anotherLeadTime: LeadTimeMeasurement =
    LeadTimeMeasurement(
      environment     = "Production",
      version         = "2.4.0",
      slugCreatedAt   = Instant.parse("2026-05-20T12:00:00Z"),
      firstDeployedAt = Instant.parse("2026-05-27T12:00:00Z"),
      days            = 7
    )

  protected val serviceLeadTimes: Seq[ServiceLeadTimes] =
    Seq(
      ServiceLeadTimes("test-service-one", Seq(sampleLeadTime)),
      ServiceLeadTimes("test-service-two", Seq(anotherLeadTime))
    )
}
