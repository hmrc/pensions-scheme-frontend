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

import controllers.register.routes
import views.behaviours.ViewBehaviours
import org.jsoup.Jsoup
import views.html.register.whatYouWillNeed

class WhatYouWillNeedViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "what_you_will_need"

  def createView = () => whatYouWillNeed(frontendAppConfig)(fakeRequest, messages)

  "WhatYouWillNeed view" must {

    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title") ,
      "_lede", "_item_1", "_item_2", "_item_3", "_item_4", "_item_5")

    "have link" in {
      Jsoup.parse(createView().toString()).select("a.button") must haveLink(routes.WhatYouWillNeedController.onPageLoad().url)
    }
  }
}