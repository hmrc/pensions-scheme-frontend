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

package views.racdac

import org.jsoup.Jsoup
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.racdac.schemeSuccess

class SchemeSuccessViewSpec extends ViewBehaviours {

  private val messageKeyPrefix = "complete_racdac"

  private val email = "email@email.com"
  private val schemeName = "schemeName"

  private val checkStatusLink = frontendAppConfig.managePensionsYourPensionSchemesUrl
  val view: schemeSuccess = app.injector.instanceOf[schemeSuccess]
  def createView: () => HtmlFormat.Appendable = () =>
    view(
      email,
      schemeName
    )(fakeRequest, messages)

  "SchemeSuccess view" must {

    behave like normalPageWithDynamicTitleAndHeader(createView, messageKeyPrefix, schemeName)

    "have dynamic text for email" in {
      Jsoup.parse(createView().toString()) must haveDynamicText("messages__complete_racdac__email", email)
    }

    "have link for check status" in {
      Jsoup.parse(createView().toString()).select("a[id=check-status-submission]") must haveLink(checkStatusLink)
    }

    "have a link to 'print this screen'" in {
      Jsoup.parse(createView().toString()) must haveLinkOnClick("window.print();return false;", "print-this-page-link")
    }
    behave like pageWithSubmitButton(createView)

  }

}
