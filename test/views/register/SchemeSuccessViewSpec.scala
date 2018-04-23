/*
 * Copyright 2018 HM Revenue & Customs
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

import controllers.register.routes
import org.joda.time.LocalDate
import views.behaviours.ViewBehaviours
import org.jsoup.Jsoup
import play.twirl.api.HtmlFormat
import views.html.register.schemeSuccess

class SchemeSuccessViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "complete"

  val submissionReferenceNumber="XX123456789132"

  val testScheme = "test scheme name"

  def createView: () => HtmlFormat.Appendable = () => schemeSuccess(frontendAppConfig, Some(testScheme),
    LocalDate.now(), submissionReferenceNumber)(fakeRequest, messages)

  "SchemeSuccess view" must {

    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__heading", testScheme),
      "_copy_1", "_copy_2", "_copy_3", "_register_pensions_regulator", "_register_vat")

    "have dynamic text for application number" in {
      Jsoup.parse(createView().toString()) must haveDynamicText("messages__complete__application_number_is", submissionReferenceNumber)
    }

     "have link for complete register vat link" in {
      Jsoup.parse(createView().toString()).select("a[id=complete-register-vat-link]") must haveLink(routes.SchemeSuccessController.onPageLoad().url)
    }

    behave like pageWithSubmitButton(createView)

  }

}
