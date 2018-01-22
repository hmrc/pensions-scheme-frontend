/*
 * Copyright 2018 HM Revenue & Customs
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

import play.api.data.Form
import forms.register.establishers.company.CompanyRegistrationNumberFormProvider
import models.{NormalMode, CompanyRegistrationNumber}
import views.behaviours.ViewBehaviours
import views.html.register.establishers.company.companyRegistrationNumber

class CompanyRegistrationNumberViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "company__crn"

  val form = new CompanyRegistrationNumberFormProvider()()

  def createView = () => companyRegistrationNumber(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  def createViewUsingForm = (form: Form[_]) => companyRegistrationNumber(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  "CompanyRegistrationNumber view" must {
    behave like normalPage(createView, messageKeyPrefix,messages("messages__company__has_crn"))
  }

  "CompanyRegistrationNumber view" when {
    val crnOptions = Seq("true", "false")

    "rendered" must {
      "contain radio buttons for the value" in {
        val doc = asDocument(createViewUsingForm(form))
        for (option <- crnOptions) {
          assertContainsRadioButton(doc, s"companyRegistrationNumber_hasCrn-$option", "companyRegistrationNumber.hasCrn", option, isChecked=false)
        }
      }
    }

    for(option <- crnOptions) {
      s"rendered with a value of '$option'" must {
        s"have the '$option' radio button selected" in {
          val doc = asDocument(createViewUsingForm(form.bind(Map("companyRegistrationNumber.hasCrn" -> s"$option"))))
          assertContainsRadioButton(doc, s"companyRegistrationNumber_hasCrn-$option", "companyRegistrationNumber_hasCrn", option, isChecked=true)

          for(unselectedOption <- crnOptions.filterNot(o => o == option)) {
            assertContainsRadioButton(doc, s"companyRegistrationNumber_hasCrn-$unselectedOption", "companyRegistrationNumber.hasCrn", unselectedOption,isChecked=false)
          }
        }
      }
    }

    "display an input text box with the value when yes is selected" in {
      val expectedValue = "1234567"
      val doc = asDocument(createViewUsingForm(form.bind(Map("companyRegistrationNumber.hasCrn" -> "true", "companyRegistrationNumber.crn" -> expectedValue))))
      doc must haveLabelAndValue("establisherNino_nino", s"${messages("messages__common__crn")} ${messages("messages__common__crn_hint")}", expectedValue)
    }

    "display an input text box with the value when no is selected" in {
      val expectedValue = "don't have crn"
      val doc = asDocument(createViewUsingForm(form.bind(Map("companyRegistrationNumber.hasCrn" -> "false", "companyRegistrationNumber.reason" -> expectedValue))))
      doc must haveLabelAndValue("companyRegistrationNumber_reason", messages("messages__company__no_crn"), expectedValue)
    }
  }
}
