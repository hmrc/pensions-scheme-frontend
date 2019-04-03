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

package views.register.establishers.partnership

import controllers.register.establishers.partnership.routes
import identifiers.register.establishers.partnership.partner.PartnerDetailsId
import models.{Index, NormalMode}
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

  def createView(readOnly: Boolean = false): () => HtmlFormat.Appendable = () => partnershipReview(
    frontendAppConfig,
    index,
    partnershipName,
    partners,
    None,
    None,
    NormalMode,
    readOnly
  )(fakeRequest, messages)

  def createSecView: () => HtmlFormat.Appendable = () => partnershipReview(
    frontendAppConfig,
    index,
    partnershipName,
    tenPartners,
    None,
    None,
    NormalMode,
    false
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
        routes.CheckYourAnswersController.onPageLoad(NormalMode, index, None).url
      )
    }

    "have link to view partnership details when readOnly flag is true" in {
      Jsoup.parse(createView()().toString).select("a[id=edit-partnership-details]") must haveLink(
        routes.CheckYourAnswersController.onPageLoad(NormalMode, index, None).url
      )
      Jsoup.parse(createView(true)().toString) must haveDynamicText("messages__partnershipReview__partnership__viewLink")
    }

    "have link to edit partner details when there are less than 10 partners" in {
      Jsoup.parse(createView()().toString).select("a[id=edit-partner-details]") must haveLink(
        routes.AddPartnersController.onPageLoad(NormalMode, index, None).url
      )
      Jsoup.parse(createView()().toString) must haveDynamicText("messages__partnershipReview__partners__editLink")

    }

    "have link to edit partners when there are 10 partners" in {
      Jsoup.parse(createView()().toString).select("a[id=edit-partner-details]") must haveLink(
        routes.AddPartnersController.onPageLoad(NormalMode, index, None).url
      )
      Jsoup.parse(createSecView().toString) must haveDynamicText("messages__partnershipReview__partners__changeLink")
    }

    "have link to view partner details when readOnly flag is true" in {
      Jsoup.parse(createView()().toString).select("a[id=edit-partner-details]") must haveLink(
        routes.AddPartnersController.onPageLoad(NormalMode, index, None).url
      )
      Jsoup.parse(createView(true)().toString) must haveDynamicText("messages__partnershipReview__partners__viewLink")

    }

    "contain list of partners" in {
      for (partner <- partners)
        Jsoup.parse(createView()().toString) must haveDynamicText(partner)
    }

    behave like pageWithReturnLink(createView(), getReturnLink)

  }
}
