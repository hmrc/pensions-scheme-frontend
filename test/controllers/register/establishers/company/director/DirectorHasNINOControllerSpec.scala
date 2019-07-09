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

package controllers.register.establishers.company.director

import controllers.ControllerSpecBase
import controllers.actions._
import forms.HasReferenceNumberFormProvider
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.establishers.company.director.{DirectorDetailsId, DirectorNameId}
import models.person.PersonDetails
import models.{CompanyDetails, Index, NormalMode}
import org.joda.time.LocalDate
import play.api.data.Form
import play.api.libs.json.Json
import play.api.test.Helpers._
import services.FakeUserAnswersService
import utils.FakeNavigator
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.hasReferenceNumber

class DirectorHasNINOControllerSpec extends ControllerSpecBase {
  private val schemeName = None
  private def onwardRoute = controllers.routes.IndexController.onPageLoad()
  val formProvider = new HasReferenceNumberFormProvider()
  val form = formProvider("error","test company name")
  val establisherIndex = Index(0)
  val directorIndex = Index(0)
  val srn = None
  val postCall = controllers.register.establishers.company.director.routes.DirectorHasNINOController.onSubmit(NormalMode, establisherIndex, directorIndex, srn)
  val viewModel = CommonFormWithHintViewModel(
    postCall,
    title = Message("messages__directorHasNino__title"),
    heading = Message("messages__directorHasNino__h1", "first middle last"),
    hint = None
  )

  override def getMandatoryEstablisherCompanyDirector: FakeDataRetrievalAction = new FakeDataRetrievalAction(
    Some(Json.obj(
      EstablishersId.toString -> Json.arr(
        Json.obj(
          CompanyDetailsId.toString ->
            CompanyDetails("test company name"),
          "director" -> Json.arr(
            Json.obj(
              DirectorNameId.toString -> PersonDetails("first", Some("middle"), "last",
                new LocalDate(1990, 2, 2))
            )
          )
        )
      )
    ))
  )

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisherCompanyDirector): DirectorHasNINOController =
    new DirectorHasNINOController(
      frontendAppConfig,
      messagesApi,
      FakeUserAnswersService,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      FakeAllowAccessProvider(),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider
    )

  private def viewAsString(form: Form[_] = form) = hasReferenceNumber(frontendAppConfig, form, viewModel, schemeName)(fakeRequest, messages).toString

  "DirectorHasNINOController" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode, establisherIndex, directorIndex, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }
//
//    "redirect to the next page when valid data is submitted for true" in {
//      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))
//
//      val result = controller().onSubmit(NormalMode, None, index)(postRequest)
//
//      status(result) mustBe SEE_OTHER
//      redirectLocation(result) mustBe Some(onwardRoute.url)
//      FakeUserAnswersService.verify(HasCompanyUTRId(index), true)
//    }
//
//    "return a Bad Request and errors when invalid data is submitted" in {
//      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
//      val boundForm = form.bind(Map("value" -> "invalid value"))
//
//      val result = controller().onSubmit(NormalMode, None, index)(postRequest)
//
//      status(result) mustBe BAD_REQUEST
//      contentAsString(result) mustBe viewAsString(boundForm)
//    }

  }
}

