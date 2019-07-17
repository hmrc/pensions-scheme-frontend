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

import base.CSRFRequest
import config.{FeatureSwitchManagementService, FrontendAppConfig}
import connectors.AddressLookupConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.address.PostCodeLookupFormProvider
import models.address.TolerantAddress
import models.person.PersonDetails
import models.{CompanyDetails, Index, NormalMode}
import org.joda.time.LocalDate
import org.mockito.Matchers
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{FakeUserAnswersService, UserAnswersService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.{FakeFeatureSwitchManagementService, FakeNavigator, Navigator}
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel
import views.html.address.postcodeLookup

import scala.concurrent.Future

class DirectorAddressPostcodeLookupControllerSpec extends ControllerSpecBase with MockitoSugar with CSRFRequest {

  def onwardRoute: Call = routes.DirectorAddressListController.onPageLoad(NormalMode, estIndex, dirIndex, None)
  def postCall: Call = routes.DirectorAddressPostcodeLookupController.onSubmit(NormalMode, estIndex, dirIndex, None)
  def manualInputCall: Call = routes.DirectorAddressController.onPageLoad(NormalMode, estIndex, dirIndex, None)

  val formProvider = new PostCodeLookupFormProvider()
  val form = formProvider()

  val fakeAddressLookupConnector: AddressLookupConnector = mock[AddressLookupConnector]
  val fakeFeatureSwitch = new FakeFeatureSwitchManagementService(false)

  implicit val hc: HeaderCarrier = mock[HeaderCarrier]

  val estIndex = Index(0)
  val dirIndex = Index(0)
  val companyName: String = "test company name"

  private def fakeAddress(postCode: String) = TolerantAddress(
    Some("Address Line 1"),
    Some("Address Line 2"),
    Some("Address Line 3"),
    Some("Address Line 4"),
    Some(postCode),
    Some("GB")
  )

  val company = CompanyDetails(companyName)
  val director = PersonDetails("first", Some("middle"), "last", LocalDate.now())

  lazy val viewmodel = PostcodeLookupViewModel(
    postCall,
    manualInputCall,
    Message("messages__directorCompanyAddressPostcodeLookup__title"),
    Message("messages__addressPostcodeLookup__heading", director.fullName),
    Some(director.fullName),
    Message("messages__common__postcode_lookup__manual_link")
  )

  "DirectorAddressPostcodeLookup Controller" must {

    "render postcodeLookup from GET request" in {
      val call: Call = routes.DirectorAddressPostcodeLookupController.onPageLoad(NormalMode, estIndex, dirIndex, None)

      val cacheConnector: UserAnswersService = mock[UserAnswersService]
      val addressConnector: AddressLookupConnector = mock[AddressLookupConnector]

      running(_.overrides(
        bind[FrontendAppConfig].to(frontendAppConfig),
        bind[Navigator].toInstance(FakeNavigator),
        bind[UserAnswersService].toInstance(cacheConnector),
        bind[AddressLookupConnector].toInstance(addressConnector),
        bind[AuthAction].to(FakeAuthAction),
        bind[DataRetrievalAction].to(getMandatoryEstablisherCompanyDirector),
        bind[FeatureSwitchManagementService].to(fakeFeatureSwitch)
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

      val validPostcode = "ZZ1 1ZZ"

      val fakeRequest = addToken(FakeRequest(postCall)
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
        bind[DataRetrievalAction].to(getMandatoryEstablisherCompanyDirector),
        bind[DataRequiredAction].to(new DataRequiredActionImpl),
        bind[PostCodeLookupFormProvider].to(formProvider),
        bind[FeatureSwitchManagementService].to(fakeFeatureSwitch)
      )) { app =>

        val result = route(app, fakeRequest).get

        status(result) must be(SEE_OTHER)
        redirectLocation(result) mustBe Some(onwardRoute.url)

      }
    }

  }

}
