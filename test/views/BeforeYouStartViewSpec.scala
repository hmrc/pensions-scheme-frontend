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

import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.beforeYouStart

class BeforeYouStartViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "before_you_start"
  private val psaName = "Psa Name"

  def createView: () => HtmlFormat.Appendable = () => beforeYouStart(frontendAppConfig, psaName)(fakeRequest, messages)

  "BeforeYouStart view" must {

    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"),
      "_p1", "_p2", "_item_1", "_item_2", "_item_3", "_item_4")

    behave like pageWithSubmitButton(createView)

    "have a link to the managing pension schemes" in {
      val doc = asDocument(createView())
      assertLink(doc, linkId = "return-link", url = frontendAppConfig.managePensionsSchemeOverviewUrl.url)
    }
  }
}
