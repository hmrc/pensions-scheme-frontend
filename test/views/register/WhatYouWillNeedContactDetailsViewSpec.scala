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

import play.twirl.api.HtmlFormat
import viewmodels.Message
import views.behaviours.ViewBehaviours
import views.html.register.whatYouWillNeedContactDetails

class WhatYouWillNeedContactDetailsViewSpec extends ViewBehaviours {

  private val messageKeyPrefix = "whatYouWillNeedContact"
  private val entityName = "Test Entity"
  private val heading = Message("messages__contactDetailsFor", entityName)
  private val title = Message("messages__contactDetailsFor", Message("messages__theIndividual").resolve.capitalize)
  private val href = controllers.routes.IndexController.onPageLoad()
  val view: whatYouWillNeedContactDetails = app.injector.instanceOf[whatYouWillNeedContactDetails]
  private def createView(): HtmlFormat.Appendable =
    view(Some("testScheme"), href, None, entityName, Message("messages__theIndividual"))(fakeRequest, messages)

  "WhatYouWillNeedContactDetailsView" must {

    behave like normalPageWithTitle(createView, messageKeyPrefix, title, heading,
      expectedGuidanceKeys = "_item1", "_item2")

    "display the correct p1" in {
      val doc = asDocument(createView())
      assertContainsText(doc, messages("messages__whatYouWillNeedContact__p1", entityName))
    }

    behave like pageWithSubmitButton(createView)

    behave like pageWithReturnLink(createView, getReturnLink)
  }
}

