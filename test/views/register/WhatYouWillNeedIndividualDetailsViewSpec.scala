/*
 * Copyright 2022 HM Revenue & Customs
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

import controllers.register.establishers.individual.routes
import models.NormalMode
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import viewmodels.Message
import views.behaviours.ViewBehaviours
import views.html.register.whatYouWillNeedIndividualDetails

class WhatYouWillNeedIndividualDetailsViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "whatYouWillNeedIndividual"
  val establisherName = "Test Name"

  lazy val href: Call = routes.EstablisherNameController.onPageLoad(NormalMode, 0, None)
  val view: whatYouWillNeedIndividualDetails = app.injector.instanceOf[whatYouWillNeedIndividualDetails]
  def createView: () => HtmlFormat.Appendable = () =>
    view(Some("testScheme"), href, None, establisherName)(fakeRequest, messages)

  "WhatYouWillNeedIndividualDetailsView" must {

    behave like normalPageWithTitle(createView, messageKeyPrefix,
      messages("messages__detailsFor", messages("messages__theIndividual")).capitalize,
      Message(s"messages__detailsFor", establisherName),
      "_item1", "_item2", "_item3")

    behave like pageWithSubmitButton(createView)

    behave like pageWithReturnLink(createView, getReturnLink)
  }
}

