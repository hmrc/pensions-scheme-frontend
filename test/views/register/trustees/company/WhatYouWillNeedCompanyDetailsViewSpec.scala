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

package views.register.trustees.company

import models.{Index, NormalMode}
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.register.trustees.company.whatYouWillNeedCompanyDetails

class WhatYouWillNeedCompanyDetailsViewSpec extends ViewBehaviours {
  val pageHeading = "page heading"

  lazy val href = controllers.register.trustees.company.routes.HasCompanyNumberController.onSubmit(NormalMode, Index(0), None)

  def createView(): HtmlFormat.Appendable = whatYouWillNeedCompanyDetails(
    frontendAppConfig,
    Some("testScheme"),
    href,
    None,
    pageHeading
  )(fakeRequest, messages)

  "WhatYouWillNeedCompanyDetails view" must {

    behave like normalPage(
      createView,
      "whatYouWillNeedTrusteeCompany",
      pageHeading,
      "_p1", "_item1", "_item2", "_item3", "_item4", "_p2")

    behave like pageWithSubmitButton(createView)

    behave like pageWithReturnLink(createView, getReturnLink)
  }
}

