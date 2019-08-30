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

package views.register.trustees.partnership

import models.NormalMode
import org.jsoup.Jsoup
import play.twirl.api.HtmlFormat
import viewmodels.Message
import views.behaviours.ViewBehaviours
import views.html.register.trustees.partnership.whatYouWillNeedPartnershipDetails

class WhatYouWillNeedPartnershipDetailsViewSpec extends ViewBehaviours {

  private val messageKeyPrefix = "whatYouWillNeedTrusteePartnershipDetails"
  private val index = 0
  private val partnershipName = "test partnership"
  private val nextPageUrl = controllers.register.trustees.partnership.routes.PartnershipHasUTRController.onPageLoad(NormalMode, index, None)
  private val pageHeader = Message("messages__whatYouWillNeedTrusteePartnershipDetails__h1", partnershipName)

  def createView(): HtmlFormat.Appendable = whatYouWillNeedPartnershipDetails(
    frontendAppConfig,
    Some("testScheme"),
    nextPageUrl,
    partnershipName,
    None
    )(fakeRequest, messages)

  "whatYouWillNeedTrusteePartnershipDetails view" must {

    behave like normalPage(createView, messageKeyPrefix, pageHeader,
      expectedGuidanceKeys = "_p1", "_item1", "_item2", "_item3")

    "display the paragraph" in {
      Jsoup.parse(createView().toString()) must haveDynamicText(s"messages__${messageKeyPrefix}__p2", partnershipName)
    }

    behave like pageWithSubmitButton(createView,
      action = Some(nextPageUrl))

    behave like pageWithReturnLink(createView, getReturnLink)
  }
}

