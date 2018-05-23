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

package views.register.establishers.company

import controllers.register.establishers.company.routes
import identifiers.register.SchemeDetailsId
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.establishers.company.director.DirectorDetailsId
import models.register.establishers.company.director.DirectorDetails
import models.register.{SchemeDetails, SchemeType}
import models.{CheckMode, CompanyDetails, Index}
import org.joda.time.LocalDate
import org.jsoup.Jsoup
import play.api.libs.json.{JsObject, Json}
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.register.establishers.company.companyReview

class CompanyReviewViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "companyReview"
  val index = Index(0)
  val schemeName = "Test Scheme Name"
  val companyName = "test company name"
  val directors = Seq("director a", "director b", "director c")
  val tenDirectors = Seq("director a", "director b", "director c", "director d", "director e",
    "director f", "director g", "director h", "director i", "director j")
  def director(lastName: String): JsObject = Json.obj(
    DirectorDetailsId.toString -> DirectorDetails("director", None, lastName, LocalDate.now())
  )

  def createView: (() => HtmlFormat.Appendable) = () => companyReview(frontendAppConfig, index, schemeName, companyName, directors)(fakeRequest, messages)
  def createSecView: (() => HtmlFormat.Appendable) = () => companyReview(frontendAppConfig, index, schemeName, companyName, tenDirectors)(fakeRequest, messages)

  "CompanyReview view" must {
    behave like normalPage(
      createView,
      messageKeyPrefix,
      messages(s"messages__${messageKeyPrefix}__heading"),
    "_directors__heading")

    behave like pageWithSecondaryHeader(
      createView,
      s"${messages("messages__companyReview__secondaryHeading__partial")} $schemeName")

    "display company name" in {
      Jsoup.parse(createView().toString) must haveDynamicText(companyName)
    }

    "have link to edit company details" in {
      Jsoup.parse(createView().toString).select("a[id=edit-company-details]") must haveLink(
        routes.CompanyDetailsController.onPageLoad(CheckMode, index).url
      )
    }

    "have link to edit director details when there are less than 10 directors" in {
      Jsoup.parse(createView().toString).select("a[id=edit-director-details]") must haveLink(
        routes.AddCompanyDirectorsController.onPageLoad(CheckMode, index).url
      )
      Jsoup.parse(createView().toString) must haveDynamicText("messages__companyReview__directors__editLink")

    }

    "have link to edit directors when there are 10 directors" in {
      Jsoup.parse(createView().toString).select("a[id=edit-director-details]") must haveLink(
        routes.AddCompanyDirectorsController.onPageLoad(CheckMode, index).url
      )
      Jsoup.parse(createSecView().toString) must haveDynamicText("messages__companyReview__directors__changeLink")
    }

    "contain list of directors" in {
      for( director <- directors)
        Jsoup.parse(createView().toString) must haveDynamicText(director)
      }
    }

}
