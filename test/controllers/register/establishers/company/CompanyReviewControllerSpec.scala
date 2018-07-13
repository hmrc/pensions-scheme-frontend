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

package controllers.register.establishers.company

import controllers.ControllerSpecBase
import controllers.actions._
import identifiers.register.SchemeDetailsId
import identifiers.register.establishers.{EstablishersId, IsEstablisherCompleteId}
import identifiers.register.establishers.company.{CompanyDetailsId, IsCompanyCompleteId}
import identifiers.register.establishers.company.director.{DirectorDetailsId, IsDirectorCompleteId}
import models.register.establishers.company.director.DirectorDetails
import models.register.{SchemeDetails, SchemeType}
import models.{CompanyDetails, Index}
import org.joda.time.LocalDate
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import play.api.test.Helpers._
import utils.{FakeNavigator, FakeSectionComplete}
import views.html.register.establishers.company.companyReview

class CompanyReviewControllerSpec extends ControllerSpecBase {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisherCompany): CompanyReviewController =
    new CompanyReviewController(frontendAppConfig, messagesApi, new FakeNavigator(desiredRoute = onwardRoute), FakeAuthAction,
      dataRetrievalAction, new DataRequiredActionImpl, FakeSectionComplete)

  val index = Index(0)
  val invalIndex= 10
  val invalidIndex = Index(invalIndex)
  val schemeName = "Test Scheme Name"
  val companyName = "test company name"
  val directors = Seq("director a", "director b", "director c")
  def director(lastName: String): JsObject = Json.obj(
    DirectorDetailsId.toString -> DirectorDetails("director", None, lastName, LocalDate.now()),
    IsDirectorCompleteId.toString -> true
  )

  val validData: JsObject = Json.obj(
    SchemeDetailsId.toString ->
      SchemeDetails(schemeName, SchemeType.SingleTrust),
    EstablishersId.toString -> Json.arr(
      Json.obj(
        CompanyDetailsId.toString ->
          CompanyDetails(companyName, Some("123456"), Some("abcd")),
        IsCompanyCompleteId.toString -> true,

        "director" -> Json.arr(
          director("a"), director("b"), director("c")
        )
      )
    )
  )

  def viewAsString(): String = companyReview(frontendAppConfig, index, schemeName, companyName, directors)(fakeRequest, messages).toString

  "CompanyReview Controller" must {

    "return OK and the correct view for a GET" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val result = controller(getRelevantData).onPageLoad(index)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to session expired page on a GET when the index is not valid" ignore {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val result = controller(getRelevantData).onPageLoad(invalidIndex)(fakeRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to the next page on submit" in {
      val result = controller().onSubmit(index)(fakeRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "set establisher as complete when company is complete and all directors are completed on submit" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val result = controller(getRelevantData).onSubmit(index)(fakeRequest)
      status(result) mustBe SEE_OTHER
      FakeSectionComplete.verify(IsEstablisherCompleteId(0), true)
    }

    "not set establisher as complete when company is not complete but directors are completed" in {
      FakeSectionComplete.reset()
      val validData: JsObject = Json.obj(
        SchemeDetailsId.toString ->
          SchemeDetails(schemeName, SchemeType.SingleTrust),
        EstablishersId.toString -> Json.arr(
          Json.obj(
            CompanyDetailsId.toString ->
              CompanyDetails(companyName, Some("123456"), Some("abcd")),

            "director" -> Json.arr(
              director("a"), director("b"), director("c")
            )
          )
        )
      )

      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val result = controller(getRelevantData).onSubmit(index)(fakeRequest)
      status(result) mustBe SEE_OTHER
      FakeSectionComplete.verifyNot(IsEstablisherCompleteId(0))
    }

    "not set establisher as complete when company is complete but directors are not complete" in {
      FakeSectionComplete.reset()
      val validData: JsObject = Json.obj(
        SchemeDetailsId.toString ->
          SchemeDetails(schemeName, SchemeType.SingleTrust),
        EstablishersId.toString -> Json.arr(
          Json.obj(
            CompanyDetailsId.toString ->
              CompanyDetails(companyName, Some("123456"), Some("abcd")),
            IsCompanyCompleteId.toString -> true,
            "director" -> Json.arr(
              director("a"), director("b"),
              Json.obj(
                DirectorDetailsId.toString -> DirectorDetails("director", None, "c", LocalDate.now()),
                IsDirectorCompleteId.toString -> false
              )
            )
          )
        )
      )

      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val result = controller(getRelevantData).onSubmit(index)(fakeRequest)
      status(result) mustBe SEE_OTHER
      FakeSectionComplete.verifyNot(IsEstablisherCompleteId(0))
    }
  }
}




