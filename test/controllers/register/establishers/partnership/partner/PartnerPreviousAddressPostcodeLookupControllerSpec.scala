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
import controllers.ControllerSpecBase
import controllers.actions._
import forms.address.PostCodeLookupFormProvider
import models.address.TolerantAddress
import models.person.PersonName
import models.{Index, NormalMode}
import navigators.Navigator
import org.mockito.Matchers
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{FakeUserAnswersService, UserAnswersService}
import utils.FakeNavigator
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel
import views.html.address.postcodeLookup

import scala.concurrent.Future

class PartnerPreviousAddressPostcodeLookupControllerSpec extends ControllerSpecBase with MockitoSugar with CSRFRequest {

  def onwardRoute: Call = routes.PartnerPreviousAddressPostcodeLookupController.onSubmit(NormalMode, establisherIndex, partnerIndex, None)

  def manualInputCall: Call = routes.PartnerPreviousAddressController.onPageLoad(NormalMode, establisherIndex, partnerIndex, None)

  private val formProvider = new PostCodeLookupFormProvider()

  private val establisherIndex = Index(0)
  private val partnerIndex = Index(0)

  private val partner = PersonName("first", "last")

  private val form = formProvider()
  private val fakeAddressLookupConnector: AddressLookupConnector = mock[AddressLookupConnector]
  private val fakeCacheConnector: UserAnswersService = mock[UserAnswersService]


  lazy val viewmodel = PostcodeLookupViewModel(
    onwardRoute,
    manualInputCall,
    Message("messages__partnerPreviousAddressPostcodeLookup__heading", Message("messages__thePartner").resolve),
    Message("messages__partnerPreviousAddressPostcodeLookup__heading", partner.fullName),
    Some(partner.fullName)
  )

  "PartnerPreviousAddressPostcodeLookup Controller" must {

    "return OK and the correct view for a GET" in {

      val call: Call = routes.PartnerPreviousAddressPostcodeLookupController.onPageLoad(NormalMode, establisherIndex, partnerIndex, None)
      running(_.overrides(
        bind[FrontendAppConfig].to(frontendAppConfig),
        bind[Navigator].toInstance(FakeNavigator),
        bind[UserAnswersService].toInstance(fakeCacheConnector),
        bind[AddressLookupConnector].toInstance(fakeAddressLookupConnector),
        bind[AuthAction].to(FakeAuthAction),
        bind[DataRetrievalAction].to(getMandatoryPartner)
      )) {
        implicit app =>

          val request = addToken(FakeRequest(call)
            .withHeaders("Csrf-Token" -> "nocheck"))

          val result = route(app, request).get
          status(result) mustBe OK

          contentAsString(result) mustEqual postcodeLookup(
            frontendAppConfig,
            form,
            viewmodel,
            None
          )(request, messages).toString
      }
    }

    "redirect to the next page on POST request" in {

      val validPostcode = "ZZ1 1ZZ"
      val onwardUrl = routes.PartnerPreviousAddressListController.onPageLoad(NormalMode, establisherIndex, partnerIndex, None)

      val fakeRequest = addToken(FakeRequest(onwardRoute))
        .withFormUrlEncodedBody("value" -> validPostcode)
        .withHeaders("Csrf-Token" -> "nocheck")

      when(fakeAddressLookupConnector.addressLookupByPostCode(Matchers.eq(validPostcode))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Seq(TolerantAddress(Some("address line 1"), Some("address line 2"), None, None, Some(validPostcode), Some("GB")))
        ))

      running(_.overrides(
        bind[FrontendAppConfig].to(frontendAppConfig),
        bind[MessagesApi].to(messagesApi),
        bind[Navigator].toInstance(new FakeNavigator(desiredRoute = onwardUrl)),
        bind[UserAnswersService].toInstance(FakeUserAnswersService),
        bind[AddressLookupConnector].toInstance(fakeAddressLookupConnector),
        bind[AuthAction].to(FakeAuthAction),
        bind[DataRetrievalAction].to(getMandatoryPartner),
        bind[DataRequiredAction].to(new DataRequiredActionImpl),
        bind[PostCodeLookupFormProvider].to(formProvider)
      )) {
        app =>
          val result = route(app, fakeRequest).get

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(onwardUrl.url)
      }
    }

  }
}
