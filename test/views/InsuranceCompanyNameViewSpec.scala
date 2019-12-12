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

import forms.InsuranceCompanyNameFormProvider
import models.{NormalMode, UpdateMode}
import play.api.data.Form
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import views.behaviours.QuestionViewBehaviours
import views.html.insuranceCompanyName

class InsuranceCompanyNameViewSpec extends QuestionViewBehaviours[String] {

  val messageKeyPrefix = "insurance_company_name"

  override val form = new InsuranceCompanyNameFormProvider()()
  val postCall: Call = controllers.routes.InsuranceCompanyNameController.onSubmit(NormalMode, None)

  def createView: () => HtmlFormat.Appendable = () => insuranceCompanyName(
    frontendAppConfig, form, NormalMode, None, postCall, None)(fakeRequest, messages)

  def createUpdateView: () => HtmlFormat.Appendable = () => insuranceCompanyName(
    frontendAppConfig, form, UpdateMode, None, postCall, Some("srn"))(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    insuranceCompanyName(frontendAppConfig, form, NormalMode, None, postCall, None)(fakeRequest, messages)

  "InsuranceCompanyName view" must {

    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__h1"))

    behave like pageWithErrorOutsideLabel(createViewUsingForm, messageKeyPrefix, controllers.routes.InsuranceCompanyNameController.onSubmit(NormalMode, None).url,
      "companyName")

    behave like pageWithReturnLink(createView, getReturnLink)

    behave like pageWithReturnLinkAndSrn(createUpdateView, getReturnLinkWithSrn)
  }
}