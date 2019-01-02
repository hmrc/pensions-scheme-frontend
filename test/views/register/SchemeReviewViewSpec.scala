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

import controllers.register.establishers.individual.{routes => routes1}
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
  private val tenTrustees = Seq("trustee one", "trustee two", "trustee three", "trustee four", "trustee five",
    "trustee six", "trustee seven", "trustee eight", "trustee nine", "trustee ten")
  private val estIndvUrl = routes1.CheckYourAnswersController.onPageLoad(0)
  private val trusteeAddUrl = routes2.AddTrusteeController.onPageLoad(CheckMode)

  private def createView = () => schemeReview(frontendAppConfig, schemeName, establishers, trustees, estIndvUrl, trusteeAddUrl)(fakeRequest, messages)

  private def createSecView = () => schemeReview(frontendAppConfig, schemeName, establishers, tenTrustees, estIndvUrl, trusteeAddUrl)(fakeRequest, messages)

  "SchemeReview view" must {
    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__heading"))

    behave like pageWithBackLink(createView)

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
        estIndvUrl.url
      )
    }

    "contain list of establishers" in {
      for (establisher <- establishers)
        Jsoup.parse(createView().toString) must haveDynamicText(establisher)
    }

    "display header for trustees section" in {
      Jsoup.parse(createView().toString) must haveDynamicText(messages("messages__schemeReview__trustees__heading"))
    }

    "have link to edit trustees details when there are less than 10 trustees" in {
      Jsoup.parse(createView().toString).select("a[id=edit-trustees]") must haveLink(
        trusteeAddUrl.url
      )
      Jsoup.parse(createView().toString) must haveDynamicText("messages__schemeReview__trustees__editLink")
    }

    "have link to edit trustees details when there are 10 trustees" in {
      Jsoup.parse(createView().toString).select("a[id=edit-trustees]") must haveLink(
        trusteeAddUrl.url
      )
      Jsoup.parse(createSecView().toString) must haveDynamicText("messages__schemeReview__trustees__changeLink")
    }

    "contain list of trustees" in {
      for (trustees <- trustees)
        Jsoup.parse(createView().toString) must haveDynamicText(trustees)
    }
  }

}
