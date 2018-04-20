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

package views.register

import forms.register.DeclarationFormProvider
import org.jsoup.Jsoup
import play.api.data.Form
import views.behaviours.QuestionViewBehaviours
import views.html.register.declaration

class DeclarationViewSpec extends QuestionViewBehaviours[Boolean] {

  val messageKeyPrefix = "declaration"
  val schemeName = "Test Scheme Name"
  val form: Form[Boolean] = new DeclarationFormProvider()()

  def createView = () => declaration(frontendAppConfig, form, schemeName, true, false)(fakeRequest, messages)
  def createViewDynamic(isCompany: Boolean = true, isDormant: Boolean = false) = () => declaration(frontendAppConfig, form, schemeName, isCompany, isDormant)(fakeRequest, messages)
  def createViewUsingForm = (form: Form[_]) => declaration(frontendAppConfig, form, schemeName, false, false)(fakeRequest, messages)

  "Declaration view" must {
    behave like normalPage(
      createView,
      messageKeyPrefix,
      messages(s"messages__${messageKeyPrefix}__title"),
        "_declare",
        "_statement1_not_dormant",
        "_statement2",
        "_statement3",
        "_statement4",
        "_statement5",
        "_statement6",
        "_statement7")

    behave like pageWithSecondaryHeader(createView, schemeName)

    "not display statement one for individual journey" in {
      Jsoup.parse(createViewDynamic(false, false).toString) mustNot haveDynamicText(s"messages__${messageKeyPrefix}__statement1_not_dormant")
      Jsoup.parse(createViewDynamic(false, false).toString) mustNot haveDynamicText(s"messages__${messageKeyPrefix}__statement1_dormant")
    }

    "show an error summary when rendered with an error" in {
      val doc = asDocument(createViewUsingForm(form.withError(error)))
      assertRenderedById(doc, "error-summary-heading")
    }

    "have an I Agree checkbox" in {
      Jsoup.parse(createView().toString) must haveCheckBox("agree", "agreed")
    }

    "have a label for the I Agree checkbox" in {
      Jsoup.parse(createView().toString) must haveLabelAndValue("agree", messages("messages__declaration__agree"), "agreed")
    }

    behave like pageWithSubmitButton(createView)

    "have a cancel link" in {
      Jsoup.parse(createView().toString).select("a[id=cancel]") must haveLink(controllers.routes.IndexController.onPageLoad().url)
    }
  }

  "Declaration view for company journey with dormant members" must {
    behave like normalPage(
      createViewDynamic(true, true),
      messageKeyPrefix,
      messages(s"messages__${messageKeyPrefix}__title"),
      "_declare",
      "_statement1_dormant")
  }
}
