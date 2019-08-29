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
import play.twirl.api.HtmlFormat
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.behaviours.ViewBehaviours
import views.html.register.trustees.partnership.whatYouWillNeedPartnershipContactDetails

class WhatYouWillNeedPartnershipContactDetailsViewSpec extends ViewBehaviours {

  private val messageKeyPrefix = "whatYouWillNeedTrusteePartnershipContact"
  private val index = 0
  private val pageHeader = Message("messages__whatYouWillNeedTrusteePartnershipContact__h1", "test partnership")

  def createView(): HtmlFormat.Appendable = whatYouWillNeedPartnershipContactDetails(
    frontendAppConfig,
    Some("testScheme"),
    CommonFormWithHintViewModel(
      postCall = controllers.register.trustees.partnership.routes.PartnershipEmailController.onSubmit(NormalMode, index, None),
      title = Message("messages__whatYouWillNeedTrusteePartnershipContact__title"),
      heading = pageHeader,
      srn = None
    ))(fakeRequest, messages)

  "whatYouWillNeedTrusteePartnershipContact view" must {

    behave like normalPage(createView, messageKeyPrefix, pageHeader,
      expectedGuidanceKeys = "_p1", "_item1", "_item2")

    behave like pageWithSubmitButton(createView)

    behave like pageWithReturnLink(createView, getReturnLink)
  }
}

