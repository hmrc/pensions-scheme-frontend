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

package views.register

import models.NormalMode
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.register.whatYouWillNeedIndividualAddress

class WhatYouWillNeedIndividualAddressViewSpec extends ViewBehaviours {

  private val messageKeyPrefix = "whatYouWillNeedAddress"
  private val testUser = "test name"

  private def href: Call = controllers.register.trustees.individual.routes.IndividualPostCodeLookupController.onPageLoad(NormalMode, index = 0, None)

  def createView: () => HtmlFormat.Appendable = () => whatYouWillNeedIndividualAddress(frontendAppConfig,
    Some("testScheme"), href, None, testUser)(fakeRequest, messages)

  "whatYouWillNeedCompanyAddress view" must {

    behave like normalPage(createView, messageKeyPrefix, messages("messages__addressFor", testUser))

    "display the correct p1" in {
      val doc = asDocument(createView())
      assertContainsText(doc, messages("messages__whatYouWillNeedAddress__p1", testUser))
    }

    "display the correct bullet points" in {
      val doc = asDocument(createView())
      assertContainsText(doc, messages("messages__whatYouWillNeedAddress__item1"))
      assertContainsText(doc, messages("messages__whatYouWillNeedAddress__item2", testUser))
    }

    behave like pageWithSubmitButton(createView, Some(href))

    behave like pageWithReturnLink(createView, getReturnLink)
  }
}

