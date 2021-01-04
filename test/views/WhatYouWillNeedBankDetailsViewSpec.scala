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

import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.whatYouWillNeedBankDetails

class WhatYouWillNeedBankDetailsViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "what_you_will_need_bank_details"

  val view: whatYouWillNeedBankDetails = app.injector.instanceOf[whatYouWillNeedBankDetails]

  def createView: () => HtmlFormat.Appendable = () => view(None)(fakeRequest, messages)

  "WhatYouWillNeedBankDetails view" must {

    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__h1"),
      "_p1", "_p2", "_item_1", "_item_2")

    behave like pageWithSubmitButton(createView)

    behave like pageWithReturnLink(createView, getReturnLink)
  }
}

