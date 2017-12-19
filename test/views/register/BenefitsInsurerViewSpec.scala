/*
 * Copyright 2017 HM Revenue & Customs
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

package views.register

import play.api.data.Form
import controllers.register.routes
import forms.register.BenefitsInsurerFormProvider
import models.NormalMode
import models.BenefitsInsurer
import views.behaviours.QuestionViewBehaviours
import views.html.register.benefitsInsurer

class BenefitsInsurerViewSpec extends QuestionViewBehaviours[BenefitsInsurer] {

  val messageKeyPrefix = "benefitsInsurer"

  override val form = new BenefitsInsurerFormProvider()()

  def createView = () => benefitsInsurer(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  def createViewUsingForm = (form: Form[_]) => benefitsInsurer(frontendAppConfig, form, NormalMode)(fakeRequest, messages)


  "BenefitsInsurer view" must {

    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithTextFields(createViewUsingForm, messageKeyPrefix, routes.BenefitsInsurerController.onSubmit(NormalMode).url, "companyName", "policyNumber")
  }
}
