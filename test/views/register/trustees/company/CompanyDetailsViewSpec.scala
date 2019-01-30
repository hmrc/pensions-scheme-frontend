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

package views.register.trustees.company

import controllers.register.trustees.company
import forms.CompanyDetailsFormProvider
import models.{CompanyDetails, Index, NormalMode}
import play.api.data.Form
import views.behaviours.QuestionViewBehaviours
import views.html.register.trustees.company.companyDetails

class CompanyDetailsViewSpec extends QuestionViewBehaviours[CompanyDetails] {

  val messageKeyPrefix = "common__company_details"

  override val form = new CompanyDetailsFormProvider()()
  val firstIndex = Index(1)

  private def createView() = () =>
    companyDetails(frontendAppConfig, form, NormalMode, firstIndex, None)(fakeRequest, messages)

  private def createViewUsingForm = (form: Form[_]) =>
    companyDetails(frontendAppConfig, form, NormalMode, firstIndex, None)(fakeRequest, messages)


  "CompanyDetails view" must {

    behave like normalPage(createView(), messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"))

    behave like pageWithReturnLink(createView(), getReturnLink)

    behave like pageWithTextFields(createViewUsingForm, messageKeyPrefix,
      company.routes.CompanyDetailsController.onSubmit(NormalMode, firstIndex).url, "companyName", "vatNumber", "payeNumber")
  }
}
