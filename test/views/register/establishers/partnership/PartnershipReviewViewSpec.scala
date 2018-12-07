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

package views.register.establishers.partnership

import controllers.register.establishers.partnership.routes
import identifiers.register.establishers.partnership.partner.PartnerDetailsId
import models.Index
import models.person.PersonDetails
import org.joda.time.LocalDate
import org.jsoup.Jsoup
import play.api.libs.json.{JsObject, Json}
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.register.establishers.partnership.partnershipReview

class PartnershipReviewViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "partnershipReview"
  val index = Index(0)
  val partnershipName = "test partnership name"
  val partners = Seq("partner a", "partner b", "partner c")
  val tenPartners = Seq("partner a", "partner b", "partner c", "partner d", "partner e",
    "partner f", "partner g", "partner h", "partner i", "partner j")

  def partner(lastName: String): JsObject = Json.obj(
    PartnerDetailsId.toString -> PersonDetails("partner", None, lastName, LocalDate.now())
  )

  def createView(isHubEnabled: Boolean = false): () => HtmlFormat.Appendable = () => partnershipReview(
    appConfig(isHubEnabled),
    index,
    partnershipName,
    partners
  )(fakeRequest, messages)

  def createSecView: () => HtmlFormat.Appendable = () => partnershipReview(
    frontendAppConfig,
    index,
    partnershipName,
    tenPartners
  )(fakeRequest, messages)

  "PartnershipReview view" must {
    behave like normalPage(
      createView(),
      messageKeyPrefix,
      messages(s"messages__${messageKeyPrefix}__heading"),
      "_partners__heading")

    "display partnership name" in {
      Jsoup.parse(createView()().toString) must haveDynamicText(partnershipName)
    }

    "have link to edit partnership details" in {
      Jsoup.parse(createView()().toString).select("a[id=edit-partnership-details]") must haveLink(
        routes.CheckYourAnswersController.onPageLoad(index).url
      )
    }

    "have link to edit partner details when there are less than 10 partners" in {
      Jsoup.parse(createView()().toString).select("a[id=edit-partner-details]") must haveLink(
        routes.AddPartnersController.onPageLoad(index).url
      )
      Jsoup.parse(createView()().toString) must haveDynamicText("messages__partnershipReview__partners__editLink")

    }

    "have link to edit partners when there are 10 partners" in {
      Jsoup.parse(createView()().toString).select("a[id=edit-partner-details]") must haveLink(
        routes.AddPartnersController.onPageLoad(index).url
      )
      Jsoup.parse(createSecView().toString) must haveDynamicText("messages__partnershipReview__partners__changeLink")
    }

    "contain list of partners" in {
      for (partner <- partners)
        Jsoup.parse(createView()().toString) must haveDynamicText(partner)
    }

    "not have a return link" in {
      val doc = asDocument(createView(isHubEnabled = false)())
      assertNotRenderedById(doc, "return-link")
    }
  }

  "PartnershipDetails view with hub enabled" must {
    behave like pageWithReturnLink(createView(isHubEnabled = true), controllers.register.routes.SchemeTaskListController.onPageLoad().url)
  }

}
