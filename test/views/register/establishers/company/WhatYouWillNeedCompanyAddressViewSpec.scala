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
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.register.establishers.company.whatYouWillNeedCompanyAddress

class WhatYouWillNeedCompanyAddressViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "whatYouWillNeedAddress"

  private def href: Call = controllers.register.establishers.company.routes.CompanyPostCodeLookupController.onPageLoad(NormalMode, None, Index(0))

  def createView: () => HtmlFormat.Appendable = () => whatYouWillNeedCompanyAddress(frontendAppConfig, Some("testScheme"), href, None)(fakeRequest, messages)

  "whatYouWillNeedCompanyAddress view" must {

    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__h1"))

    "display the correct paragraph" in {
      val doc = asDocument(createView())
      assertContainsText(doc, messages("messages__whatYouWillNeedEstablisherCompanyAddress__lede"))
    }

    "display the correct bullet points" in {
      val doc = asDocument(createView())
      assertContainsText(doc, messages("messages__whatYouWillNeedEstablisherCompanyAddress__item1"))
      assertContainsText(doc, messages("messages__whatYouWillNeedEstablisherCompanyAddress__item2"))
    }

    behave like pageWithSubmitButton(createView, Some(href))

    behave like pageWithReturnLink(createView, getReturnLink)
  }
}

