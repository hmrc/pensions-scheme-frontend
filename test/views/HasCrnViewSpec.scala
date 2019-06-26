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

import forms.HasCrnFormProvider
import models.{Index, NormalMode}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewmodels.{HasCrnViewModel, Message}
import views.behaviours.YesNoViewBehaviours
import views.html.hasCrn

class HasCrnViewSpec extends YesNoViewBehaviours {
  val schemeName = Some("Scheme x")
  val messageKeyPrefix = "hasCompanyNumber"
  val srn = None
  val index = Index(0)

  def viewModel(srn : Option[String] = None) = HasCrnViewModel(
    controllers.register.establishers.company.routes.HasCompanyNumberController.onSubmit(NormalMode, srn, index),
    title = Message("messages__hasCompanyNumber__title"),
    heading = Message("messages__hasCompanyNumber__h1", "ABC"),
    hint = Message("messages__hasCompanyNumber__p1"),
    srn = srn
  )

  val form = new HasCrnFormProvider()("messages__hasCompanyNumber__error__required", "ABC")
  private val postCall = controllers.register.establishers.company.routes.HasCompanyNumberController.onSubmit(NormalMode, srn, index)

  def createView(srn : Option[String] = None): () => HtmlFormat.Appendable = () =>
    hasCrn(frontendAppConfig, form, viewModel(srn), schemeName)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    hasCrn(frontendAppConfig, form, viewModel(), schemeName)(fakeRequest, messages)

  "HasCompanyNumber view" must {

    behave like normalPage(createView(), messageKeyPrefix, pageHeader = messages(s"messages__${messageKeyPrefix}__h1", "ABC"),
      expectedGuidanceKeys = "_p1")

    behave like yesNoPage(
      createView = createViewUsingForm,
      messageKeyPrefix = messageKeyPrefix,
      expectedFormAction = postCall.url
    )

    behave like pageWithSubmitButton(createView())

    behave like pageWithReturnLinkAndSrn(createView(Some("srn")), getReturnLinkWithSrn)

  }
}
