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
import forms.register.establishers.company.CompanyPreviousAddressFormProvider
import models.NormalMode
import models.register.establishers.company.CompanyPreviousAddress
import views.behaviours.QuestionViewBehaviours
import views.html.register.establishers.company.companyPreviousAddress

class CompanyPreviousAddressViewSpec extends QuestionViewBehaviours[CompanyPreviousAddress] {

  val messageKeyPrefix = "companyPreviousAddress"

  override val form = new CompanyPreviousAddressFormProvider()()

  def createView = () => companyPreviousAddress(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  def createViewUsingForm = (form: Form[_]) => companyPreviousAddress(frontendAppConfig, form, NormalMode)(fakeRequest, messages)


  "CompanyPreviousAddress view" must {

    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"))

    behave like pageWithBackLink(createView)

    behave like pageWithTextFields(createViewUsingForm, messageKeyPrefix, controllers.register.establishers.company.routes.CompanyPreviousAddressController.onSubmit(NormalMode).url, "field1", "field2")
  }
}
