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

package controllers.register.establishers.partnership

import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.AddPartnersFormProvider
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.partnership.partner.{PartnerDetailsId, PartnerNameId}
import identifiers.register.establishers.partnership.{AddPartnersId, PartnershipDetailsId}
import models.person.{PersonDetails, PersonName}
import models.register.PartnerEntity
import models.{Index, NormalMode, PartnershipDetails}
import navigators.Navigator
import org.joda.time.LocalDate
import play.api.data.Form
import play.api.libs.json._
import play.api.mvc.Call
import play.api.test.Helpers._
import utils.{FakeNavigator, UserAnswers}
import views.html.register.addPartners

class AddPartnersControllerSpec extends ControllerSpecBase {

  appRunning()

  private def onwardRoute = controllers.routes.IndexController.onPageLoad()

  private val formProvider = new AddPartnersFormProvider()
  private val form = formProvider()

  private def fakeNavigator() = new FakeNavigator(desiredRoute = onwardRoute)

  val firstIndex = Index(0)

  private val establisherIndex = 0

  private def controller(
                          dataRetrievalAction: DataRetrievalAction = getEmptyData,
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
      formProvider
    )

  private val postUrl: Call = routes.AddPartnersController.onSubmit(NormalMode, establisherIndex, None)

  private def viewAsString(form: Form[_] = form, partners: Seq[PartnerEntity] = Nil) =
    addPartners(
      frontendAppConfig,
      form,
      partners,
      postUrl,
      None,
      false,
      NormalMode,
      None
    )(fakeRequest, messages).toString

  private val partnershipName = "MyCo Ltd"

  // scalastyle:off magic.number
  private val johnDoeNonHns = PersonDetails("John", None, "Doe", new LocalDate(1862, 6, 9))
  private val joeBloggsNonHns = PersonDetails("Joe", None, "Bloggs", new LocalDate(1969, 7, 16))
  private val johnDoe = PersonName("John", "Doe")
  private val joeBloggs = PersonName("Joe", "Bloggs")
  // scalastyle:on magic.number

  private val maxPartners = frontendAppConfig.maxPartners

  private def validData(partners: PersonDetails*) = {
    Json.obj(
      EstablishersId.toString -> Json.arr(
        Json.obj(
          PartnershipDetailsId.toString -> PartnershipDetails(partnershipName, false),
          "partner" -> partners.map(d => Json.obj(PartnerDetailsId.toString -> Json.toJson(d)))
        )
      )
    )
  }

  "AddPartners Controller" must {

    "return OK and the correct view for a GET" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData()))
      val result = controller(getRelevantData).onPageLoad(NormalMode, establisherIndex, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "not populate the view on a GET when the question has previously been answered" in {
      UserAnswers(validData(johnDoeNonHns))
        .set(AddPartnersId(firstIndex))(true)
        .map { userAnswers =>
          val getRelevantData = new FakeDataRetrievalAction(Some(userAnswers.json))
          val result = controller(getRelevantData).onPageLoad(NormalMode, establisherIndex, None)(fakeRequest)

          contentAsString(result) mustBe viewAsString(form,
            Seq(PartnerEntity(PartnerNameId(0, 0), johnDoeNonHns.fullName, isDeleted = false, isCompleted = false, isNewEntity = false, 1)))
        }
    }

    "redirect to the next page when no partners exist and the user submits" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData()))
      val result = controller(getRelevantData).onSubmit(NormalMode, establisherIndex, None)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "redirect to the next page when less than maximum partners exist and valid data is submitted" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData(johnDoeNonHns)))
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))
      val result = controller(getRelevantData).onSubmit(NormalMode, establisherIndex, None)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when less than maximum partners exist and invalid data is submitted" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData(johnDoeNonHns)))
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "meh"))
      val boundForm = form.bind(Map("value" -> "meh"))
      val result = controller(getRelevantData).onSubmit(NormalMode, establisherIndex, None)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm,
        Seq(PartnerEntity(PartnerNameId(0, 0), johnDoeNonHns.fullName, isDeleted = false, isCompleted = false, isNewEntity = false, 1)))
    }

    "redirect to the next page when maximum partners exist and the user submits" in {
      val partners = Seq.fill(maxPartners)(johnDoeNonHns)
      val getRelevantData = new FakeDataRetrievalAction(Some(validData(partners: _*)))
      val result = controller(getRelevantData).onSubmit(NormalMode, establisherIndex, None)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "populate the view with partners when they exist and toggle is off" in {
      val partners = Seq(johnDoeNonHns, joeBloggsNonHns)
      val partnersViewModel = Seq(
        PartnerEntity(
          PartnerNameId(0, 0), johnDoeNonHns.fullName, isDeleted = false, isCompleted = false, isNewEntity = false, 2),
        PartnerEntity(
          PartnerNameId(0, 1), joeBloggsNonHns.fullName, isDeleted = false, isCompleted = false, isNewEntity = false, 2))
      val getRelevantData = new FakeDataRetrievalAction(Some(validData(partners: _*)))
      val result = controller(getRelevantData).onPageLoad(NormalMode, establisherIndex, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(form, partnersViewModel)
    }

    "populate the view with partners when they exist and toggle is on" in {
      val partners = Seq(johnDoeNonHns, joeBloggsNonHns)
      val partnersViewModel = Seq(
        PartnerEntity(
          PartnerNameId(0, 0), johnDoeNonHns.fullName, isDeleted = false, isCompleted = false, isNewEntity = false, 2),
        PartnerEntity(
          PartnerNameId(0, 1), joeBloggsNonHns.fullName, isDeleted = false, isCompleted = false, isNewEntity = false, 2))
      val getRelevantData = new FakeDataRetrievalAction(Some(validData(partners: _*)))
      val result = controller(getRelevantData).onPageLoad(NormalMode, establisherIndex, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(form, partnersViewModel)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode, 0, None)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))
      val result = controller(dontGetAnyData).onSubmit(NormalMode, 0, None)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }

}
