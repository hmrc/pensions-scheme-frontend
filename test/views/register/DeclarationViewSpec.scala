/*
 * Copyright 2021 HM Revenue & Customs
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

import org.jsoup.Jsoup
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.register.declaration

class DeclarationViewSpec extends ViewBehaviours {
  private val messageKeyPrefix = "declaration"

  val schemeName = "Test Scheme Name"
  private val href = controllers.register.routes.DeclarationController.onClickAgree
  val view: declaration = app.injector.instanceOf[declaration]
  def createView(hasWorkingKnowledge:Boolean = false): () => HtmlFormat.Appendable =
    () => view(isCompany = true,
    isDormant = false,
    showMasterTrustDeclaration = true,
    hasWorkingKnowledge = hasWorkingKnowledge, None, href)(fakeRequest, messages)

  "Declaration view where no working knowledge" must {

    behave like normalPage(
      createView(),
      messageKeyPrefix,
      messages(s"messages__${messageKeyPrefix}__title"),
      "_statement8_no_working_knowledge",
      "_statement9")

    "have a return link" in {
      Jsoup.parse(createView()().toString).select("a[id=return-link]") must
        haveLink(getReturnLink)
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