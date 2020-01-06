/*
 * Copyright 2020 HM Revenue & Customs
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

import models.NormalMode
import org.jsoup.Jsoup
import play.twirl.api.HtmlFormat
import viewmodels.Message
import views.behaviours.ViewBehaviours
import views.html.register.whatYouWillNeedPartnershipDetails

class WhatYouWillNeedPartnershipDetailsViewSpec extends ViewBehaviours {

  private val messageKeyPrefix = "whatYouWillNeedPartnershipDetails"
  private val index = 0
  private val partnershipName = "test partnership"
  private val nextPageUrl = controllers.register.trustees.partnership.routes.PartnershipHasUTRController.onPageLoad(NormalMode, index, None)
  private val pageHeader = Message("messages__detailsFor", partnershipName)
  private val title = Message("messages__detailsFor", Message("messages__thePartnership").resolve.capitalize)

  def createView(): HtmlFormat.Appendable = whatYouWillNeedPartnershipDetails(
    frontendAppConfig,
    Some("testScheme"),
    nextPageUrl,
    partnershipName,
    None
  )(fakeRequest, messages)

  "whatYouWillNeedTrusteePartnershipDetails view" must {

    behave like normalPageWithTitle(createView, messageKeyPrefix, title, pageHeader,
      expectedGuidanceKeys = "_item1", "_item2")

    "display the dynamic paragraph and bullet points" in {
      val doc = Jsoup.parse(createView().toString())
      doc must haveDynamicText(s"messages__${messageKeyPrefix}__p1", partnershipName)
      doc must haveDynamicText(s"messages__${messageKeyPrefix}__p2", partnershipName)
      doc must haveDynamicText(s"messages__${messageKeyPrefix}__item3", partnershipName)
    }

    behave like pageWithSubmitButton(createView,
      action = Some(nextPageUrl))

    behave like pageWithReturnLink(createView, getReturnLink)
  }
}

