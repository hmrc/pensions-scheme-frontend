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

package views.register

import forms.CompanyRegistrationNumberFormProvider
import models.{Index, NormalMode, UpdateMode}
import play.api.data.Form
import viewmodels.{CompanyRegistrationNumberViewModel, Message}
import views.behaviours.ViewBehaviours
import views.html.register.companyRegistrationNumberVariations

class CompanyRegistrationNumberVariationsViewSpec extends ViewBehaviours {

  val name = "test name"
  val messageKeyPrefix = "companyNumber__trustee"
  val index = Index(0)
  val form = new CompanyRegistrationNumberFormProvider()(name)
  val submitUrl = controllers.register.trustees.company.routes.CompanyEmailController.onSubmit(NormalMode, index, None)

  def viewModel(name: String = name): CompanyRegistrationNumberViewModel = {
    CompanyRegistrationNumberViewModel(
      title = Message(s"messages__${messageKeyPrefix}__title"),
      heading = Message(s"messages__${messageKeyPrefix}__heading", name),
      hint = Message("messages__common__crn_hint", name)
    )
  }
  private def createView() = () => companyRegistrationNumberVariations(
    frontendAppConfig, viewModel(), form, None, submitUrl, None)(fakeRequest, messages)
  private def createUpdateView = () => companyRegistrationNumberVariations(
    frontendAppConfig, viewModel(), form, None, submitUrl, Some("srn"))(fakeRequest, messages)

  private def createViewUsingForm = (form: Form[_]) =>
    companyRegistrationNumberVariations(frontendAppConfig, viewModel(), form, None, submitUrl, None)(fakeRequest, messages)

  "CompanyRegistrationNumberVariations view" should {
    behave like normalPage(createView(), messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__heading", name))

    behave like pageWithReturnLink(createView(), getReturnLink)

    behave like pageWithReturnLinkAndSrn(createUpdateView, getReturnLinkWithSrn)

    "Generate correct hint text" in {
      val doc = asDocument(createView()())
      assertContainsText(doc, messages("messages__common__crn_hint"))
    }

    "display an input text box" in {
      val expectedValue = "12345678"
      val doc = asDocument(createViewUsingForm(form.bind(Map("companyRegistrationNumber" -> expectedValue))))
      doc must haveLabelAndValue("companyRegistrationNumber", s"${messages("messages__common__crn")}", expectedValue)
    }

  }
}