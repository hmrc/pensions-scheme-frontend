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
import forms.register.establishers.company.CompanyDetailsFormProvider
import models.NormalMode
import models.CompanyDetails
import views.behaviours.QuestionViewBehaviours
import views.html.register.establishers.company.companyDetails
import models.Index
import play.twirl.api.HtmlFormat

class CompanyDetailsViewSpec extends QuestionViewBehaviours[CompanyDetails] {

  val messageKeyPrefix = "common__company_details"

  override val form = new CompanyDetailsFormProvider()()
  val firstIndex = Index(1)
  val schemeName = "test scheme name"

  def createView: () => HtmlFormat.Appendable = () => companyDetails(frontendAppConfig, form, NormalMode, firstIndex, schemeName)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    companyDetails(frontendAppConfig, form, NormalMode, firstIndex, schemeName)(fakeRequest, messages)


  "CompanyDetails view" must {

    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"))

    behave like pageWithTextFields(createViewUsingForm, messageKeyPrefix,
      routes.CompanyDetailsController.onSubmit(NormalMode, firstIndex).url, "companyName", "vatNumber", "payeNumber")
  }
}
