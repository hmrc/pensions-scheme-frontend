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

import org.joda.time.LocalDate
import org.jsoup.Jsoup
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.register.schemeSuccess

class SchemeSuccessViewSpec extends ViewBehaviours {

  private val messageKeyPrefix = "complete"

  private val submissionReferenceNumber = "XX123456789132"
  private val email = "email@email.com"

  private val checkStatusLink = frontendAppConfig.managePensionsYourPensionSchemesUrl
  private val pensionsRegulatorLink = frontendAppConfig.pensionsRegulatorLink

  def createView: () => HtmlFormat.Appendable = () =>
    schemeSuccess(
      frontendAppConfig,
      LocalDate.now(),
      submissionReferenceNumber,
      showMasterTrustContent = false,
      email
    )(fakeRequest, messages)

  def createMasterTrustView: () => HtmlFormat.Appendable = () =>
    schemeSuccess(
      frontendAppConfig,
      LocalDate.now(),
      submissionReferenceNumber,
      showMasterTrustContent = true,
      email
    )(fakeRequest, messages)

  "SchemeSuccess view" must {

    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__heading"),
      "_copy_1", "_copy_3", "_copy_4", "_copy_5"
    )

    "have dynamic text for email" in {
      Jsoup.parse(createView().toString()) must haveDynamicText("messages__complete__email", email)
    }

    "have dynamic text for application number" in {
      Jsoup.parse(createView().toString()) must haveDynamicText("messages__complete__application_number_is", submissionReferenceNumber)
    }

    "have correct CSS class against hint text" in {
      val actual = asDocument(createView())
      actual.select("#hintText")
        .hasClass("panel panel-border-wide") mustBe true
    }

    "have link for check status" in {
      Jsoup.parse(createView().toString()).select("a[id=check-status-submission]") must haveLink(checkStatusLink)
    }

    "have a link to 'print this screen'" in {
      Jsoup.parse(createView().toString()) must haveLinkOnClick("window.print();return false;", "print-this-page-link")
    }

    behave like pageWithSubmitButton(createView)

  }

  "SchemeSuccess view when a master trust" must {

    behave like normalPage(createMasterTrustView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__heading"),
      "_pension_regulator_link", "_master_trust_heading"
    )

    "have link for pensions regulator" in {
      Jsoup.parse(createMasterTrustView().toString()).select("a[id=regulator-contact]") must haveLink(pensionsRegulatorLink)
    }

  }

}
