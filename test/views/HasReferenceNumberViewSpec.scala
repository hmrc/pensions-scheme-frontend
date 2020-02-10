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

package views

import forms.mappings.Mappings
import javax.inject.Inject
import models.{Index, NormalMode}
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.behaviours.YesNoViewBehaviours
import views.html.hasReferenceNumber

class HasReferenceNumberViewSpec extends YesNoViewBehaviours {
  val schemeName = Some("Scheme x")
  val messageKeyPrefix = "hasX"
  val srn = None
  val index = Index(0)

  def viewModel(srn : Option[String] = None) = CommonFormWithHintViewModel(
    Call("GET","url"),
    title = Message("messages__hasX__title"),
    heading = Message("messages__hasX__heading"),
    hint = Some(Message("messages__hasX__hint")),
    srn = srn
  )

  class HasXFormProvider @Inject() extends Mappings {

    def apply(errorKey : String, name : String)(implicit messages: Messages): Form[Boolean] =
      Form(
        "value" -> boolean(Message(errorKey, name).resolve)
      )
  }

  val form = new HasXFormProvider()("required", "name")
  private val postCall = controllers.register.establishers.company.routes.HasCompanyCRNController.onSubmit(NormalMode, srn, index)

  val view: hasReferenceNumber = app.injector.instanceOf[hasReferenceNumber]

  def createView(srn : Option[String] = None): () => HtmlFormat.Appendable = () =>
    view(form, viewModel(srn), schemeName)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    view(form, viewModel(), schemeName)(fakeRequest, messages)

  "HasReferenceNumber view" must {

    behave like normalPage(createView(), messageKeyPrefix, pageHeader = messages("messages__hasX__heading"))

    behave like yesNoPage(
      createView = createViewUsingForm,
      messageKeyPrefix = messageKeyPrefix,
      expectedFormAction = postCall.url,
      legendKey = "_heading"
    )

    behave like pageWithSubmitButton(createView())

    behave like pageWithReturnLinkAndSrn(createView(Some("srn")), getReturnLinkWithSrn)

  }
}
