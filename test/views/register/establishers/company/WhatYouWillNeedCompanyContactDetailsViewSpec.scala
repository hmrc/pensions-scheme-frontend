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

package views.register.establishers.company

import models.{Index, NormalMode}
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.register.establishers.company.whatYouWillNeedCompanyContactDetails

class WhatYouWillNeedCompanyContactDetailsViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "whatYouWillNeedEstablisherCompanyContact"

  val href = controllers.register.establishers.company.routes.CompanyEmailController.onPageLoad(NormalMode, None, Index(0))

  def createView: () => HtmlFormat.Appendable = () => whatYouWillNeedCompanyContactDetails(frontendAppConfig, Some("testScheme"), href, None)(fakeRequest, messages)

  "whatYouWillNeedCompanyContactDetails view" must {

    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__h1"),
      "_lede", "_item1", "_item2")

    behave like pageWithSubmitButton(createView)

    behave like pageWithReturnLink(createView, getReturnLink)
  }
}

