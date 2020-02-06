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

package views.register.trustees.company

import controllers.register.trustees.company
import forms.CompanyDetailsFormProvider
import models.{CompanyDetails, Index, NormalMode, UpdateMode}
import play.api.data.Form
import views.behaviours.QuestionViewBehaviours
import views.html.register.trustees.company.companyDetails

class CompanyDetailsViewSpec extends QuestionViewBehaviours[CompanyDetails] {

  val messageKeyPrefix = "companyName"

  override val form = new CompanyDetailsFormProvider()()
  val firstIndex = Index(1)
  val submitUrl = controllers.register.trustees.company.routes.CompanyDetailsController.onSubmit(NormalMode, firstIndex, None)

  val view: companyDetails = app.injector.instanceOf[companyDetails]

  private def createView() = () => view(
    form, NormalMode, firstIndex, None, submitUrl, None)(fakeRequest, messages)
  private def createUpdateView = () => view(
    form, UpdateMode, firstIndex, None, submitUrl, Some("srn"))(fakeRequest, messages)

  private def createViewUsingForm = (form: Form[_]) =>
    view(form, NormalMode, firstIndex, None, submitUrl, None)(fakeRequest, messages)


  "CompanyDetails view" must {

    behave like normalPage(createView(), messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"))

    behave like pageWithReturnLink(createView(), getReturnLink)

    behave like pageWithReturnLinkAndSrn(createUpdateView, getReturnLinkWithSrn)

    behave like pageWithErrorOutsideLabel(createViewUsingForm, messageKeyPrefix,
      company.routes.CompanyDetailsController.onSubmit(NormalMode, firstIndex, None).url, "companyName")
  }
}
