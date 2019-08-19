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

package views.register.trustees.individual

import models.{Index, NormalMode}
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.register.trustees.individual.WhatYouWillNeedIndividualContactDetailsView

class WhatYouWillNeedIndividualContactDetailsViewSpec extends ViewBehaviours {


  lazy val href: Call = Call("GET", "url")

  val schemeName = Some("testScheme")

  def createView(): HtmlFormat.Appendable = WhatYouWillNeedIndividualContactDetailsView(frontendAppConfig, schemeName, href, None, "heading")(fakeRequest, messages)

  "whatYouWillNeedTrusteeIndividualDetails view" must {

    behave like normalPage(
      createView,
      "whatYouWillNeedTrusteeIndividualContact",
      "heading",
      "_item1",
      "_item2"
    )

    behave like pageWithSubmitButton(createView)

    behave like pageWithReturnLink(createView, getReturnLink)
  }
}

