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

package views.register.trustees.company

import play.api.data.Form
import controllers.register.trustees.company.routes
import forms.ContactDetailsFormProvider
import models.register.ContactDetails
import models.{Index, NormalMode}
import models.register.ContactDetails
import views.behaviours.QuestionViewBehaviours
import views.html.register.trustees.company.companyContactDetails
import play.twirl.api.HtmlFormat

class CompanyContactDetailsViewSpec extends QuestionViewBehaviours[ContactDetails] {

  val messageKeyPrefix = "establisher_company_contact_details"
  val index = Index(1)
  val companyName = "test company name"


  override val form = new ContactDetailsFormProvider()()

  def createView: () => HtmlFormat.Appendable = () => companyContactDetails(frontendAppConfig, form, NormalMode, index, companyName)(fakeRequest, messages)

  def createViewUsingForm: Form[_] =>HtmlFormat.Appendable = (form: Form[_]) => companyContactDetails(frontendAppConfig,
    form, NormalMode, index, companyName)(fakeRequest, messages)


  "CompanyContactDetails view" must {

    behave like normalPage(createView, messageKeyPrefix, messages("messages__trustee_company_contact_details__title"))

    behave like pageWithBackLink(createView)

    behave like pageWithTextFields(createViewUsingForm, messageKeyPrefix,
      controllers.register.trustees.company.routes.CompanyContactDetailsController.onSubmit(NormalMode, index).url, "emailAddress", "phoneNumber")
  }
}
