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

package controllers

import config.FrontendAppConfig
import connectors.{UserAnswersCacheConnector, AddressLookupConnector}
import controllers.actions._
import forms.address.PostCodeLookupFormProvider
import models.NormalMode
import models.address.TolerantAddress
import navigators.Navigator
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.ArgumentMatchers
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.CSRFTokenHelper.addCSRFToken
import play.api.test.FakeRequest
import play.api.test.Helpers.{running, redirectLocation, OK, status, contentAsString, route, SEE_OTHER, _}
import services.{FakeUserAnswersService, UserAnswersService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.FakeNavigator
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel
import views.html.address.postcodeLookup

import scala.concurrent.Future

class AdviserPostcodeLookupControllerSpec extends ControllerSpecBase with MockitoSugar with ScalaFutures with OptionValues {

  def onwardRoute: Call = controllers.routes.AdviserAddressListController.onPageLoad(NormalMode)

  def manualInputCall: Call = routes.AdviserAddressController.onPageLoad(NormalMode)

  private def fakeAddress(postCode: String) = TolerantAddress(
    Some("Address Line 1"),
    Some("Address Line 2"),
    Some("Address Line 3"),
    Some("Address Line 4"),
    Some(postCode),
    Some("GB")
  )

  private val testAnswer = "AB12 1AB"

  val formProvider = new PostCodeLookupFormProvider()
  val form: Form[String] = formProvider()

  val fakeAddressLookupConnector: AddressLookupConnector = mock[AddressLookupConnector]
  implicit val hc: HeaderCarrier = mock[HeaderCarrier]
  private val view = injector.instanceOf[postcodeLookup]

  lazy val viewModel: PostcodeLookupViewModel = PostcodeLookupViewModel(
    postCall = routes.AdviserPostCodeLookupController.onSubmit(NormalMode),
    manualInputCall = manualInputCall,
    title = Message("messages__adviserPostCodeLookup__heading", Message("messages__theAdviser").resolve),
    heading = Message("messages__adviserPostCodeLookup__heading", "name"),
    subHeading = Some(Message("messages__adviserPostCodeLookupAddress__secondary"))
  )

  "Adviser Postcode Controller" must {

    "render postcodeLookop from GET request" in {
      val cacheConnector: UserAnswersCacheConnector = mock[UserAnswersCacheConnector]
      val addressConnector: AddressLookupConnector = mock[AddressLookupConnector]

      running(_.overrides(
        bind[FrontendAppConfig].to(frontendAppConfig),
        bind[UserAnswersCacheConnector].toInstance(cacheConnector),
        bind[AddressLookupConnector].toInstance(addressConnector),
        bind[AuthAction].to(FakeAuthAction),
        bind[DataRetrievalAction].to(getMandatoryWorkingKnowledgePerson),
        bind[Navigator].toInstance(new FakeNavigator(onwardRoute))
      )) {
        implicit app =>

          val request = addCSRFToken(FakeRequest(routes.AdviserPostCodeLookupController.onPageLoad(NormalMode))
            .withHeaders("Csrf-Token" -> "nocheck"))

          val result = route(app, request).value

          status(result) must be(OK)

          contentAsString(result).removeAllNonces() mustEqual view(
            form,
            viewModel,
            None
          )(request, messages).toString
      }
    }

    "redirect to next page on POST request" in {

  //    val call: Call = routes.AdviserPostCodeLookupController.onSubmit(NormalMode)

      val validPostcode = "ZZ1 1ZZ"

      when(fakeAddressLookupConnector.addressLookupByPostCode(ArgumentMatchers.eq(validPostcode))(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(
          Seq(fakeAddress(testAnswer)))
        )

      running(_.overrides(
        bind[FrontendAppConfig].to(frontendAppConfig),
        bind[MessagesApi].to(messagesApi),
        bind[UserAnswersService].toInstance(FakeUserAnswersService),
        bind[AddressLookupConnector].toInstance(fakeAddressLookupConnector),
        bind[AuthAction].to(FakeAuthAction),
        bind[DataRetrievalAction].to(getMandatoryWorkingKnowledgePerson),
        bind[DataRequiredAction].to(new DataRequiredActionImpl),
        bind[PostCodeLookupFormProvider].to(formProvider),
        bind[Navigator].toInstance(new FakeNavigator(onwardRoute))
      )) {
        implicit app =>

          val fakeRequest = addCSRFToken(FakeRequest()
            .withFormUrlEncodedBody("postcode" -> validPostcode))
          val controller = app.injector.instanceOf[AdviserPostCodeLookupController]
          val result = controller.onSubmit(NormalMode)(fakeRequest)

          status(result) must be(SEE_OTHER)
          redirectLocation(result).value mustEqual onwardRoute.url

      }
    }
  }
}
