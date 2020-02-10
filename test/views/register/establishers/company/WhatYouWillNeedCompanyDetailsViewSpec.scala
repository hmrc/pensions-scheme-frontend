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

package views.register.establishers.company

import models.{Index, NormalMode}
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.register.establishers.company.whatYouWillNeedCompanyDetails

class WhatYouWillNeedCompanyDetailsViewSpec extends ViewBehaviours {

  private val messageKeyPrefix = "whatYouWillNeedEstablisherCompany"
  private val companyName = "hifi ltd"
  private val token = messages("messages__theCompany")

  private val href = controllers.register.establishers.company.routes.HasCompanyCRNController.onPageLoad(NormalMode, None, index=Index(0))

  val view: whatYouWillNeedCompanyDetails = app.injector.instanceOf[whatYouWillNeedCompanyDetails]

  private def createView: () => HtmlFormat.Appendable = () => view(
    Some("testScheme"), href, None, companyName)(fakeRequest, messages)

  "WhatYouWillNeedCompanyDetails view" must {

    behave like normalPageWithTitle(createView, messageKeyPrefix,
      messages("messages__company_detailsFor", token),
      messages("messages__company_detailsFor", companyName),
      "_item1", "_item2", "_item3")

    "display the correct dynamic lede, item4, p1 and p2" in {
      val doc = asDocument(createView())
      assertContainsText(doc, messages("messages__whatYouWillNeedEstablisherCompany__lede", companyName))
      assertContainsText(doc, messages("messages__whatYouWillNeedEstablisherCompany__item4", companyName))
      assertContainsText(doc, messages("messages__whatYouWillNeedEstablisherCompany__p1", companyName))
      assertContainsText(doc, messages("messages__whatYouWillNeedEstablisherCompany__p2", companyName))
    }

    behave like pageWithSubmitButton(createView)

    behave like pageWithReturnLink(createView, getReturnLink)
  }
}

