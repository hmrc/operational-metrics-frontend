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

import controllers.actions.{FakeInternalAuthAction, InternalAuthAction}
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest

trait ApplicationTestSupport {

  protected def messages(app: Application): Messages =
    app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  protected def applicationBuilder(): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure(testConfiguration)
      .overrides(
        bind[InternalAuthAction].to[FakeInternalAuthAction]
      )

  protected val testConfiguration: Map[String, Any] =
    Map(
      "catalogue-frontend.base-url" -> "http://localhost:9017",
      "microservice.services.operational-metrics.protocol" -> "http",
      "microservice.services.operational-metrics.host" -> "localhost",
      "microservice.services.operational-metrics.port" -> 8863,
      "contact-frontend.host" -> "http://localhost:9250",
      "host" -> "http://localhost:9000",
      "mongodb.uri" -> "mongodb://localhost:27017/operational-metrics-frontend-test",
      "mongodb.timeToLiveInSeconds" -> 900,
      "timeout-dialog.timeout" -> 900,
      "timeout-dialog.countdown" -> 120,
      "features.welsh-translation" -> true,
      "play.filters.disabled.0" -> "play.filters.csrf.CSRFFilter",
      "play.filters.disabled.1" -> "play.filters.csp.CSPFilter"
    )
}
