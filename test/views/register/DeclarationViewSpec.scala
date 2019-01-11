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

import forms.register.DeclarationFormProvider
import org.jsoup.Jsoup
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.QuestionViewBehaviours
import views.html.register.declaration

class DeclarationViewSpec extends QuestionViewBehaviours[Boolean] {
  private val messageKeyPrefix = "declaration"

  val schemeName = "Test Scheme Name"
  val form: Form[Boolean] = new DeclarationFormProvider()()

  def createView(hasWorkingKnowledge:Boolean = false): () => HtmlFormat.Appendable = () => declaration(frontendAppConfig,
    form, isCompany = true,
    isDormant = false,
    showMasterTrustDeclaration = true,
    hasWorkingKnowledge = hasWorkingKnowledge)(fakeRequest, messages)

  "Declaration view where no working knowledge" must {

    behave like normalPage(
      createView(),
      messageKeyPrefix,
      messages(s"messages__${messageKeyPrefix}__title"),
      "_statement8_no_working_knowledge",
      "_statement9")

    "have a return link" in {
      Jsoup.parse(createView()().toString).select("a[id=return-link]") must
        haveLink(controllers.register.routes.SchemeTaskListController.onPageLoad().url)
    }
  }

  "Declaration view where working knowledge" must {

    behave like normalPage(
      createView(hasWorkingKnowledge = true),
      messageKeyPrefix,
      messages(s"messages__${messageKeyPrefix}__title"),
      "_statement8_working_knowledge",
      "_statement9")
  }
}