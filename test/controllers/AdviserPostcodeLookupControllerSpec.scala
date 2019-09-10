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

package controllers

import base.CSRFRequest
import config.FrontendAppConfig
import connectors.{AddressLookupConnector, FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.actions._
import forms.address.PostCodeLookupFormProvider
import models.NormalMode
import models.address.TolerantAddress
import navigators.Navigator
import org.mockito.Matchers
import org.mockito.Mockito.when
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.{OK, SEE_OTHER, contentAsString, redirectLocation, route, running, status, _}
import services.{FakeUserAnswersService, UserAnswersService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.annotations.Adviser
import utils.FakeNavigator
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel
import views.html.address.postcodeLookup

import scala.concurrent.Future

class AdviserPostcodeLookupControllerSpec extends ControllerSpecBase with MockitoSugar with ScalaFutures with CSRFRequest with OptionValues {

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
  val form = formProvider()

  val fakeAddressLookupConnector: AddressLookupConnector = mock[AddressLookupConnector]
  implicit val hc: HeaderCarrier = mock[HeaderCarrier]

  lazy val viewModel = PostcodeLookupViewModel(
    postCall = routes.AdviserPostCodeLookupController.onSubmit(NormalMode),
    manualInputCall = manualInputCall,
    title = Message("messages__adviserPostCodeLookup__title"),
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
        bind[Navigator].qualifiedWith(classOf[Adviser]).toInstance(new FakeNavigator(onwardRoute))
      )) {
        implicit app =>

          val request = addToken(FakeRequest(routes.AdviserPostCodeLookupController.onPageLoad(NormalMode))
            .withHeaders("Csrf-Token" -> "nocheck"))

          val result = route(app, request).value

          status(result) must be(OK)

          contentAsString(result) mustEqual postcodeLookup(
            frontendAppConfig,
            form,
            viewModel,
            None
          )(request, messages).toString
      }
    }

    "redirect to next page on POST request" in {

      val call: Call = routes.AdviserPostCodeLookupController.onSubmit(NormalMode)

      val validPostcode = "ZZ1 1ZZ"

      when(fakeAddressLookupConnector.addressLookupByPostCode(Matchers.eq(validPostcode))(Matchers.any(), Matchers.any()))
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
        bind[Navigator].qualifiedWith(classOf[Adviser]).toInstance(new FakeNavigator(onwardRoute))
      )) {
        implicit app =>

          val fakeRequest = addToken(FakeRequest(call)
            .withFormUrlEncodedBody("value" -> validPostcode)
            .withHeaders("Csrf-Token" -> "nocheck"))

          val result = route(app, fakeRequest).value

          status(result) must be(SEE_OTHER)
          redirectLocation(result).value mustEqual onwardRoute.url

      }
    }
  }
}
