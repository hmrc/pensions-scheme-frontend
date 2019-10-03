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

import com.google.inject.Inject
import config.FrontendAppConfig
import forms.HasUTRFormProvider
import models.{Index, NormalMode}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.behaviours.YesNoViewBehaviours
import views.html.hasUtr

class HasUtrViewSpec@Inject()(appConfig: FrontendAppConfig) extends YesNoViewBehaviours {
  val schemeName = Some("Scheme x")
  val messageKeyPrefix = "hasCompanyUtr"
  val srn = Some("srn")

  val form = new HasUTRFormProvider()("messages__hasCompanyUtr__error__required", "ABC")
  val postCall = controllers.register.establishers.company.routes.HasCompanyUTRController.onSubmit(NormalMode, None, Index(0))

  private def viewModel(srn : Option[String] = None) = CommonFormWithHintViewModel(
    postCall,
    title = Message("messages__hasUTR", Message("messages__theCompany").resolve),
    heading = Message("messages__hasUTR", "ABC"),
    hint = Some(Message("messages__hasUtr__p1")),
    srn = srn
  )
  def createView(srn : Option[String] = None): () => HtmlFormat.Appendable = () =>
    hasUtr(frontendAppConfig, form, viewModel(srn), schemeName)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    hasUtr(frontendAppConfig, form, viewModel(), schemeName)(fakeRequest, messages)

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
