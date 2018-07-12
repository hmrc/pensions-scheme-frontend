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

package controllers.register.establishers

import connectors.FakeDataCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.establishers.AddEstablisherFormProvider
import identifiers.register.SchemeDetailsId
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.establishers.individual.EstablisherDetailsId
import models.person.PersonDetails
import models.register.SchemeType.SingleTrust
import models.register.{Establisher, EstablisherCompanyEntity, EstablisherIndividualEntity, SchemeDetails}
import models.{CompanyDetails, NormalMode}
import org.joda.time.LocalDate
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import utils.FakeNavigator
import views.html.register.establishers.addEstablisher

class AddEstablisherControllerSpec extends ControllerSpecBase {

  import AddEstablisherControllerSpec._

  "AddEstablisher Controller" must {

    "return OK and the correct view for a GET when scheme name is present" in {
      val result = controller().onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "not populate the view on a GET when the question has previously been answered" in {
      val getRelevantData = individualEstablisherDataRetrieval

      val result = controller(getRelevantData).onPageLoad(NormalMode)(fakeRequest)
      contentAsString(result) mustBe viewAsString(form, Seq(johnDoe))
    }

    "populate the view with establishers when they exist" in {
      val establishersAsEntities = Seq(johnDoe, testLtd)
      val getRelevantData = establisherWithDeletedDataRetrieval
      val result = controller(getRelevantData).onPageLoad(NormalMode)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form, establishersAsEntities)
    }

    "exclude the deleted establishers from the list" in {
      val getRelevantData = establisherWithDeletedDataRetrieval
      val result = controller(getRelevantData).onPageLoad(NormalMode)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form, Seq(johnDoe, testLtd))
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))

      val result = controller().onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "redirect to the next page when no establishers exist and the user submits" in {
      val result = controller().onSubmit(NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))
      val result = controller(dontGetAnyData).onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller().onSubmit(NormalMode)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }
  }
}

object AddEstablisherControllerSpec extends AddEstablisherControllerSpec {

  private def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  private val schemeName = "Test Scheme Name"

  private val formProvider = new AddEstablisherFormProvider()
  private val form = formProvider(Seq.empty)

  protected def fakeNavigator() = new FakeNavigator(desiredRoute = onwardRoute)

  private def controller(dataRetrievalAction: DataRetrievalAction = getMandatorySchemeName): AddEstablisherController =
    new AddEstablisherController(
      frontendAppConfig,
      messagesApi,
      FakeDataCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider
    )

  private def viewAsString(form: Form[_] = form, allEstablishers: Seq[Establisher[_]] = Seq.empty): String =
    addEstablisher(
      frontendAppConfig,
      form,
      NormalMode,
      allEstablishers,
      schemeName
    )(fakeRequest, messages).toString

  private val day = LocalDate.now().getDayOfMonth
  private val month = LocalDate.now().getMonthOfYear
  private val year = LocalDate.now().getYear - 20

  private val personDetails = PersonDetails("John", None, "Doe", new LocalDate(year, month, day))
  private val johnDoe = EstablisherIndividualEntity(
    id = EstablisherDetailsId(0),
    name = "John Doe",
    isDeleted = false,
    isCompleted = false
  )

  private val companyDetails = CompanyDetails("Test Ltd", None, None)
  private val testLtd = EstablisherCompanyEntity(
    id = CompanyDetailsId(1),
    name = "Test Ltd",
    isDeleted = false,
    isCompleted = false
  )

  private val deletedEstablisher = personDetails.copy(isDeleted = true)

  private def individualEstablisherDataRetrieval: FakeDataRetrievalAction = {
    val validData = Json.obj(
      SchemeDetailsId.toString -> SchemeDetails(schemeName, SingleTrust),
      EstablishersId.toString -> Json.arr(
        Json.obj(
          EstablisherDetailsId.toString -> personDetails
        )
      )
    )
    new FakeDataRetrievalAction(Some(validData))
  }

  private def establisherWithDeletedDataRetrieval: FakeDataRetrievalAction = {
    val validData = Json.obj(
      SchemeDetailsId.toString -> SchemeDetails(schemeName, SingleTrust),
      EstablishersId.toString -> Json.arr(
        Json.obj(
          EstablisherDetailsId.toString -> personDetails
        ),
        Json.obj(
          CompanyDetailsId.toString -> companyDetails
        ),
        Json.obj(
          EstablisherDetailsId.toString -> deletedEstablisher
        )
      )
    )
    new FakeDataRetrievalAction(Some(validData))
  }

}
