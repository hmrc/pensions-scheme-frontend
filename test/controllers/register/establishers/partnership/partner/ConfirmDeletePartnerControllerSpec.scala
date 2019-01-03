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

package controllers.register.establishers.partnership.partner

import connectors.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAuthAction, FakeDataRetrievalAction}
import controllers.register.establishers.partnership.routes.AddPartnersController
import identifiers.register.SchemeDetailsId
import identifiers.register.establishers.partnership.PartnershipDetailsId
import identifiers.register.establishers.partnership.partner.PartnerDetailsId
import identifiers.register.establishers.{EstablishersId, IsEstablisherCompleteId}
import models.person.PersonDetails
import models.register.{SchemeDetails, SchemeType}
import models.{Index, PartnershipDetails}
import org.joda.time.LocalDate
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers._
import utils.{FakeNavigator, FakeSectionComplete}
import views.html.register.establishers.partnership.partner.confirmDeletePartner

class ConfirmDeletePartnerControllerSpec extends ControllerSpecBase {

  import ConfirmDeletePartnerControllerSpec._

  "ConfirmDeletePartner Controller" must {
    "return OK and the correct view for a GET" in {
      val data = new FakeDataRetrievalAction(Some(testData()))
      val result = controller(data).onPageLoad(establisherIndex, partnerIndex)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to already deleted view for a GET if the partner was already deleted" in {
      val data = new FakeDataRetrievalAction(Some(testData(partnerDeleted)))
      val result = controller(data).onPageLoad(establisherIndex, partnerIndex)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(routes.AlreadyDeletedController.onPageLoad(establisherIndex, partnerIndex).url)
    }

    "delete the partner on a POST" in {
      val data = new FakeDataRetrievalAction(Some(testData()))
      val result = controller(data).onSubmit(establisherIndex, partnerIndex)(fakeRequest)

      status(result) mustBe SEE_OTHER
      FakeUserAnswersCacheConnector.verify(PartnerDetailsId(establisherIndex, partnerIndex), partnerDetails.copy(isDeleted = true))
    }

    "redirect to the next page on a successful POST" in {
      val data = new FakeDataRetrievalAction(Some(testData()))
      val result = controller(data).onSubmit(establisherIndex, partnerIndex)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(establisherIndex, partnerIndex)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val result = controller(dontGetAnyData).onSubmit(establisherIndex, partnerIndex)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "set the establisher as not complete when partners are deleted" in {
      FakeSectionComplete.reset()
      val validData: JsObject = Json.obj(
        SchemeDetailsId.toString ->
          SchemeDetails("Test Scheme Name", SchemeType.SingleTrust),
        EstablishersId.toString -> Json.arr(
          Json.obj(
            PartnershipDetailsId.toString -> PartnershipDetails(partnershipName),
            "partner" -> Json.arr(
              Json.obj(
                PartnerDetailsId.toString ->
                  PersonDetails("John", None, "Doe", LocalDate.now())
              )
            )
          )
        )
      )
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val result = controller(getRelevantData).onSubmit(establisherIndex, partnerIndex)(fakeRequest)
      status(result) mustBe SEE_OTHER
      FakeSectionComplete.verify(IsEstablisherCompleteId(0), false)
    }
  }

}

object ConfirmDeletePartnerControllerSpec extends ControllerSpecBase {

  private val establisherIndex = Index(0)
  private val partnerIndex = Index(0)
  private val partnershipName = "My Partnership Ltd"
  private val partnerName = "John Doe"

  private lazy val postCall = routes.ConfirmDeletePartnerController.onSubmit(establisherIndex, partnerIndex)
  private lazy val cancelCall = AddPartnersController.onPageLoad(establisherIndex)
  private val partnerDetails = PersonDetails("John", None, "Doe", LocalDate.now())

  private def testData(partners: PersonDetails = partnerDetails) = Json.obj(
    EstablishersId.toString -> Json.arr(
      Json.obj(
        PartnershipDetailsId.toString -> PartnershipDetails(partnershipName),
        "partner" -> Json.arr(
          Json.obj(
            PartnerDetailsId.toString ->
              partners
          )
        )
      )
    )
  )

  val partnerDeleted: PersonDetails = partnerDetails.copy(isDeleted = true)

  private def onwardRoute = controllers.routes.IndexController.onPageLoad()

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new ConfirmDeletePartnerController(
      frontendAppConfig,
      messagesApi,
      FakeUserAnswersCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      FakeSectionComplete
    )

  private def viewAsString() = confirmDeletePartner(
    frontendAppConfig,
    partnerName,
    postCall,
    cancelCall
  )(fakeRequest, messages).toString

}
