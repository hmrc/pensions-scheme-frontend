/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.register.trustees.partnership

import controllers.ControllerSpecBase
import controllers.actions._
import forms.HasPAYEFormProvider
import identifiers.register.trustees.TrusteesId
import identifiers.register.trustees.partnership.{PartnershipDetailsId, PartnershipEnterPAYEId, PartnershipHasPAYEId}
import models.{Index, NormalMode, PartnershipDetails}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.test.Helpers.{contentAsString, redirectLocation, status, _}
import services.FakeUserAnswersService
import utils.FakeNavigator
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.hasReferenceNumber

class PartnershipHasPAYEControllerSpec extends ControllerSpecBase {
  private val schemeName = None
  private def onwardRoute = controllers.routes.IndexController.onPageLoad
  private val formProvider = new HasPAYEFormProvider()
  private val form = formProvider("messages__partnershipHasPaye__error__required","test partnership name")
  private val index = Index(0)
  private val srn = None
  private val postCall = controllers.register.trustees.partnership.routes.PartnershipHasPAYEController.onSubmit(NormalMode, index, OptionalSchemeReferenceNumber(srn))
  private val viewModel = CommonFormWithHintViewModel(
    postCall,
    title = Message("messages__hasPAYE", Message("messages__thePartnership").resolve),
    heading = Message("messages__hasPAYE", "test partnership name"),
    hint = Some(Message("messages__hasPaye__p1")),
    formFieldName = Some("hasPaye")
  )

  private val getDataWithPaye: FakeDataRetrievalAction = new FakeDataRetrievalAction(
    Some(Json.obj(
      TrusteesId.toString -> Json.arr(
        Json.obj(
          PartnershipDetailsId.toString -> PartnershipDetails("test partnership name"),
          PartnershipHasPAYEId.toString -> true,
          PartnershipEnterPAYEId.toString -> "9999999999"
        )
      )
    ))
  )

  private val view = injector.instanceOf[hasReferenceNumber]

  private def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryTrusteePartnership): PartnershipHasPAYEController =
    new PartnershipHasPAYEController(
      frontendAppConfig,
      messagesApi,
      FakeUserAnswersService,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      FakeAllowAccessProvider(),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider,
      controllerComponents,
      view
    )

  private def viewAsString(form: Form[_] = form) = view(form, viewModel, schemeName)(fakeRequest, messages).toString

  "PartnershipHasPAYEController" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad( NormalMode, index, EmptyOptionalSchemeReferenceNumber)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to the next page when valid data is submitted for true" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("hasPaye", "true"))

      val result = controller().onSubmit( NormalMode, index, EmptyOptionalSchemeReferenceNumber)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
      FakeUserAnswersService.verify(PartnershipHasPAYEId(index), true)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("hasPaye", "invalid value"))
      val boundForm = form.bind(Map("hasPaye" -> "invalid value"))

      val result = controller().onSubmit( NormalMode, index, EmptyOptionalSchemeReferenceNumber)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "if user changes answer from yes to no then clean up should take place on utr number" in {
      FakeUserAnswersService.reset()
      val postRequest = fakeRequest.withFormUrlEncodedBody(("hasPaye", "false"))
      val result = controller(getDataWithPaye).onSubmit( NormalMode, index, EmptyOptionalSchemeReferenceNumber)(postRequest)

      status(result) mustBe SEE_OTHER
      FakeUserAnswersService.verify(PartnershipHasPAYEId(index), false)
      FakeUserAnswersService.verifyNot(PartnershipEnterPAYEId(index))
    }

  }
}
