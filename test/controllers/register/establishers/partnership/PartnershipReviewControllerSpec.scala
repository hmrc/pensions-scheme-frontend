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

package controllers.register.establishers.partnership

import controllers.ControllerSpecBase
import controllers.actions._
import controllers.behaviours.ControllerAllowChangeBehaviour
import identifiers.register.establishers.partnership.partner.{IsPartnerAddressCompleteId, IsPartnerCompleteId, PartnerDetailsId}
import identifiers.register.establishers.partnership.{IsPartnershipCompleteId, PartnershipDetailsId}
import identifiers.register.establishers.{EstablishersId, IsEstablisherAddressCompleteId, IsEstablisherCompleteId}
import models.person.PersonDetails
import models.{Index, NormalMode, PartnershipDetails}
import org.joda.time.LocalDate
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import play.api.test.Helpers._
import services.FakeUserAnswersService
import utils.{AllowChangeHelper, FakeNavigator}
import views.html.register.establishers.partnership.partnershipReview

class PartnershipReviewControllerSpec extends ControllerSpecBase with ControllerAllowChangeBehaviour {

  import PartnershipReviewControllerSpec._

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisherPartnership,
                 allowChangeHelper: AllowChangeHelper = ach): PartnershipReviewController =
    new PartnershipReviewController(
      frontendAppConfig,
      messagesApi,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      FakeUserAnswersService,
      allowChangeHelper
    )

  def viewAsString(): String = partnershipReview(
    frontendAppConfig,
    index,
    partnershipName,
    partnerNames,
    None,
    None,
    NormalMode,
    false,
    false
  )(fakeRequest, messages).toString

  "PartnershipReview Controller" must {

    "return OK and the correct view for a GET" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData()))
      val result = controller(getRelevantData).onPageLoad(NormalMode, index, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to session expired page on a GET when the index is not valid" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData()))
      val result = controller(getRelevantData).onPageLoad(NormalMode, invalidIndex, None)(fakeRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to the next page on submit" in {
      val result = controller().onSubmit(NormalMode, index, None)(fakeRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "set establisher as complete when partnership is complete and all partners are completed on submit" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData()))
      val result = controller(getRelevantData).onSubmit(NormalMode, index, None)(fakeRequest)
      status(result) mustBe SEE_OTHER
      FakeUserAnswersService.verify(IsEstablisherCompleteId(0), true)
    }

    "not set establisher as complete when partner is not complete but partners are completed" in {
      FakeUserAnswersService.reset()
      val validData: JsObject = Json.obj(
        EstablishersId.toString -> Json.arr(
          Json.obj(
            PartnershipDetailsId.toString -> partnershipDetails,
            "partner" -> partners
          )
        )
      )
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val result = controller(getRelevantData).onSubmit(NormalMode, index, None)(fakeRequest)
      status(result) mustBe SEE_OTHER
      FakeUserAnswersService.verifyNot(IsEstablisherCompleteId(0))
    }

    "not set establisher as complete when partnership is complete but partners are not complete" in {
      FakeUserAnswersService.reset()
      val getRelevantData = new FakeDataRetrievalAction(Some(validData(isComplete = false)))
      val result = controller(getRelevantData).onSubmit(NormalMode, index, None)(fakeRequest)
      status(result) mustBe SEE_OTHER
      FakeUserAnswersService.verifyNot(IsEstablisherCompleteId(0))
    }

    "not set establisher as complete when partnership is complete but partners are not present" in {
      FakeUserAnswersService.reset()
      val validData: JsObject = Json.obj(
        EstablishersId.toString -> Json.arr(
          Json.obj(
            PartnershipDetailsId.toString -> partnershipDetails,
            IsPartnershipCompleteId.toString -> true
          )
        )
      )
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val result = controller(getRelevantData).onSubmit(NormalMode, index, None)(fakeRequest)
      status(result) mustBe SEE_OTHER
      FakeUserAnswersService.verifyNot(IsEstablisherCompleteId(0))
    }
  }
}

object PartnershipReviewControllerSpec {
  val index = Index(0)
  val invalidIndex = Index(10)
  val schemeName = "Test Scheme Name"
  val partnershipName = "test partnership name"
  val partnerNames = Seq("partner a", "partner b", "partner c")
  val partnershipDetails = PartnershipDetails(partnershipName)

  def partner(lastName: String, isComplete: Boolean = true): JsObject = Json.obj(
    PartnerDetailsId.toString -> PersonDetails("partner", None, lastName, LocalDate.now()),
    IsPartnerCompleteId.toString -> isComplete
  )

  val partners = Seq(partner("a"), partner("b"), partner("c"))

  def validData(isComplete: Boolean = true): JsObject = Json.obj(
    EstablishersId.toString -> Json.arr(
      Json.obj(
        PartnershipDetailsId.toString -> partnershipDetails,
        IsPartnershipCompleteId.toString -> true,
        IsEstablisherAddressCompleteId.toString -> true,
        "partner" -> Json.arr(
          Json.obj("partnerDetails" -> Json.obj("firstName" -> "partner", "lastName" -> "a", "date" -> "2019-04-30", "isDeleted" -> false),
            "isPartnerComplete" -> true, IsPartnerAddressCompleteId.toString -> true),
          Json.obj("partnerDetails" -> Json.obj("firstName" -> "partner", "lastName" -> "b", "date" -> "2019-04-30", "isDeleted" -> false),
            "isPartnerComplete" -> true, IsPartnerAddressCompleteId.toString -> true),
          Json.obj("partnerDetails" -> Json.obj("firstName" -> "partner", "lastName" -> "c", "date" -> "2019-04-30", "isDeleted" -> false),
            "isPartnerComplete" -> isComplete, IsPartnerAddressCompleteId.toString -> true)
        )
      )
    )
  )
}
