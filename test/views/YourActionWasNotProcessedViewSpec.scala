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

package views

import base.SpecBase
import models.NormalMode
import org.jsoup.Jsoup
import views.behaviours.ViewBehaviours
import views.html.yourActionWasNotProcessed

class YourActionWasNotProcessedViewSpec extends ViewBehaviours with SpecBase {

  private val schemeName = "test scheme"
  private val messageKeyPrefix = "yourActionWasNotProcessed"
  private val view: yourActionWasNotProcessed = app.injector.instanceOf[yourActionWasNotProcessed]

  private def createView = () => view(Some(schemeName), NormalMode, None)(fakeRequest, messages)

  "YourActionWasNotProcessed view" must {
    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__heading"),
      expectedGuidanceKeys = "_p1", "_p2", "_p3")

    "have button to redirect to task list page" in {
      Jsoup.parse(createView().toString()).select("a[id=redirect-to-tasklist]") must
        haveLink(controllers.routes.PsaSchemeTaskListController.onPageLoad(NormalMode, None).url)
    }
  }
}
