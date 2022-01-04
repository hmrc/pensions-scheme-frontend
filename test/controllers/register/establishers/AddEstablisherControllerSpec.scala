/*
 * Copyright 2022 HM Revenue & Customs
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

import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.establishers.AddEstablisherFormProvider
import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.establishers.individual.EstablisherNameId
import identifiers.register.establishers.{EstablisherKindId, EstablishersId, IsEstablisherNewId}
import models.person.PersonName
import models.register.establishers.EstablisherKind
import models.register.{Establisher, EstablisherCompanyEntity, EstablisherIndividualEntity}
import models.{CompanyDetails, NormalMode}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import utils.FakeNavigator
import views.html.register.establishers.addEstablisher

class AddEstablisherControllerSpec extends ControllerSpecBase {

  import AddEstablisherControllerSpec._


  "AddEstablisher Controller" must {

    "continue button should be enabled" in {
      val establishersAsEntities = Seq(johnDoe, testLtd)
      val getRelevantData = establisherWithDeletedDataRetrieval
      val result = controller(getRelevantData).onPageLoad(NormalMode, None)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form, establishersAsEntities)
    }

    "return OK and the correct view for a GET when scheme name is present" in {
      val result = controller().onPageLoad(NormalMode, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "not populate the view on a GET when the question has previously been answered" in {
      val getRelevantData = individualEstablisherDataRetrieval

      val result = controller(getRelevantData).onPageLoad(NormalMode, None)(fakeRequest)
      contentAsString(result) mustBe viewAsString(form, Seq(johnDoe))
    }

    "populate the view with establishers when they exist and continue button should be disabled" in {
      val establishersAsEntities = Seq(johnDoe, testLtd)
      val getRelevantData = establisherWithDeletedDataRetrieval
      val result = controller(getRelevantData).onPageLoad(NormalMode, None)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form, establishersAsEntities)
    }

    "exclude the deleted establishers from the list" in {
      val getRelevantData = establisherWithDeletedDataRetrieval
      val result = controller(getRelevantData).onPageLoad(NormalMode, None)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form, Seq(johnDoe, testLtd))
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))

      val result = controller().onSubmit(NormalMode, None)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "redirect to the next page when no establishers exist and the user submits" in {
      val result = controller().onSubmit(NormalMode, None)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode, None)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))
      val result = controller(dontGetAnyData).onSubmit(NormalMode, None)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller().onSubmit(NormalMode, None)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }
  }
}

object AddEstablisherControllerSpec extends AddEstablisherControllerSpec {

  private def onwardRoute: Call = controllers.routes.IndexController.onPageLoad

  private val formProvider = new AddEstablisherFormProvider()
  private val form = formProvider(Seq.empty)

  protected def fakeNavigator() = new FakeNavigator(desiredRoute = onwardRoute)

  private val view = injector.instanceOf[addEstablisher]

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData): AddEstablisherController =
    new AddEstablisherController(
      frontendAppConfig,
      messagesApi,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      formProvider,
      controllerComponents,
      view
    )

  private def viewAsString(form: Form[_] = form, allEstablishers: Seq[Establisher[_]] = Seq.empty): String =
    view(
      form,
      NormalMode,
      allEstablishers,
      None,
      None
    )(fakeRequest, messages).toString

  private val personDetails = PersonName("John", "Doe")
  private val johnDoe = EstablisherIndividualEntity(
    EstablisherNameId(0),
    "John Doe",
    false,
    false,
    true,
    1
  )

  private val companyDetails = CompanyDetails("Test Ltd")
  private val testLtd = EstablisherCompanyEntity(
    CompanyDetailsId(1),
    "Test Ltd",
    false,
    false,
    true,
    1
  )

  private val deletedEstablisher = personDetails.copy(isDeleted = true)

  private def individualEstablisherDataRetrieval: FakeDataRetrievalAction = {
    val validData = Json.obj(
      EstablishersId.toString -> Json.arr(
        Json.obj(
          EstablisherNameId.toString -> personDetails,
          IsEstablisherNewId.toString -> true,
          EstablisherKindId.toString -> EstablisherKind.Indivdual.toString
        )
      )
    )
    new FakeDataRetrievalAction(Some(validData))
  }

  private def establisherWithDeletedDataRetrieval: FakeDataRetrievalAction = {
    val validData = Json.obj(
      EstablishersId.toString -> Json.arr(
        Json.obj(
          EstablisherNameId.toString -> personDetails,
          IsEstablisherNewId.toString -> true,
          EstablisherKindId.toString -> EstablisherKind.Indivdual.toString
        ),
        Json.obj(
          CompanyDetailsId.toString -> companyDetails,
          IsEstablisherNewId.toString -> true,
          EstablisherKindId.toString -> EstablisherKind.Company.toString
        ),
        Json.obj(
          EstablisherNameId.toString -> deletedEstablisher,
          IsEstablisherNewId.toString -> true,
          EstablisherKindId.toString -> EstablisherKind.Indivdual.toString
        )
      )
    )
    new FakeDataRetrievalAction(Some(validData))
  }

}
