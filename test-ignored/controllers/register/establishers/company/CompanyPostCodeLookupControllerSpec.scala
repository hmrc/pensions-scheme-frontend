/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers.register.establishers.company

import base.CSRFRequest
import config.FrontendAppConfig
import connectors.AddressLookupConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.address.PostCodeLookupFormProvider
import models.address.TolerantAddress
import models.{CompanyDetails, Index, NormalMode}
import org.mockito.Matchers
import org.mockito.Mockito.when
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{FakeUserAnswersService, UserAnswersService}
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel
import views.html.address.postcodeLookup

import scala.concurrent.Future

class CompanyPostCodeLookupControllerSpec extends ControllerSpecBase with MockitoSugar with ScalaFutures with CSRFRequest with OptionValues {

  def onwardRoute: Call = routes.CompanyAddressListController.onPageLoad(NormalMode, None, firstIndex)

  def manualInputCall: Call = routes.CompanyAddressController.onPageLoad(NormalMode, None, firstIndex)

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

  val firstIndex = Index(0)
  val companyName: String = "test company name"


  val company = CompanyDetails(companyName)

  lazy val viewModel = PostcodeLookupViewModel(
    postCall = routes.CompanyPostCodeLookupController.onSubmit(NormalMode, None, firstIndex),
    manualInputCall = manualInputCall,
    title = Message("messages__establisherPostCode__title"),
    heading = Message("messages__establisherPostCode__h1", companyName)
  )

  "Company Postcode Controller" must {

    "render postcodeLookup from GET request" in {

      val cacheConnector: UserAnswersService = mock[UserAnswersService]
      val addressConnector: AddressLookupConnector = mock[AddressLookupConnector]

      running(_.overrides(
        bind[FrontendAppConfig].to(frontendAppConfig),
        bind[UserAnswersService].toInstance(cacheConnector),
        bind[AddressLookupConnector].toInstance(addressConnector),
        bind[AuthAction].to(FakeAuthAction),
        bind[DataRetrievalAction].to(getMandatoryEstablisherCompany)
      )) {
        implicit app =>

          val request = addToken(FakeRequest(routes.CompanyPostCodeLookupController.onPageLoad(NormalMode, None, firstIndex))
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

      val call: Call = routes.CompanyPostCodeLookupController.onSubmit(NormalMode, None, firstIndex)

      val validPostcode = "ZZ1 1ZZ"

      when(fakeAddressLookupConnector.addressLookupByPostCode(Matchers.eq(validPostcode))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Seq(fakeAddress(testAnswer)))
        )

      running(_.overrides(
        bind[FrontendAppConfig].to(frontendAppConfig),
        bind[MessagesApi].to(messagesApi),
        bind[UserAnswersService].toInstance(FakeUserAnswersService),
        bind[AddressLookupConnector].toInstance(fakeAddressLookupConnector),
        bind[AuthAction].to(FakeAuthAction),
        bind[DataRetrievalAction].to(getMandatoryEstablisherCompany),
        bind[DataRequiredAction].to(new DataRequiredActionImpl),
        bind[PostCodeLookupFormProvider].to(formProvider)
      )) {
        implicit app =>

          val fakeRequest = addToken(FakeRequest(call)
            .withFormUrlEncodedBody("postcode" -> validPostcode)
            .withHeaders("Csrf-Token" -> "nocheck"))

          val result = route(app, fakeRequest).value

          status(result) must be(SEE_OTHER)
          redirectLocation(result).value mustEqual onwardRoute.url
      }
    }
  }
}
