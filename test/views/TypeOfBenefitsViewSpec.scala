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

package views

import forms.TypeOfBenefitsFormProvider
import models.{NormalMode, TypeOfBenefits}
import play.api.data.Form
import views.behaviours.ViewBehaviours
import views.html.typeOfBenefits

class TypeOfBenefitsViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "type_of_benefits"

  val form = new TypeOfBenefitsFormProvider()()
  val schemeName = "schemename"

  private def createView() = () =>
    typeOfBenefits(frontendAppConfig, form, NormalMode, Some(schemeName))(fakeRequest, messages)

  private def createViewUsingForm = (form: Form[_]) =>
    typeOfBenefits(frontendAppConfig, form, NormalMode, Some(schemeName))(fakeRequest, messages)

  "Type of benefits view" when {
    "rendered" must {
      behave like normalPage(createView(), messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__h1",schemeName))

      behave like pageWithReturnLink(createView(), getReturnLink)

      "contain radio buttons for the value" in {
        val doc = asDocument(createViewUsingForm(form))
        for (option <- TypeOfBenefits.options) {
          assertContainsRadioButton(doc, s"value-${option.value}", "value", option.value, false)
        }
      }
    }

    for (option <- TypeOfBenefits.options) {
      s"rendered with a value of '${option.value}'" must {
        s"have the '${option.value}' radio button selected" in {
          val doc = asDocument(createViewUsingForm(form.bind(Map("value" -> s"${option.value}"))))
          assertContainsRadioButton(doc, s"value-${option.value}", "value", option.value, true)

          for (unselectedOption <- TypeOfBenefits.options.filterNot(o => o == option)) {
            assertContainsRadioButton(doc, s"value-${unselectedOption.value}", "value", unselectedOption.value, false)
          }
        }
      }
    }
  }
}
