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

package views

import controllers.routes
import forms.AnyMoreChangesFormProvider
import org.jsoup.Jsoup
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewmodels.Message
import views.behaviours.YesNoViewBehaviours
import views.html.anyMoreChanges

class AnyMoreChangesViewSpec extends YesNoViewBehaviours {
  val schemeName = Some("Scheme x")
  val messageKeyPrefix = "any_more_changes"
  val date: String = "27 February 2019"
  val srn = Some("srn")

  val form = new AnyMoreChangesFormProvider()()
  private val postCall = controllers.routes.AnyMoreChangesController.onSubmit(srn)

  val view: anyMoreChanges = app.injector.instanceOf[anyMoreChanges]

  def createView: () => HtmlFormat.Appendable = () =>
    view(form, schemeName, date, postCall, srn)(fakeRequest, messages)

  def createUpdateView: () => HtmlFormat.Appendable = () =>
    view(form, schemeName, date, postCall, srn)(fakeRequest, messages)

  def createViewInCheckMode: () => HtmlFormat.Appendable = () =>
    view(form, schemeName, date, postCall, srn)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    view(form, schemeName, date, postCall, srn)(fakeRequest, messages)

  "Any More Changes view" must {

    behave like normalPage(createView, messageKeyPrefix, pageHeader = messages(s"messages__${messageKeyPrefix}__title"),
      expectedGuidanceKeys = "_p1", "_p2")

    "display partnership name" in {
      Jsoup.parse(createView().toString) must haveDynamicText(Message("messages__any_more_changes__p3_date", date))
    }
    behave like yesNoPage(
      createView = createViewUsingForm,
      messageKeyPrefix = messageKeyPrefix,
      expectedFormAction = routes.AnyMoreChangesController.onSubmit(Some("123")).url
    )

    behave like pageWithSubmitButton(createView)

    behave like pageWithReturnLinkAndSrn(createUpdateView, getReturnLinkWithSrn)

  }
}
