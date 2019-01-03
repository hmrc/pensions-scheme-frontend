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

package views.register.trustees.company

import forms.CompanyRegistrationNumberFormProvider
import models.{Index, NormalMode}
import play.api.data.Form
import views.behaviours.ViewBehaviours
import views.html.register.trustees.company.companyRegistrationNumber

class CompanyRegistrationNumberViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "companyRegistrationNumber"
  val index = Index(0)
  val form = new CompanyRegistrationNumberFormProvider()()

  private def createView() = () =>
    companyRegistrationNumber(frontendAppConfig, form, NormalMode, index)(fakeRequest, messages)

  private def createViewUsingForm = (form: Form[_]) =>
    companyRegistrationNumber(frontendAppConfig, form, NormalMode, index)(fakeRequest, messages)

  "CompanyRegistrationNumber view" when {
    behave like normalPage(createView(), messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"))
    behave like pageWithReturnLink(createView(), controllers.register.routes.SchemeTaskListController.onPageLoad().url)

    "Generate correct hint text" in {
      val doc = asDocument(createView()())
      assertContainsText(doc, messages("messages__common__crn_hint"))
    }

    val crnOptions = Seq("true", "false")
    "rendered" must {
      "contain radio buttons for the value" in {
        val doc = asDocument(createViewUsingForm(form))
        for (option <- crnOptions) {
          assertContainsRadioButton(doc, s"companyRegistrationNumber_hasCrn-$option", "companyRegistrationNumber.hasCrn", option, false)
        }
      }
    }

    for (option <- crnOptions) {
      s"rendered with a value of '$option'" must {
        s"have the '$option' radio button selected" in {
          val doc = asDocument(createViewUsingForm(form.bind(Map("companyRegistrationNumber.hasCrn" -> s"$option"))))
          assertContainsRadioButton(doc, s"companyRegistrationNumber_hasCrn-$option", "companyRegistrationNumber.hasCrn", option, true)

          for (unselectedOption <- crnOptions.filterNot(o => o == option)) {
            assertContainsRadioButton(doc, s"companyRegistrationNumber_hasCrn-$unselectedOption", "companyRegistrationNumber.hasCrn", unselectedOption, false)
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
