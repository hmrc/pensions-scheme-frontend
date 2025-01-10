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

package controllers.register.establishers.partnership

import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.AddPartnersFormProvider
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.partnership.partner.PartnerNameId
import identifiers.register.establishers.partnership.{AddPartnersId, PartnershipDetailsId}
import models.FeatureToggleName.SchemeRegistration
import models.person.PersonName
import models.register.PartnerEntity
import models.{EmptyOptionalSchemeReferenceNumber, FeatureToggle, Index, NormalMode, PartnershipDetails}
import navigators.Navigator
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.libs.json._
import play.api.mvc.Call
import play.api.test.Helpers._
import services.FeatureToggleService
import utils.{FakeNavigator, UserAnswers}
import views.html.register.addPartners

import scala.concurrent.Future

class AddPartnersControllerSpec extends ControllerSpecBase with BeforeAndAfterEach with MockitoSugar{

  appRunning()

  private def onwardRoute = controllers.routes.IndexController.onPageLoad

  private val formProvider = new AddPartnersFormProvider()
  private val form = formProvider()

  private def fakeNavigator() = new FakeNavigator(desiredRoute = onwardRoute)

  val firstIndex = Index(0)

  private val establisherIndex = 0

  private val view = injector.instanceOf[addPartners]
  private val mockFeatureToggleService = mock[FeatureToggleService]

  private def controller(
                          dataRetrievalAction: DataRetrievalAction,
                          navigator: Navigator = fakeNavigator()
                        ) =
    new AddPartnersController(
      frontendAppConfig,
      messagesApi,
      navigator,
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      formProvider,
      controllerComponents,
      view,
      mockFeatureToggleService
    )

  private val postUrl: Call = routes.AddPartnersController.onSubmit(NormalMode, establisherIndex, EmptyOptionalSchemeReferenceNumber)

  private def viewAsString(form: Form[_] = form, completePartners: Seq[PartnerEntity] = Nil, incompletePartners: Seq[PartnerEntity] = Nil) =
    view(
      form,
      completePartners,
      incompletePartners,
      postUrl,
      None,
      false,
      NormalMode,
      EmptyOptionalSchemeReferenceNumber
    )(fakeRequest, messages).toString

  private val partnershipName = "MyCo Ltd"

  // scalastyle:off magic.number
  private val johnDoe = PersonName("John", "Doe")
  private val joeBloggs = PersonName("Joe", "Bloggs")
  // scalastyle:on magic.number

  private val maxPartners = frontendAppConfig.maxPartners

  private def validData(partners: PersonName*) = {
    Json.obj(
      EstablishersId.toString -> Json.arr(
        Json.obj(
          PartnershipDetailsId.toString -> PartnershipDetails(partnershipName),
          "partner" -> partners.map(d => Json.obj(PartnerNameId.toString -> Json.toJson(d)))
        )
      )
    )
  }

  override def beforeEach(): Unit = {
    reset(mockFeatureToggleService)
    when(mockFeatureToggleService.get(any())(any(), any()))
      .thenReturn(Future.successful(FeatureToggle(SchemeRegistration, true)))
  }

  "AddPartners Controller" must {

    "return OK and the correct view for a GET" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData()))
      val result = controller(getRelevantData).onPageLoad(NormalMode, establisherIndex, EmptyOptionalSchemeReferenceNumber)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "not populate the view on a GET when the question has previously been answered" in {
      UserAnswers(validData(johnDoe))
        .set(AddPartnersId(firstIndex))(true)
        .map { userAnswers =>
          val getRelevantData = new FakeDataRetrievalAction(Some(userAnswers.json))
          val result = controller(getRelevantData).onPageLoad(NormalMode, establisherIndex, EmptyOptionalSchemeReferenceNumber)(fakeRequest)

          contentAsString(result) mustBe viewAsString(form,
            incompletePartners = Seq(PartnerEntity(PartnerNameId(0, 0), johnDoe.fullName, isDeleted = false, isCompleted = false, isNewEntity = false, 1)))
        }
    }

    "redirect to the next page when no partners exist and the user submits" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData()))
      val result = controller(getRelevantData).onSubmit(NormalMode, establisherIndex, EmptyOptionalSchemeReferenceNumber)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "redirect to the next page when less than maximum partners exist and valid data is submitted" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData(johnDoe)))
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))
      val result = controller(getRelevantData).onSubmit(NormalMode, establisherIndex, EmptyOptionalSchemeReferenceNumber)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when less than maximum partners exist and invalid data is submitted" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData(johnDoe)))
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "meh"))
      val boundForm = form.bind(Map("value" -> "meh"))
      val result = controller(getRelevantData).onSubmit(NormalMode, establisherIndex, EmptyOptionalSchemeReferenceNumber)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm,
        incompletePartners = Seq(PartnerEntity(PartnerNameId(0, 0), johnDoe.fullName, isDeleted = false, isCompleted = false, isNewEntity = false, 1)))
    }

    "redirect to the next page when maximum partners exist and the user submits" in {
      val partners = Seq.fill(maxPartners)(johnDoe)
      val getRelevantData = new FakeDataRetrievalAction(Some(validData(partners: _*)))
      val result = controller(getRelevantData).onSubmit(NormalMode, establisherIndex, EmptyOptionalSchemeReferenceNumber)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "populate the view with partners when they exist" in {
      val partners = Seq(johnDoe, joeBloggs)
      val partnersViewModel = Seq(
        PartnerEntity(
          PartnerNameId(0, 0), johnDoe.fullName, isDeleted = false, isCompleted = false, isNewEntity = false, 2),
        PartnerEntity(
          PartnerNameId(0, 1), joeBloggs.fullName, isDeleted = false, isCompleted = false, isNewEntity = false, 2))
      val getRelevantData = new FakeDataRetrievalAction(Some(validData(partners: _*)))
      val result = controller(getRelevantData).onPageLoad(NormalMode, establisherIndex, EmptyOptionalSchemeReferenceNumber)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(form, incompletePartners = partnersViewModel)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode, 0, EmptyOptionalSchemeReferenceNumber)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))
      val result = controller(dontGetAnyData).onSubmit(NormalMode, 0, EmptyOptionalSchemeReferenceNumber)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }
  }

}
