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

import forms.InsurancePolicyNumberFormProvider
import models.{NormalMode, UpdateMode}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.QuestionViewBehaviours
import views.html.insurancePolicyNumber

class InsurancePolicyNumberViewSpec extends QuestionViewBehaviours[String] {

  val messageKeyPrefix = "insurance_policy_number"
  private val insuranceCompanyName = "test company"
  private val postCall = controllers.routes.BenefitsSecuredByInsuranceController.onSubmit(NormalMode, None)

  override val form = new InsurancePolicyNumberFormProvider()()

  val view: insurancePolicyNumber = app.injector.instanceOf[insurancePolicyNumber]

  def createView(companyName : Option[String] = None): () => HtmlFormat.Appendable = () =>
    view(form, NormalMode, companyName, None, postCall, None)(fakeRequest, messages)


  def createUpdateView(companyName : Option[String] = None): () => HtmlFormat.Appendable = () =>
    view(form, UpdateMode, companyName, None, postCall, Some("srn"))(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    view(form, NormalMode, Some(insuranceCompanyName), None, postCall, None)(fakeRequest, messages)

  "InsurancePolicyNumber view" must {

    behave like normalPage(createView(Some(insuranceCompanyName)), messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__h1", insuranceCompanyName))

    behave like pageWithErrorOutsideLabel(createViewUsingForm, messageKeyPrefix,
      controllers.routes.InsurancePolicyNumberController.onSubmit(NormalMode, None).url,
      "policyNumber")

    behave like pageWithReturnLink(createView(Some(insuranceCompanyName)), getReturnLink)
  }

  "Insurance Policy Number view in change mode" must {

    behave like normalPage(createView(), messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"))

    behave like pageWithReturnLinkAndSrn(createUpdateView(Some(insuranceCompanyName)), getReturnLinkWithSrn)

  }
}