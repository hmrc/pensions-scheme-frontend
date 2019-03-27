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

package controllers.register.trustees.individual

import connectors.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.trustees.individual.TrusteeNinoFormProvider
import identifiers.register.trustees.individual.{TrusteeDetailsId, TrusteeNinoId}
import models.person.PersonDetails
import models.{Index, Nino, NormalMode}
import org.joda.time.LocalDate
import play.api.data.Form
import play.api.libs.json.{JsError, JsResultException, JsSuccess}
import play.api.test.Helpers._
import utils.{FakeNavigator, UserAnswers}
import views.html.register.trustees.individual.trusteeNino

class TrusteeNinoControllerSpec extends ControllerSpecBase {

  import TrusteeNinoControllerSpec._

  "TrusteeNino Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller(trusteeData).onPageLoad(mode, index, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "return OK and the correct view when the question has already been answered" in {
      val answer = Nino.Yes(nino)
      val filledForm = form.fill(answer)
      val result = controller(trusteeAndAnswerData(answer)).onPageLoad(mode, index, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(filledForm)
    }

    "redirect to Session Expired on a GET when no cached data exists" in {
      val result = controller(dontGetAnyData).onPageLoad(mode, index, None)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to the next page when valid data is submitted" in {
      val request = fakeRequest.withFormUrlEncodedBody(("nino.hasNino", "true"), ("nino.nino", nino))
      val result = controller().onSubmit(mode, index, None)(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "save the asnwer when valid data is submitted" in {
      val request = fakeRequest.withFormUrlEncodedBody(("nino.hasNino", "true"), ("nino.nino", nino))
      val result = controller().onSubmit(mode, index, None)(request)

      status(result) mustBe SEE_OTHER
      FakeUserAnswersCacheConnector.verify[Nino, TrusteeNinoId](TrusteeNinoId(index), Nino.Yes(nino))
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val errorForm = form.bind(Map.empty[String, String])
      assume(errorForm.errors.nonEmpty)

      val result = controller(trusteeData).onSubmit(mode, index, None)(fakeRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(errorForm)
    }

    "redirect to Session Expired on a POST when no cached data exists" in {
      val result = controller(dontGetAnyData).onSubmit(mode, index, None)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

  }

}

object TrusteeNinoControllerSpec extends ControllerSpecBase {

  private val mode = NormalMode
  private val index = Index(0)
  private val nino = "AB123456C"

  private val trustee = PersonDetails(
    "Joe",
    None,
    "Bloggs",
    LocalDate.now()
  )

  private val onwardRoute = controllers.routes.IndexController.onPageLoad()
  private val fakeNavigator = new FakeNavigator(desiredRoute = onwardRoute)

  private val form: Form[Nino] = new TrusteeNinoFormProvider()()

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new TrusteeNinoController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      fakeNavigator,
      FakeUserAnswersCacheConnector
    )
  val submitUrl = controllers.register.trustees.individual.routes.TrusteeNinoController.onSubmit(NormalMode, index, None)

  private def viewAsString(form: Form[Nino] = form) =
    trusteeNino(
      frontendAppConfig,
      form,
      mode,
      index,
      None, submitUrl
    )(fakeRequest, messages).toString

  private def trusteeUserAnswers: UserAnswers = {
    UserAnswers().set(TrusteeDetailsId(index))(trustee) match {
      case JsSuccess(userAnswers, _) => userAnswers
      case JsError(errors) => throw JsResultException(errors)
    }
  }

  private def trusteeData: DataRetrievalAction = {
    new FakeDataRetrievalAction(Some(trusteeUserAnswers.json))
  }

  private def trusteeAndAnswerData(answer: Nino): DataRetrievalAction = {
    trusteeUserAnswers.set(TrusteeNinoId(index))(answer) match {
      case JsSuccess(userAnswers, _) => new FakeDataRetrievalAction(Some(userAnswers.json))
      case JsError(errors) => throw JsResultException(errors)
    }
  }

}
