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

package views

import config.FrontendAppConfig
import play.api.inject.guice.GuiceApplicationBuilder
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.whatYouWillNeed

class WhatYouWillNeedViewSpec extends ViewBehaviours {

  override def frontendAppConfig: FrontendAppConfig = new GuiceApplicationBuilder().configure(
    conf = "features.is-hub-enabled" -> false
  ).build().injector.instanceOf[FrontendAppConfig]

  val messageKeyPrefix = "what_you_will_need"

  def createView: () => HtmlFormat.Appendable = () => whatYouWillNeed(frontendAppConfig)(fakeRequest, messages)

  "WhatYouWillNeed view" must {

    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"),
      "_lede", "_item_1", "_item_2", "_item_3", "_item_4")

    behave like pageWithSubmitButton(createView)
  }
}

class WhatYouWillNeedHsViewSpec extends ViewBehaviours {

  override def frontendAppConfig: FrontendAppConfig = new GuiceApplicationBuilder().configure(
    conf = "features.is-hub-enabled" -> true
  ).build().injector.instanceOf[FrontendAppConfig]

  val messageKeyPrefix = "hs_what_you_will_need"

  def createView: () => HtmlFormat.Appendable = () => whatYouWillNeed(frontendAppConfig)(fakeRequest, messages)

  "WhatYouWillNeed view (hub and spoke version)" must {

    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"),
      "_lede", "_p2", "_item_1", "_item_2", "_item_3", "_item_4", "_p3")

    behave like pageWithSubmitButton(createView)

    "have a link to the register a scheme page on gov uk" in {
      val doc = asDocument(createView())
      assertLink(doc, linkId = "apply-to-register-govuk-link", url = frontendAppConfig.applyToRegisterLink)
    }

    "have a link to the managing pension schemes" in {
      val doc = asDocument(createView())
      assertLink(doc, linkId = "return-managing", url = frontendAppConfig.managePensionsSchemeOverviewUrl.url)
    }
  }
}
