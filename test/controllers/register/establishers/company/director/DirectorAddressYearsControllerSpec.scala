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

package controllers.register.establishers.company.director

import controllers.ControllerSpecBase
import controllers.actions._
import forms.address.AddressYearsFormProvider
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.company.director.{DirectorAddressYearsId, DirectorNameId}
import models.person.PersonName
import models.{AddressYears, Index, NormalMode}
import play.api.data.Form
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import play.api.test.Helpers._
import services.FakeUserAnswersService

import utils.FakeNavigator
import viewmodels.Message
import viewmodels.address.AddressYearsViewModel
import views.html.address.addressYears

//scalastyle:off magic.number

class DirectorAddressYearsControllerSpec extends ControllerSpecBase {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val formProvider = new AddressYearsFormProvider()
  val form = formProvider("")
  val establisherIndex = Index(0)
  val directorIndex = Index(0)
  val invalidIndex = Index(10)
  val director = PersonName("first", "last")

  private val view = injector.instanceOf[addressYears]

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData): DirectorAddressYearsController =
    new DirectorAddressYearsController(
      frontendAppConfig,
      FakeUserAnswersService,
      new FakeNavigator(desiredRoute = onwardRoute),
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      controllerComponents,
      view
    )

  private def viewAsString(form: Form[_] = form) =
    view(
      form,
      AddressYearsViewModel(
        postCall = routes.DirectorAddressYearsController.onSubmit(NormalMode, establisherIndex, directorIndex, None),
        title = Message("messages__director_address_years__title", Message("messages__common__address_years__director")),
        heading = Message("messages__director_address_years__heading", director.fullName),
        legend = Message("messages__director_address_years__heading", director.fullName),
        subHeading = Some(director.fullName)
      ),
      None
    )(fakeRequest, messages).toString

  val validData: JsObject = Json.obj(
    EstablishersId.toString -> Json.arr(
      Json.obj(
        "director" -> Json.arr(
          Json.obj(
            DirectorNameId.toString -> director
          )
        )
      )
    )
  )

  "CompanyDirectorAddressYears Controller" must {

    "return OK and the correct view for a GET" in {

      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val result = controller(getRelevantData).onPageLoad(NormalMode, establisherIndex, directorIndex, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val validData = Json.obj(
        EstablishersId.toString -> Json.arr(
          Json.obj(
            "director" -> Json.arr(
              Json.obj(
                DirectorAddressYearsId.toString -> AddressYears.options.head.value.toString,
                DirectorNameId.toString -> director
              )
            )
          )
        )
      )

      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val result = controller(getRelevantData).onPageLoad(NormalMode, establisherIndex, directorIndex, None)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(AddressYears.values.head))
    }

    "redirect to session expired from a GET when the index is invalid" in {

      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val result = controller(getRelevantData).onPageLoad(NormalMode, establisherIndex, invalidIndex, None)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }

    "redirect to the next page when valid data is submitted" in {

      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", AddressYears.options.head.value))
      val result = controller(getRelevantData).onSubmit(NormalMode, establisherIndex, directorIndex, None)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))
      val result = controller(getRelevantData).onSubmit(NormalMode, establisherIndex, directorIndex, None)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode, establisherIndex, directorIndex, None)(fakeRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", AddressYears.options.head.value))
      val result = controller(dontGetAnyData).onSubmit(NormalMode, establisherIndex, directorIndex, None)(postRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }
  }
}
