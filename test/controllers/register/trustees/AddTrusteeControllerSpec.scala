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

package controllers.register.trustees

import controllers.ControllerSpecBase
import controllers.actions.*
import forms.register.trustees.AddTrusteeFormProvider
import helpers.DataCompletionHelper
import identifiers.register.trustees.company.CompanyDetailsId
import identifiers.register.trustees.individual.TrusteeNameId
import identifiers.register.trustees.{IsTrusteeNewId, TrusteeKindId, TrusteesId}
import models.*
import models.person.PersonName
import models.register.*
import models.register.SchemeType.SingleTrust
import models.register.trustees.TrusteeKind
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.libs.json.*
import play.api.mvc.Call
import play.api.test.Helpers.*
import services.UserAnswersService
import utils.{FakeNavigator, UserAnswers}
import views.html.register.trustees.{addTrustee, addTrusteeOld}

import scala.concurrent.Future

class AddTrusteeControllerSpec extends ControllerSpecBase with DataCompletionHelper with BeforeAndAfterEach with MockitoSugar {
  appRunning()

  private lazy val trusteeCompanyA: TrusteeCompanyEntity = TrusteeCompanyEntity(
    CompanyDetailsId(0), "Trustee Company A", isDeleted = false, isCompleted = false, isNewEntity = true, 3, Some(SingleTrust.toString))
  private lazy val trusteeCompanyB: TrusteeCompanyEntity = TrusteeCompanyEntity(
    CompanyDetailsId(1), "Trustee Company B", isDeleted = false, isCompleted = false, isNewEntity = true, 3, Some(SingleTrust.toString))
  lazy val trusteeIndividual: TrusteeIndividualEntity = TrusteeIndividualEntity(
    TrusteeNameId(2), "Trustee Individual", isDeleted = false, isCompleted = false, isNewEntity = true, 3, Some(SingleTrust.toString))

  private lazy val allTrustees = Seq(trusteeCompanyA, trusteeCompanyB, trusteeIndividual)
  private val formProvider = new AddTrusteeFormProvider()
  private val form = formProvider()
  private val testAnswer = "true"

  def editTrusteeCompanyRoute(id: Int): String =
    controllers.register.trustees.company.routes.CompanyDetailsController.onPageLoad(NormalMode, id, EmptyOptionalSchemeReferenceNumber).url

  def deleteTrusteeRoute(id: Int, kind: TrusteeKind): String =
    controllers.register.trustees.routes.ConfirmDeleteTrusteeController.onPageLoad(NormalMode, id, kind, EmptyOptionalSchemeReferenceNumber).url

  private val view = injector.instanceOf[addTrustee]
  private val oldView = injector.instanceOf[addTrusteeOld]
  private val mockUserAnswersService = mock[UserAnswersService]

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData): AddTrusteeController = {
    new AddTrusteeController(
      frontendAppConfig,
      messagesApi,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      formProvider,
      controllerComponents,
      view,
      oldView,
      mockUserAnswersService
    )
  }

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad

  def viewAsString(form: Form[?] = form, trustees: Seq[Trustee[?]] = Seq.empty): String = {
    val completeTrustees = trustees.filter(_.isCompleted)
    val inCompleteTrustees = trustees.filterNot(_.isCompleted)
    view(form, NormalMode, completeTrustees, inCompleteTrustees, None, EmptyOptionalSchemeReferenceNumber)(fakeRequest, messages).toString
  }

  private def validData = {
    Json.obj(
      "schemeType" -> Json.obj("name" -> "single"),
      TrusteesId.toString -> Json.arr(
        Json.obj(
          TrusteeKindId.toString -> TrusteeKind.Company.toString,
          CompanyDetailsId.toString -> CompanyDetails("Trustee Company A"),
          IsTrusteeNewId.toString -> true
        ),
        Json.obj(
          TrusteeKindId.toString -> TrusteeKind.Company.toString,
          CompanyDetailsId.toString -> CompanyDetails("Trustee Company B"),
          IsTrusteeNewId.toString -> true
        ),
        Json.obj(
          TrusteeKindId.toString -> TrusteeKind.Individual.toString,
          TrusteeNameId.toString -> PersonName("Trustee", "Individual"),
          IsTrusteeNewId.toString -> true
        )
      )
    )
  }

  "AddTrustee Controller" must {

    "return view with button ENABLED when some trustees incomplete" in {
      val trusteeList: JsValue =
        setTrusteeCompletionStatus(isComplete = false, 1,
          setTrusteeCompletionStatus(isComplete = true, 0,
            UserAnswers()
              .set(TrusteeNameId(0))(PersonName("fistName", "lastName")).asOpt.value
              .set(TrusteeKindId(0))(TrusteeKind.Individual).asOpt.value
              .set(TrusteeNameId(1))(PersonName("fistName", "lastName")).asOpt.value
              .set(TrusteeKindId(1))(TrusteeKind.Individual).asOpt.value
          )
        ).json

      when(mockUserAnswersService.upsert(any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(trusteeList))

      val trusteeController: AddTrusteeController = controller(new FakeDataRetrievalAction(Some(trusteeList)))

      val result = trusteeController.onPageLoad(NormalMode, EmptyOptionalSchemeReferenceNumber)(fakeRequest)

      val view = asDocument(contentAsString(result))

      view.getElementById("submit").hasAttr("disabled") mustEqual false

    }

    "return view with button ENABLED when all trustees complete" in {
      val trusteeList: JsValue =
        setTrusteeCompletionStatus(isComplete = true, 1,
          setTrusteeCompletionStatus(isComplete = true, 0,
            UserAnswers()
              .set(TrusteeNameId(0))(PersonName("fistName", "lastName")).asOpt.value
              .set(TrusteeNameId(1))(PersonName("fistName", "lastName")).asOpt.value
          )
        ).json

      val trusteeController: AddTrusteeController = controller(new FakeDataRetrievalAction(Some(trusteeList)))

      val result = trusteeController.onPageLoad(NormalMode, EmptyOptionalSchemeReferenceNumber)(fakeRequest)

      val view = asDocument(contentAsString(result))

      view.getElementById("submit").hasAttr("disabled") mustEqual false
    }

    "return view with button ENABLED when at least one trustee is INCOMPLETE" in {

      val trusteeList: JsValue =
        setTrusteeCompletionStatus(isComplete = true, 1,
          setTrusteeCompletionStatus(isComplete = false, 0,
            UserAnswers()
              .set(TrusteeNameId(0))(PersonName("fistName", "lastName")).asOpt.value
              .set(TrusteeNameId(1))(PersonName("fistName", "lastName")).asOpt.value
          )
        ).json

      val trusteeController: AddTrusteeController = controller(new FakeDataRetrievalAction(Some(trusteeList)))

      val result = trusteeController.onPageLoad(NormalMode, EmptyOptionalSchemeReferenceNumber)(fakeRequest)

      val view = asDocument(contentAsString(result))

      view.getElementById("submit").hasAttr("disabled") mustEqual false
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", testAnswer))

      val result = controller().onSubmit(NormalMode, EmptyOptionalSchemeReferenceNumber)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "redirect to the next page when valid data is submitted and few trustees were previously added" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", testAnswer))

      val result = controller(getRelevantData).onSubmit(NormalMode, EmptyOptionalSchemeReferenceNumber)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))

      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller(getRelevantData).onSubmit(NormalMode, EmptyOptionalSchemeReferenceNumber)(postRequest)
      status(result) mustBe BAD_REQUEST

      contentAsString(result) mustBe viewAsString(boundForm, allTrustees)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode, EmptyOptionalSchemeReferenceNumber)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", testAnswer))
      val result = controller(dontGetAnyData).onSubmit(NormalMode, EmptyOptionalSchemeReferenceNumber)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }
  }
}
