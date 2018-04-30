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

package controllers.register

import controllers.ControllerSpecBase
import controllers.actions._
import identifiers.register.SchemeDetailsId
import identifiers.register.establishers.EstablisherKindId
import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.establishers.individual.EstablisherDetailsId
import identifiers.register.trustees.HaveAnyTrusteesId
import identifiers.register.trustees.individual.TrusteeDetailsId
import models.{CheckMode, CompanyDetails, NormalMode}
import models.person.PersonDetails
import models.register.establishers.individual.EstablisherDetails
import models.register.{SchemeDetails, SchemeType}
import org.joda.time.LocalDate
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import play.api.test.Helpers.{contentAsString, _}
import utils.FakeNavigator
import views.html.register.schemeReview

class SchemeReviewControllerSpec extends ControllerSpecBase {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val validData: JsObject = Json.obj(
    SchemeDetailsId.toString ->
      SchemeDetails("Test Scheme Name", SchemeType.SingleTrust),
    "establishers" -> Json.arr(
      Json.obj(
        EstablisherKindId.toString -> "individual",
        EstablisherDetailsId.toString -> EstablisherDetails("establisher", None, "name", LocalDate.now())
      )
    ),
    "trustees" -> Json.arr(
      Json.obj(
        TrusteeDetailsId.toString -> PersonDetails("trustee", None, "name", LocalDate.now())
      ),
      Json.obj(
        identifiers.register.trustees.company.CompanyDetailsId.toString -> CompanyDetails("trustee company name", None, None)
      )
    )
  )

  val schemeName = "Test Scheme Name"
  val establisherIndv = Seq("establisher name")
  val establisherOrg = Seq("establisher")
  val trustees = Seq("trustee name", "trustee company name")
  val establisherIndvUrl: Call = controllers.register.establishers.individual.routes.CheckYourAnswersController.onPageLoad(0)
  val establisherCompanyUrl: Call = controllers.register.establishers.company.routes.CompanyReviewController.onPageLoad(0)

  val trusteeSingleTrustUrl = controllers.register.trustees.routes.AddTrusteeController.onPageLoad(CheckMode)
  val haveAnyTrusteeUrl = controllers.register.trustees.routes.HaveAnyTrusteesController.onPageLoad(NormalMode)

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData): SchemeReviewController =
    new SchemeReviewController(frontendAppConfig, messagesApi, new FakeNavigator(desiredRoute = onwardRoute), FakeAuthAction,
      dataRetrievalAction, new DataRequiredActionImpl)

  def viewAsString(establisherUrl: Call, trusteeUrl: Call, establishers: Seq[String] = establisherOrg, trustees: Seq[String] = Seq.empty): String =
    schemeReview(frontendAppConfig, schemeName, establishers, trustees, establisherUrl, trusteeUrl)(fakeRequest, messages).toString

  "SchemeReview Controller" when {

    "called GET" must {

      "return OK, the correct view and the correct Edit Urls for Trustees and establishers for individuals" in {
        val getRelevantData = new FakeDataRetrievalAction(Some(validData))
        val result = controller(getRelevantData).onPageLoad()(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(establisherIndvUrl, trusteeSingleTrustUrl, establisherIndv, trustees)
      }

      "return OK, the correct view and the correct Edit Urls for Trustees and establishers for company" in {
        val validData: JsObject = Json.obj(
          SchemeDetailsId.toString ->
            SchemeDetails("Test Scheme Name", SchemeType.SingleTrust),
          "establishers" -> Json.arr(
            Json.obj(
              EstablisherKindId.toString -> "company",
              CompanyDetailsId.toString -> CompanyDetails("establisher", None, None)
            )
          )
        )
        val getRelevantData = new FakeDataRetrievalAction(Some(validData))
        val result = controller(getRelevantData).onPageLoad()(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(establisherCompanyUrl, trusteeSingleTrustUrl)
      }

      "return OK, the correct view and the correct Edit Urls for Trustees and establishers for Body Corporate" in {
        val validData: JsObject = Json.obj(
          SchemeDetailsId.toString ->
            SchemeDetails("Test Scheme Name", SchemeType.BodyCorporate),
          "establishers" -> Json.arr(
            Json.obj(
              EstablisherKindId.toString -> "company",
              CompanyDetailsId.toString -> CompanyDetails("establisher", None, None)
            )
          ),
          HaveAnyTrusteesId.toString -> false
        )
        val getRelevantData = new FakeDataRetrievalAction(Some(validData))
        val result = controller(getRelevantData).onPageLoad()(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(establisherCompanyUrl, haveAnyTrusteeUrl)
      }
    }

    "redirect to the next page on submit" in {
      val result = controller().onSubmit()(fakeRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }
  }
}




