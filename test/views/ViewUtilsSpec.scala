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

package views

import base.{ApplicationTestSupport, BaseSpec}
import play.api.data.Form
import play.api.data.Forms.{nonEmptyText, single}
import play.api.test.Helpers.*

class ViewUtilsSpec extends BaseSpec with ApplicationTestSupport {

  private val form: Form[String] =
    Form(single("value" -> nonEmptyText))

  "ViewUtils.titleNoForm" should {

    "construct a title without a section" in {
      val application = applicationBuilder().build()

      running(application) {
        implicit val msgs = messages(application)

        ViewUtils.titleNoForm("index.title") mustBe
          "operational-metrics-frontend - operational-metrics-frontend - GOV.UK"
      }
    }

    "construct a title with a section" in {
      val application = applicationBuilder().build()

      running(application) {
        implicit val msgs = messages(application)

        ViewUtils.titleNoForm(
          title = "index.title",
          section = Some("checkYourAnswers.title")
        ) mustBe
          "operational-metrics-frontend - Check your answers - operational-metrics-frontend - GOV.UK"
      }
    }
  }

  "ViewUtils.errorPrefix" should {

    "return the error prefix when the form contains errors" in {
      val application = applicationBuilder().build()

      running(application) {
        implicit val msgs = messages(application)

        val invalidForm =
          form.bind(Map("value" -> ""))

        ViewUtils.errorPrefix(invalidForm) mustBe "Error:"
      }
    }

    "return an empty prefix when the form has no errors" in {
      val application = applicationBuilder().build()

      running(application) {
        implicit val msgs = messages(application)

        val validForm =
          form.bind(Map("value" -> "something"))

        ViewUtils.errorPrefix(validForm) mustBe ""
      }
    }
  }

  "ViewUtils.title" should {

    "include the error prefix when the form contains errors" in {
      val application = applicationBuilder().build()

      running(application) {
        implicit val msgs = messages(application)

        val invalidForm =
          form.bind(Map("value" -> ""))

        ViewUtils.title(invalidForm, "index.title") must startWith("Error:")
      }
    }

    "not include the error prefix when the form is valid" in {
      val application = applicationBuilder().build()

      running(application) {
        implicit val msgs = messages(application)

        val validForm =
          form.bind(Map("value" -> "something"))

        ViewUtils.title(validForm, "index.title") must not startWith "Error:"
      }
    }
  }
}
