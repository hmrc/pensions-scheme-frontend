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
import controllers.register.establishers.company.routes
import forms.register.establishers.company.CompanyPostCodeLookupFormProvider
import models.NormalMode
import views.behaviours.StringViewBehaviours
import views.html.register.establishers.company.companyPostCodeLookup

class CompanyPostCodeLookupViewSpec extends StringViewBehaviours {

  val messageKeyPrefix = "companyPostCodeLookup"

  val form = new CompanyPostCodeLookupFormProvider()()

  def createView: () => _root_.play.twirl.api.HtmlFormat.Appendable = () => companyPostCodeLookup(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  def createViewUsingForm: (Form[String]) => _root_.play.twirl.api.HtmlFormat.Appendable = (form: Form[String]) =>
    companyPostCodeLookup(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  "CompanyPostCodeLookup view" must {
    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__heading"))

    behave like pageWithBackLink(createView)

    behave like stringPage(
      createViewUsingForm, messageKeyPrefix, controllers.register.establishers.company.routes.CompanyPostCodeLookupController.onSubmit(NormalMode).url)
  }
}
