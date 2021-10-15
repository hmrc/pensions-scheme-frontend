/*
 * Copyright 2021 HM Revenue & Customs
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

import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.establishers.partnership.partner.ConfirmDeletePartnerFormProvider
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.partnership.PartnershipDetailsId
import identifiers.register.establishers.partnership.partner.PartnerNameId
import models.person.PersonName
import models.{Index, NormalMode, PartnershipDetails, UpdateMode}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.FakeUserAnswersService
import utils.FakeNavigator
import views.html.register.establishers.partnership.partner.confirmDeletePartner

class ConfirmDeletePartnerControllerSpec extends ControllerSpecBase {

  import ConfirmDeletePartnerControllerSpec._

  "ConfirmDeletePartner Controller" must {
    "return OK and the correct view for a GET" in {
      val data = new FakeDataRetrievalAction(Some(testData()))
      val result = controller(data).onPageLoad(NormalMode, establisherIndex, partnerIndex, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to already deleted view for a GET if the partner was already deleted" in {
      val data = new FakeDataRetrievalAction(Some(testData(partnerDeleted)))
      val result = controller(data).onPageLoad(NormalMode, establisherIndex, partnerIndex, None)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(routes.AlreadyDeletedController.onPageLoad(NormalMode, establisherIndex, partnerIndex, None).url)
    }

    "return a Bad Request on POST and errors when invalid data is submitted" in {
      val data = new FakeDataRetrievalAction(Some(testData()))
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))

      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller(data).onSubmit(NormalMode, establisherIndex, partnerIndex, None)(postRequest)
      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "delete the partner on a POST" in {
      val data = new FakeDataRetrievalAction(Some(testData()))
      val result = controller(data).onSubmit(NormalMode, establisherIndex, partnerIndex, None)(postRequest)

      status(result) mustBe SEE_OTHER
      FakeUserAnswersService.verify(PartnerNameId(establisherIndex, partnerIndex), partnerName.copy(isDeleted = true))
    }


    "never delete the partner on a POST if selected No" in {
      FakeUserAnswersService.reset()
      val data = new FakeDataRetrievalAction(Some(testData()))
      val result = controller(data).onSubmit(NormalMode, establisherIndex, partnerIndex, None)(postRequestForCancle)

      status(result) mustBe SEE_OTHER
      FakeUserAnswersService.verifyNot(PartnerNameId(establisherIndex, partnerIndex))
    }

    "never delete the partner on a POST if selected No in UpdateMode" in {
      FakeUserAnswersService.reset()
      val data = new FakeDataRetrievalAction(Some(testData()))
      val result = controller(data).onSubmit(UpdateMode, establisherIndex, partnerIndex, Some("S123"))(postRequestForCancle)

      status(result) mustBe SEE_OTHER
      FakeUserAnswersService.verifyNot(PartnerNameId(establisherIndex, partnerIndex))
    }

    "redirect to the next page on a successful POST" in {
      val data = new FakeDataRetrievalAction(Some(testData()))
      val result = controller(data).onSubmit(NormalMode, establisherIndex, partnerIndex, None)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode, establisherIndex, partnerIndex, None)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val result = controller(dontGetAnyData).onSubmit(NormalMode, establisherIndex, partnerIndex, None)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }
  }

}

object ConfirmDeletePartnerControllerSpec extends ControllerSpecBase {

  private val establisherIndex = Index(0)
  private val partnerIndex = Index(0)
  private val partnershipName = "My Partnership Ltd"

  private val formProvider = new ConfirmDeletePartnerFormProvider()
  private val partnerName = PersonName("John", "Doe")

  private val form = formProvider.apply(partnerName.fullName)
  private lazy val postCall = routes.ConfirmDeletePartnerController.onSubmit(NormalMode, establisherIndex, partnerIndex, None)

  private val postRequest: FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest().withFormUrlEncodedBody(("value", "true"))
  private val postRequestForCancle: FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest().withFormUrlEncodedBody(("value", "false"))

  private def testData(partners: PersonName = partnerName) = Json.obj(
    EstablishersId.toString -> Json.arr(
      Json.obj(
        PartnershipDetailsId.toString -> PartnershipDetails(partnershipName),
        "partner" -> Json.arr(
          Json.obj(
            PartnerNameId.toString ->
              partners
          )
        )
      )
    )
  )

  val partnerDeleted: PersonName = partnerName.copy(isDeleted = true)

  private val view = injector.instanceOf[confirmDeletePartner]

  private def onwardRoute = controllers.routes.IndexController.onPageLoad

  private def controller(dataRetrievalAction: DataRetrievalAction) =
    new ConfirmDeletePartnerController(
      frontendAppConfig,
      messagesApi,
      FakeUserAnswersService,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      formProvider,
      controllerComponents,
      view
    )

  private def viewAsString(form: Form[_] = form) = view(
    form,
    "John Doe",
    postCall,
    None
  )(fakeRequest, messages).toString

}
