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

package controllers.register.establishers.partnership.partner

import base.CSRFRequest
import config.FrontendAppConfig
import connectors.AddressLookupConnector
import services.{FakeUserAnswersService, UserAnswersService}
import controllers.ControllerSpecBase
import controllers.actions._
import forms.address.PostCodeLookupFormProvider
import models.address.TolerantAddress
import models.person.PersonDetails
import models.{Index, NormalMode}
import navigators.Navigator
import org.joda.time.LocalDate
import org.mockito.Matchers
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import utils.FakeNavigator
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel
import views.html.address.postcodeLookup

import scala.concurrent.Future

class PartnerAddressPostcodeLookupControllerSpec extends ControllerSpecBase with MockitoSugar with CSRFRequest {

  def onwardRoute: Call = routes.PartnerAddressPostcodeLookupController.onSubmit(NormalMode, estIndex, parIndex, None)

  def manualInputCall: Call = routes.PartnerAddressController.onPageLoad(NormalMode, estIndex, parIndex, None)

  val formProvider = new PostCodeLookupFormProvider()
  val form = formProvider()

  val fakeAddressLookupConnector: AddressLookupConnector = mock[AddressLookupConnector]

  implicit val hc: HeaderCarrier = mock[HeaderCarrier]

  val estIndex = Index(0)
  val parIndex = Index(0)
  val partnershipName: String = "test partnership name"

  private def fakeAddress(postCode: String) = TolerantAddress(
    Some("Address Line 1"),
    Some("Address Line 2"),
    Some("Address Line 3"),
    Some("Address Line 4"),
    Some(postCode),
    Some("GB")
  )

  val partner = PersonDetails("first", None, "last", LocalDate.now())

  lazy val viewmodel = PostcodeLookupViewModel(
    onwardRoute,
    manualInputCall,
    Message("messages__partnerAddressPostcodeLookup__title"),
    Message("messages__partnerAddressPostcodeLookup__heading", partner.fullName),
    Some(partner.fullName)
  )

  "PartnerAddressPostcodeLookup Controller" must {

    "render postcodeLookup from GET request" in {
      val call: Call = routes.PartnerAddressPostcodeLookupController.onPageLoad(NormalMode, estIndex, parIndex, None)

      val cacheConnector: UserAnswersService = mock[UserAnswersService]
      val addressConnector: AddressLookupConnector = mock[AddressLookupConnector]

      running(_.overrides(
        bind[FrontendAppConfig].to(frontendAppConfig),
        bind[Navigator].toInstance(FakeNavigator),
        bind[UserAnswersService].toInstance(cacheConnector),
        bind[AddressLookupConnector].toInstance(addressConnector),
        bind[AuthAction].to(FakeAuthAction),
        bind[DataRetrievalAction].to(getMandatoryEstablisherPartner)
      )) { implicit app =>

        val request = addToken(FakeRequest(call)
          .withHeaders("Csrf-Token" -> "nocheck"))
        val result = route(app, request).get

        status(result) must be(OK)

        contentAsString(result) mustEqual postcodeLookup(
          frontendAppConfig,
          form,
          viewmodel,
          None
        )(request, messages).toString

      }
    }

    "redirect to next page on POST request" in {

      val call: Call = routes.PartnerAddressListController.onSubmit(NormalMode, estIndex, parIndex, None)

      val validPostcode = "ZZ1 1ZZ"

      val fakeRequest = addToken(FakeRequest(call)
        .withFormUrlEncodedBody("value" -> validPostcode))

      when(fakeAddressLookupConnector.addressLookupByPostCode(Matchers.eq(validPostcode))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Seq(fakeAddress(validPostcode)))
        )

      running(_.overrides(
        bind[FrontendAppConfig].to(frontendAppConfig),
        bind[MessagesApi].to(messagesApi),
        bind[UserAnswersService].toInstance(FakeUserAnswersService),
        bind[AddressLookupConnector].toInstance(fakeAddressLookupConnector),
        bind[Navigator].toInstance(new FakeNavigator(desiredRoute = onwardRoute)),
        bind[AuthAction].to(FakeAuthAction),
        bind[DataRetrievalAction].to(getMandatoryEstablisherPartner),
        bind[DataRequiredAction].to(new DataRequiredActionImpl),
        bind[PostCodeLookupFormProvider].to(formProvider)
      )) { app =>

        val result = route(app, fakeRequest).get

        status(result) must be(SEE_OTHER)
        redirectLocation(result) mustBe Some(onwardRoute.url)

      }
    }

  }

}
