/*
 * Copyright 2020 HM Revenue & Customs
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

import controllers.routes
import forms.BenefitsSecuredByInsuranceFormProvider
import models.{NormalMode, UpdateMode}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.benefitsSecuredByInsurance

class BenefitsSecuredByInsuranceViewSpec extends YesNoViewBehaviours {

  val messageKeyPrefix = "benefits_secured_by_insurance"
  val schemeName = "schemename"

  val form = new BenefitsSecuredByInsuranceFormProvider()()
  def postCall = controllers.routes.BenefitsSecuredByInsuranceController.onSubmit(NormalMode, None)

  def createView(): () => HtmlFormat.Appendable = () =>
    benefitsSecuredByInsurance(frontendAppConfig, form, NormalMode, Some(schemeName), postCall, None)(fakeRequest, messages)

  def createUpdateView(): () => HtmlFormat.Appendable = () =>
    benefitsSecuredByInsurance(frontendAppConfig, form, UpdateMode, None, postCall, Some("srn"))(fakeRequest, messages)


  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    benefitsSecuredByInsurance(frontendAppConfig, form, NormalMode, None, postCall, None)(fakeRequest, messages)

  "BenefitsSecuredByInsurance view " must {

    behave like normalPage(createView(), messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__h1",schemeName))

    behave like yesNoPage(createView = createViewUsingForm, messageKeyPrefix = messageKeyPrefix,
      expectedFormAction = routes.BenefitsSecuredByInsuranceController.onSubmit(NormalMode, None).url)

    behave like pageWithReturnLink(createView(), getReturnLink)

    behave like pageWithReturnLinkAndSrn(createUpdateView(), getReturnLinkWithSrn)
  }
}
