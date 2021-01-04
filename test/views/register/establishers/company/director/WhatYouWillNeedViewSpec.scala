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

package views.register.establishers.company.director

import controllers.register.establishers.company.director.routes.DirectorNameController
import models.NormalMode
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import viewmodels.Message
import views.behaviours.ViewBehaviours
import views.html.register.establishers.company.director.whatYouWillNeed

class WhatYouWillNeedViewSpec extends ViewBehaviours {

  private val messageKeyPrefix = "whatYouWillNeed"

  private val href: Call = DirectorNameController.onPageLoad(NormalMode, establisherIndex = 0, directorIndex = 0, None)

  val view: whatYouWillNeed = app.injector.instanceOf[whatYouWillNeed]

  private def createView: () => HtmlFormat.Appendable =
    () => view(Some("testScheme"), None, href)(fakeRequest, messages)

  private val messageKeys = (1 to 8).map(num => s"_item$num").toList

  "WhatYouWillNeedCompanyDetails view" must {

    behave like normalPageWithTitle(createView, messageKeyPrefix,
      Message("messages__directors__heading"),
      Message("messages__directors__heading"))

    "display the correct guidance" in {
      val doc = asDocument(createView())
      for (key <- messageKeys) assertContainsText(doc, messages(s"messages__${messageKeyPrefix}_$key"))
    }

    "display the correct paragraph" in {
      val doc = asDocument(createView())
      assertContainsText(doc, messages("messages__whatYouWillNeedDirectors__p1"))
    }

    behave like pageWithSubmitButton(createView)

    behave like pageWithReturnLink(createView, getReturnLink)
  }
}

