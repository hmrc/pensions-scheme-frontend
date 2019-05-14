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

import controllers.register.establishers.company.routes
import identifiers.register.establishers.company.director.DirectorDetailsId
import models.person.PersonDetails
import models.{Index, NormalMode, UpdateMode}
import org.joda.time.LocalDate
import org.jsoup.Jsoup
import play.api.libs.json.{JsObject, Json}
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.register.establishers.company.companyReview

class CompanyReviewViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "companyReview"
  val index = Index(0)
  val companyName = "test company name"
  val directors = Seq("director a", "director b", "director c")
  val tenDirectors = Seq("director a", "director b", "director c", "director d", "director e",
    "director f", "director g", "director h", "director i", "director j")

  def director(lastName: String): JsObject = Json.obj(
    DirectorDetailsId.toString -> PersonDetails("director", None, lastName, LocalDate.now())
  )

  def createView(viewOnly: Boolean = false): () => HtmlFormat.Appendable = () =>
    companyReview(frontendAppConfig, index, companyName, directors, None, NormalMode, None, viewOnly)(fakeRequest, messages)

  def createUpdateView(viewOnly: Boolean = false): () => HtmlFormat.Appendable = () =>
    companyReview(frontendAppConfig, index, companyName, directors, None, UpdateMode, Some("srn"), viewOnly)(fakeRequest, messages)

  def createSecView: () => HtmlFormat.Appendable = () => companyReview(frontendAppConfig, index, companyName, tenDirectors, None, NormalMode, None, false)(fakeRequest, messages)

  "CompanyReview view" must {
    behave like normalPage(
      createView(),
      messageKeyPrefix,
      messages(s"messages__${messageKeyPrefix}__heading"),
      "_directors__heading")

    "display company name" in {
      Jsoup.parse(createView()().toString) must haveDynamicText(companyName)
    }

    "have link to edit company details" in {
      Jsoup.parse(createView()().toString).select("a[id=edit-company-details]") must haveLink(
        routes.CheckYourAnswersController.onPageLoad(NormalMode, None, index).url
      )
    }

    "have link to view company details when viewOnly flag is true" in {
      Jsoup.parse(createView()().toString).select("a[id=edit-company-details]") must haveLink(
        routes.CheckYourAnswersController.onPageLoad(NormalMode, None, index).url
      )
      Jsoup.parse(createView(true)().toString) must haveDynamicText("messages__companyReview__company__viewLink")
    }

    "have link to edit director details when there are less than 10 directors" in {
      Jsoup.parse(createView()().toString).select("a[id=edit-director-details]") must haveLink(
        routes.AddCompanyDirectorsController.onPageLoad(NormalMode, None, index).url
      )
      Jsoup.parse(createView()().toString) must haveDynamicText("messages__companyReview__directors__editLink")

    }

    "have link to edit directors when there are 10 directors" in {
      Jsoup.parse(createView()().toString).select("a[id=edit-director-details]") must haveLink(
        routes.AddCompanyDirectorsController.onPageLoad(NormalMode, None, index).url
      )
      Jsoup.parse(createSecView().toString) must haveDynamicText("messages__companyReview__directors__changeLink")
    }

    "have link to view directors when viewOnly flag is true" in {
      Jsoup.parse(createView()().toString).select("a[id=edit-director-details]") must haveLink(
        routes.AddCompanyDirectorsController.onPageLoad(NormalMode, None, index).url
      )
      Jsoup.parse(createView(true)().toString) must haveDynamicText("messages__companyReview__directors__viewLink")
    }

    "not have confirm button when viewOnly flag is true" in {
      val view = asDocument(createView(true)())
      assertNotRenderedById(view, "submit")
    }

    "have confirm button when viewOnly flag is false" in {
      val view = asDocument(createView()())
      assertRenderedById(view, "submit")
    }

    "contain list of directors" in {
      for (director <- directors)
        Jsoup.parse(createView()().toString) must haveDynamicText(director)
    }

    behave like pageWithReturnLink(createView(), getReturnLink)

    behave like pageWithReturnLinkAndSrn(createUpdateView(), getReturnLinkWithSrn)
  }

}
