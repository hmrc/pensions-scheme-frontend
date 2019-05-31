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

package controllers.register.establishers.company

import controllers.ControllerSpecBase
import controllers.actions._
import controllers.behaviours.ControllerAllowChangeBehaviour
import identifiers.register.establishers.company.director.{DirectorDetailsId, IsDirectorAddressCompleteId, IsDirectorCompleteId}
import identifiers.register.establishers.company.{CompanyDetailsId, CompanyPayeId, IsCompanyCompleteId}
import identifiers.register.establishers.{EstablishersId, IsEstablisherAddressCompleteId, IsEstablisherCompleteId}
import identifiers.register.trustees.company.CompanyVatId
import models._
import models.person.PersonDetails
import org.joda.time.LocalDate
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import play.api.test.Helpers._
import services.FakeUserAnswersService
import utils.{AllowChangeHelper, FakeNavigator}
import views.html.register.establishers.company.companyReview

class CompanyReviewControllerSpec extends ControllerSpecBase with ControllerAllowChangeBehaviour {

  import CompanyReviewControllerSpec._

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisherCompany,
                 allowChangeHelper: AllowChangeHelper = ach): CompanyReviewController =
    new CompanyReviewController(frontendAppConfig, messagesApi, new FakeNavigator(desiredRoute = onwardRoute), FakeAuthAction,
      dataRetrievalAction, FakeAllowAccessProvider(),new DataRequiredActionImpl, FakeUserAnswersService, allowChangeHelper)

  def viewAsString(): String = companyReview(frontendAppConfig, index, companyName, directorNames, None, NormalMode, None, false, false)(fakeRequest, messages).toString

  "CompanyReview Controller" must {

    "return OK and the correct view for a GET" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData()))
      val result = controller(getRelevantData).onPageLoad(NormalMode, None, index)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to session expired page on a GET when the index is not valid" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData()))
      val result = controller(getRelevantData).onPageLoad(NormalMode, None, invalidIndex)(fakeRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    behave like changeableController(
      controller(new FakeDataRetrievalAction(Some(validData())), _:AllowChangeHelper)
        .onPageLoad(NormalMode, None, index)(fakeRequest)
    )

    "redirect to the next page on submit" in {
      val result = controller().onSubmit(NormalMode, None, index)(fakeRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "set establisher as complete when company is complete and all directors are completed on submit" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData()))
      val result = controller(getRelevantData).onSubmit(NormalMode, None, index)(fakeRequest)
      status(result) mustBe SEE_OTHER
      FakeUserAnswersService.verify(IsEstablisherCompleteId(0), true)
    }

    "not set establisher as complete when company is not complete but directors are completed" in {
      FakeUserAnswersService.reset()
      val validData: JsObject = Json.obj(
        EstablishersId.toString -> Json.arr(
          Json.obj(
            CompanyDetailsId.toString -> companyDetails,
            "director" -> directors
          )
        )
      )
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val result = controller(getRelevantData).onSubmit(NormalMode, None, index)(fakeRequest)
      status(result) mustBe SEE_OTHER
      FakeUserAnswersService.verifyNot(IsEstablisherCompleteId(0))
    }

    "not set establisher as complete when company is complete but directors are not complete" in {
      FakeUserAnswersService.reset()
      val getRelevantData = new FakeDataRetrievalAction(Some(validData(isComplete = false)))
      val result = controller(getRelevantData).onSubmit(NormalMode, None, index)(fakeRequest)
      status(result) mustBe SEE_OTHER
      FakeUserAnswersService.verifyNot(IsEstablisherCompleteId(0))
    }

    "not set establisher as complete when company is complete but directors are not present" in {
      FakeUserAnswersService.reset()
      val validData: JsObject = Json.obj(
        EstablishersId.toString -> Json.arr(
          Json.obj(
            CompanyDetailsId.toString -> companyDetails,
            IsCompanyCompleteId.toString -> true
          )
        )
      )
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val result = controller(getRelevantData).onSubmit(NormalMode, None, index)(fakeRequest)
      status(result) mustBe SEE_OTHER
      FakeUserAnswersService.verifyNot(IsEstablisherCompleteId(0))
    }
  }
}

object CompanyReviewControllerSpec {
  val index = Index(0)
  val invalIndex = 10
  val invalidIndex = Index(invalIndex)
  val schemeName = "Test Scheme Name"
  val companyName = "test company name"
  val directorNames = Seq("director a", "director b", "director c")
  val companyDetails = CompanyDetails(companyName)

  def director(lastName: String, isComplete: Boolean = true): JsObject = Json.obj(
    DirectorDetailsId.toString -> PersonDetails("director", None, lastName, LocalDate.now()),
    IsDirectorCompleteId.toString -> isComplete
  )

  val directors = Seq(director("a"), director("b"), director("c"))

  def validData(isComplete:Boolean = true): JsObject = Json.obj(
    EstablishersId.toString -> Json.arr(
      Json.obj(
        CompanyDetailsId.toString -> companyDetails,
        IsCompanyCompleteId.toString -> true,
        CompanyPayeId.toString -> Paye.Yes("paye"),
        CompanyVatId.toString -> Vat.Yes("vat"),
        IsEstablisherAddressCompleteId.toString -> true,
        "director" -> Json.arr(
          Json.obj("directorDetails" -> Json.obj("firstName" -> "director", "lastName" -> "a", "date" -> "2019-04-30", "isDeleted" -> false),
            "isDirectorComplete" -> true, IsDirectorAddressCompleteId.toString -> true),
          Json.obj("directorDetails" -> Json.obj("firstName" -> "director", "lastName" -> "b", "date" -> "2019-04-30", "isDeleted" -> false),
            "isDirectorComplete" -> true, IsDirectorAddressCompleteId.toString -> true),
          Json.obj("directorDetails" -> Json.obj("firstName" -> "director", "lastName" -> "c", "date" -> "2019-04-30", "isDeleted" -> false),
            "isDirectorComplete" -> isComplete, IsDirectorAddressCompleteId.toString -> true)
        )
      )
    )
  )
}



