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

package controllers.register.establishers

import services.FakeUserAnswersService
import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.establishers.ConfirmDeleteEstablisherFormProvider
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.establishers.individual.EstablisherDetailsId
import identifiers.register.establishers.partnership.PartnershipDetailsId
import models.person.PersonDetails
import models.register.establishers.EstablisherKind
import models.{CompanyDetails, Index, NormalMode, PartnershipDetails}
import org.joda.time.LocalDate
import play.api.libs.json._
import play.api.mvc.{AnyContentAsFormUrlEncoded, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.FakeNavigator
import views.html.register.establishers.confirmDeleteEstablisher

class ConfirmDeleteEstablisherControllerSpec extends ControllerSpecBase {

  import ConfirmDeleteEstablisherControllerSpec._

  "ConfirmDeleteEstablisher Controller" must {
    "return OK and the correct view for a GET" in {
      val data = new FakeDataRetrievalAction(Some(testData))
      val result = controller(data).onPageLoad(NormalMode, establisherIndex, establisherKind, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "return OK and the correct view for a GET when establisher kind is company" in {
      val establisherName = "Test Ltd"
      val establisherIndex = Index(1)
      val postCall = routes.ConfirmDeleteEstablisherController.onSubmit(NormalMode, establisherIndex, EstablisherKind.Company, None)
      val data = new FakeDataRetrievalAction(Some(testData))
      val result = controller(data).onPageLoad(NormalMode, establisherIndex, EstablisherKind.Company, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(
        hintText = Some(messages("messages__confirmDeleteEstablisher__companyHint")),
        estName = establisherName,
        postCall = postCall)
    }

    "return OK and the correct view for a GET when establisher kind is partnership" in {
      val establisherName = "Test Partnership Ltd"
      val establisherIndex = Index(2)
      val postCall = routes.ConfirmDeleteEstablisherController.onSubmit(NormalMode, establisherIndex, EstablisherKind.Partnership, None)
      val data = new FakeDataRetrievalAction(Some(testData))
      val result = controller(data).onPageLoad(NormalMode, establisherIndex, EstablisherKind.Partnership, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(
        hintText = Some(messages("messages__confirmDeleteEstablisher__partnershipHint")),
        estName = establisherName,
        postCall = postCall)
    }

    "redirect to already deleted view for a GET if the establisher was already deleted" in {

      val deletedData = Json.obj(
        EstablishersId.toString -> Json.arr(
          Json.obj(
            EstablisherDetailsId.toString -> deletedEstablisher
          )
        )
      )
      val data = new FakeDataRetrievalAction(Some(deletedData))

      val result = controller(data).onPageLoad(NormalMode, establisherIndex, establisherKind, None)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(routes.AlreadyDeletedController.onPageLoad(NormalMode, establisherIndex, establisherKind, None).url)
    }

    "delete the establisher individual on a POST" in {
      val data = new FakeDataRetrievalAction(Some(testData))
      val result = controller(data).onSubmit(NormalMode, establisherIndex, establisherKind, None)(postRequest)

      status(result) mustBe SEE_OTHER
      FakeUserAnswersService.verify(EstablisherDetailsId(establisherIndex), personDetails.copy(isDeleted = true))
    }

    "dont delete the establisher individual on a POST if selcted NO" in {
      FakeUserAnswersService.reset()
      val data = new FakeDataRetrievalAction(Some(testData))
      val result = controller(data).onSubmit(NormalMode, establisherIndex, establisherKind, None)(postRequestForCancle)

      status(result) mustBe SEE_OTHER
      FakeUserAnswersService.verifyNot(EstablisherDetailsId(establisherIndex))
    }

    "delete the establisher company on a POST" in {
      val data = new FakeDataRetrievalAction(Some(testData))
      val result = controller(data).onSubmit(NormalMode, Index(1), EstablisherKind.Company, None)(postRequest)

      status(result) mustBe SEE_OTHER
      FakeUserAnswersService.verify(CompanyDetailsId(Index(1)), companyDetails.copy(isDeleted = true))
    }

    "dont delete the establisher company on a POST if selected NO" in {
      FakeUserAnswersService.reset()
      val data = new FakeDataRetrievalAction(Some(testData))
      val result = controller(data).onSubmit(NormalMode, Index(1), EstablisherKind.Company, None)(postRequestForCancle)

      status(result) mustBe SEE_OTHER
      FakeUserAnswersService.verifyNot(CompanyDetailsId(Index(1)))
    }

    "delete the establisher partnership on a POST" in {
      val data = new FakeDataRetrievalAction(Some(testData))
      val result = controller(data).onSubmit(NormalMode, Index(2), EstablisherKind.Partnership, None)(postRequest)

      status(result) mustBe SEE_OTHER
      FakeUserAnswersService.verify(PartnershipDetailsId(Index(2)), partnershipDetails.copy(isDeleted = true))
    }

    "dont delete the establisher partnership on a POST if selected No" in {
      FakeUserAnswersService.reset()
      val data = new FakeDataRetrievalAction(Some(testData))
      val result = controller(data).onSubmit(NormalMode, Index(2), EstablisherKind.Partnership, None)(postRequestForCancle)

      status(result) mustBe SEE_OTHER
      FakeUserAnswersService.verifyNot(PartnershipDetailsId(Index(2)))
    }

    "redirect to the next page on a successful POST" in {
      val data = new FakeDataRetrievalAction(Some(testData))
      val result = controller(data).onSubmit(NormalMode, establisherIndex, establisherKind, None)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode, establisherIndex, establisherKind, None)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val result = controller(dontGetAnyData).onSubmit(NormalMode, establisherIndex, establisherKind, None)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }

}

object ConfirmDeleteEstablisherControllerSpec extends ControllerSpecBase {

  private val establisherIndex = Index(0)
  private val schemeName = "MyScheme Ltd"
  private val establisherName = "John Doe"
  private val establisherKind = EstablisherKind.Indivdual
  private val day = LocalDate.now().getDayOfMonth
  private val month = LocalDate.now().getMonthOfYear
  private val year = LocalDate.now().getYear - 20
  private lazy val postCall = routes.ConfirmDeleteEstablisherController.onSubmit(NormalMode, establisherIndex, establisherKind, None)
  private lazy val cancelCall = routes.AddEstablisherController.onPageLoad(NormalMode)
  private val personDetails = PersonDetails("John", None, "Doe", new LocalDate(year, month, day))
  private val companyDetails = CompanyDetails("Test Ltd", None, None)
  private val partnershipDetails = PartnershipDetails("Test Partnership Ltd")
  private val deletedEstablisher = personDetails.copy(isDeleted = true)

  private val formProvider = new ConfirmDeleteEstablisherFormProvider()
  private val form = formProvider.apply()

  private val postRequest: FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest().withFormUrlEncodedBody(("value", "true"))

  private val postRequestForCancle: FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest().withFormUrlEncodedBody(("value", "false"))

  private val testData = Json.obj(
    EstablishersId.toString -> Json.arr(
      Json.obj(
        EstablisherDetailsId.toString -> personDetails
      ),
      Json.obj(
        CompanyDetailsId.toString -> companyDetails
      ),
      Json.obj(
        PartnershipDetailsId.toString -> partnershipDetails
      ),
      Json.obj(
        EstablisherDetailsId.toString -> deletedEstablisher
      )
    )
  )

  private def onwardRoute = controllers.routes.IndexController.onPageLoad()

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new ConfirmDeleteEstablisherController(
      frontendAppConfig,
      messagesApi,
      FakeUserAnswersService,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider
    )

  private def viewAsString(hintText:Option[String] = None,
                           estName:String = establisherName,
                           postCall:Call = postCall) = confirmDeleteEstablisher(
    frontendAppConfig,
    form,
    estName,
    hintText,
    postCall,
    None
  )(fakeRequest, messages).toString

}
