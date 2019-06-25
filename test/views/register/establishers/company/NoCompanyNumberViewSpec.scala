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

package views.register.establishers.company

import forms.register.establishers.company.NoCompanyNumberFormProvider
import models.{Index, NormalMode}
import play.api.data.Form
import viewmodels.{Message, NoCompanyNumberViewModel}
import views.behaviours.ViewBehaviours
import views.html.register.establishers.company.noCompanyNumber

class NoCompanyNumberViewSpec extends ViewBehaviours {

  val name = "test name"
  val messageKeyPrefix = "noCompanyNumber__establisher"
  val index = Index(0)
  val form = new NoCompanyNumberFormProvider()(name)
  val submitUrl = controllers.register.establishers.company.routes.NoCompanyNumberController.onSubmit(NormalMode, None, index)

  def viewModel(name: String = name): NoCompanyNumberViewModel = {
    NoCompanyNumberViewModel(
      title = Message(s"messages__${messageKeyPrefix}__title"),
      heading = Message(s"messages__${messageKeyPrefix}__heading", name)
    )
  }

  private def createView() = () => noCompanyNumber(
    frontendAppConfig, viewModel(), form, None, submitUrl, None)(fakeRequest, messages)
  private def createUpdateView = () => noCompanyNumber(
    frontendAppConfig, viewModel(), form, None, submitUrl, Some("srn"))(fakeRequest, messages)

  private def createViewUsingForm = (form: Form[_]) =>
    noCompanyNumber(frontendAppConfig, viewModel(), form, None, submitUrl, None)(fakeRequest, messages)

  "CompanyRegistrationNumberVariations view" should {
    behave like normalPage(createView(), messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__heading", name))

    behave like pageWithReturnLink(createView(), getReturnLink)

    behave like pageWithReturnLinkAndSrn(createUpdateView, getReturnLinkWithSrn)

    "display an input text box" in {
      val expectedValue = "12345678"
      val doc = asDocument(createViewUsingForm(form.bind(Map("reason" -> expectedValue))))
      doc must haveLabelAndValue("reason", s"${messages("messages__common__reason")}", expectedValue)
    }

  }
}