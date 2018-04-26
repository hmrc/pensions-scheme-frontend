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

import controllers.register.establishers.{routes => routes1}
import controllers.register.routes
import controllers.register.trustees.{routes => routes2}
import models.CheckMode
import org.jsoup.Jsoup
import views.behaviours.ViewBehaviours
import views.html.register.schemeReview

class SchemeReviewViewSpec extends ViewBehaviours {

  private val messageKeyPrefix = "schemeReview"

  private val schemeName = "Test Scheme Name"
  private val establishers = Seq("establisher name", "establisher company name")
  private val trustees = Seq("trustee name", "trustee company name")

  private def createView = () => schemeReview(frontendAppConfig, schemeName, establishers, trustees)(fakeRequest, messages)

  "SchemeReview view" must {
    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__heading"))

    behave like pageWithBackLink(createView)

    behave like pageWithSecondaryHeader(createView, schemeName)

    "display scheme name" in {
      Jsoup.parse(createView().toString) must haveDynamicText(schemeName)
    }

    "have link to edit scheme details" in {
      Jsoup.parse(createView().toString).select("a[id=edit-scheme-details]") must haveLink(
        routes.CheckYourAnswersController.onPageLoad().url
      )
    }

    "display header for establishers section" in {
      Jsoup.parse(createView().toString) must haveDynamicText(messages("messages__schemeReview__establishers__heading"))
    }

    "have link to edit establishers details" in {
      Jsoup.parse(createView().toString).select("a[id=edit-establishers]") must haveLink(
        routes1.AddEstablisherController.onPageLoad(CheckMode).url
      )
    }

    "contain list of establishers" in {
      for(establisher <- establishers)
        Jsoup.parse(createView().toString) must haveDynamicText(establisher)
    }

    "display header for trustees section" in {
      Jsoup.parse(createView().toString) must haveDynamicText(messages("messages__schemeReview__trustees__heading"))
    }

    "have link to edit trustees details" in {
      Jsoup.parse(createView().toString).select("a[id=edit-trustees]") must haveLink(
        routes2.AddTrusteeController.onPageLoad(CheckMode).url
      )
    }

    "contain list of trustees" in {
      for(trustees <- trustees)
        Jsoup.parse(createView().toString) must haveDynamicText(trustees)
    }
  }

}
