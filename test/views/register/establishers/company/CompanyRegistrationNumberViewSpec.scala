/*
 * Copyright 2019 HM Revenue & Customs
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

package views.register.establishers.company

import forms.CompanyRegistrationNumberFormProvider
import models.{Index, NormalMode}
import play.api.data.Form
import views.behaviours.ViewBehaviours
import views.html.register.establishers.company.companyRegistrationNumber

class CompanyRegistrationNumberViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "company__crn"
  val index = Index(1)
  val form = new CompanyRegistrationNumberFormProvider()()

  private def createView() = () =>
    companyRegistrationNumber(frontendAppConfig, form, NormalMode, index)(fakeRequest, messages)

  private def createViewUsingForm = (form: Form[_]) =>
    companyRegistrationNumber(frontendAppConfig, form, NormalMode, index)(fakeRequest, messages)

  "CompanyRegistrationNumber view" when {
    val crnOptions = Seq("true", "false")

    behave like normalPage(createView(), messageKeyPrefix, messages("messages__company__has_crn"))

    "Generate correct hint text" in {
      val doc = asDocument(createView()())
      assertContainsText(doc, messages("messages__common__crn_hint"))
    }

    behave like pageWithReturnLink(createView(), controllers.register.routes.SchemeTaskListController.onPageLoad().url)

    "rendered" must {
      "contain radio buttons for the value" in {
        val doc = asDocument(createViewUsingForm(form))
        for (option <- crnOptions) {
          assertContainsRadioButton(doc, s"companyRegistrationNumber_hasCrn-$option", "companyRegistrationNumber.hasCrn", option, isChecked = false)
        }
      }
    }

    for (option <- crnOptions) {
      s"rendered with a value of '$option'" must {
        s"have the '$option' radio button selected" in {
          val doc = asDocument(createViewUsingForm(form.bind(Map("companyRegistrationNumber.hasCrn" -> s"$option"))))
          assertContainsRadioButton(doc, s"companyRegistrationNumber_hasCrn-$option", "companyRegistrationNumber.hasCrn", option, isChecked = true)

          for (unselectedOption <- crnOptions.filterNot(o => o == option)) {
            assertContainsRadioButton(doc, s"companyRegistrationNumber_hasCrn-$unselectedOption", "companyRegistrationNumber.hasCrn", unselectedOption, isChecked = false)
          }
        }
      }
    }

    "display an input text box with the value when yes is selected" in {
      val expectedValue = "1234567"
      val doc = asDocument(createViewUsingForm(form.bind(Map("companyRegistrationNumber.hasCrn" -> "true", "companyRegistrationNumber.crn" -> expectedValue))))
      doc must haveLabelAndValue("companyRegistrationNumber_crn", s"${messages("messages__common__crn")}", expectedValue)
    }

    "display an input text box with the value when no is selected" in {
      val expectedValue = "don't have crn"
      val doc = asDocument(createViewUsingForm(form.bind(Map("companyRegistrationNumber.hasCrn" -> "false", "companyRegistrationNumber.reason" -> expectedValue))))
      doc must haveLabelAndValue("companyRegistrationNumber_reason", messages("messages__company__no_crn"), expectedValue)
    }
  }
}
