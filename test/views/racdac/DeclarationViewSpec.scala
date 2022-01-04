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

package views.racdac

import org.jsoup.Jsoup
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.racdac.declaration

class DeclarationViewSpec extends ViewBehaviours {
  private val messageKeyPrefix = "declaration_racdac"
  private val href = controllers.register.routes.DeclarationController.onClickAgree
  private val view: declaration = app.injector.instanceOf[declaration]
  private val returnLink = frontendAppConfig.managePensionsSchemeOverviewUrl.url
  private def createView(hasWorkingKnowledge:Boolean = false): () => HtmlFormat.Appendable = {
    () => view("PSA", href)(fakeRequest, messages)

  }

  "Declaration view where no working knowledge" must {

    behave like normalPage(
      createView(),
      messageKeyPrefix,
      messages(s"messages__${messageKeyPrefix}__title"),
      "_statement1",
      "_statement2",
      "_statement3"
    )

    "have a return link" in {
      Jsoup.parse(createView()().toString).select("a[id=return-link]") must
        haveLink(returnLink)
    }
  }

  "Declaration view where working knowledge" must {

    behave like normalPage(
      createView(hasWorkingKnowledge = true),
      messageKeyPrefix,
      messages(s"messages__${messageKeyPrefix}__title"),
      "_statement1",
      "_statement2",
      "_statement3")
  }
}
