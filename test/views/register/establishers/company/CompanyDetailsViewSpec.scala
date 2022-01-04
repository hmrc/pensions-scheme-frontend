/*
 * Copyright 2022 HM Revenue & Customs
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

import controllers.register.establishers.company.routes
import forms.CompanyDetailsFormProvider
import models.{CompanyDetails, Index, NormalMode}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.QuestionViewBehaviours
import views.html.register.establishers.company.companyDetails

class CompanyDetailsViewSpec extends QuestionViewBehaviours[CompanyDetails] {

  val messageKeyPrefix = "companyName"

  override val form = new CompanyDetailsFormProvider()()
  val firstIndex = Index(1)
  private val postCall = routes.CompanyDetailsController.onSubmit _
  val view: companyDetails = app.injector.instanceOf[companyDetails]
  def createView(): () => HtmlFormat.Appendable = () =>
    view(form, NormalMode, firstIndex, None, postCall(NormalMode, None, firstIndex), None)(fakeRequest, messages)

  def createUpdateView(): () => HtmlFormat.Appendable = () =>
    view(form, NormalMode, firstIndex, None, postCall(NormalMode, None, firstIndex), Some("srn"))(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    view(form, NormalMode, firstIndex, None, postCall(NormalMode, None, firstIndex), None)(fakeRequest, messages)

  "CompanyDetails view" must {

    behave like normalPage(createView(), messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"))

    behave like pageWithErrorOutsideLabel(createViewUsingForm, messageKeyPrefix,
      routes.CompanyDetailsController.onSubmit(NormalMode, None, firstIndex).url, "companyName")

    behave like pageWithReturnLink(createView(), getReturnLink)

    behave like pageWithReturnLinkAndSrn(createUpdateView(), getReturnLinkWithSrn)
  }
}
