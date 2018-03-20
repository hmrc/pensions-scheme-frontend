/*
 * Copyright 2018 HM Revenue & Customs
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
import connectors.{AddressLookupConnector, DataCacheConnector, FakeDataCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.establishers.individual.PostCodeLookupFormProvider
import models.addresslookup.{Address, AddressRecord}
import models.{CompanyDetails, Index, NormalMode}
import org.mockito.Matchers
import org.mockito.Matchers.{eq => eqTo}
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import utils.{FakeNavigator, Navigator}
import viewmodels.address.PostcodeLookupViewModel
import views.html.address.postcodeLookup

import scala.concurrent.Future

class CompanyPostCodeLookupControllerSpec extends ControllerSpecBase with MockitoSugar with ScalaFutures with CSRFRequest {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val formProvider = new PostCodeLookupFormProvider()
  val form = formProvider()

  val fakeAddressLookupConnector: AddressLookupConnector = mock[AddressLookupConnector]
  implicit val hc: HeaderCarrier = mock[HeaderCarrier]

  val firstIndex = Index(0)
  val companyName: String = "test company name"

  val company = CompanyDetails(companyName, None, None)

  def viewAsString(form: Form[_] = form): String =
    postcodeLookup(
      frontendAppConfig,
      form,
      PostcodeLookupViewModel(
        postCall = onwardRoute,
        manualInputCall = routes.CompanyPreviousAddressController.onPageLoad(NormalMode, firstIndex),
        subHeading = Some(company.companyName)
      )
    )(fakeRequest, messages).toString

  "Company Postcode Controller" must {

    "render postcodeLookup from GET request" in {

      val call: Call = routes.CompanyPostCodeLookupController.onPageLoad(NormalMode, firstIndex)

      val cacheConnector: DataCacheConnector = mock[DataCacheConnector]
      val addressConnector: AddressLookupConnector = mock[AddressLookupConnector]

      running(_.overrides(
        bind[FrontendAppConfig].to(frontendAppConfig),
        bind[Navigator].toInstance(FakeNavigator),
        bind[DataCacheConnector].toInstance(cacheConnector),
        bind[AddressLookupConnector].toInstance(addressConnector),
        bind[AuthAction].to(FakeAuthAction),
        bind[DataRetrievalAction].to(getMandatoryEstablisherCompany)
      )){ app =>

        val result = route(app, FakeRequest("GET", call.url)).get

        status(result) must be(OK)

      }
    }

    "redirect to next page on POST request" in {

      val call: Call = routes.CompanyPostCodeLookupController.onSubmit(NormalMode, firstIndex)

      val validPostcode = "ZZ11ZZ"

      val fakeRequest = addToken(FakeRequest(call)
        .withFormUrlEncodedBody("value" -> validPostcode))

      when(fakeAddressLookupConnector.addressLookupByPostCode(Matchers.eq(validPostcode))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(
          Some(Seq(AddressRecord(Address("address line 1", "address line 2", None, None, Some(validPostcode), "GB"))))
        ))

      running(_.overrides(
        bind[FrontendAppConfig].to(frontendAppConfig),
        bind[MessagesApi].to(messagesApi),
        bind[DataCacheConnector].toInstance(FakeDataCacheConnector),
        bind[AddressLookupConnector].toInstance(fakeAddressLookupConnector),
        bind[Navigator].toInstance(new FakeNavigator(desiredRoute = onwardRoute)),
        bind[AuthAction].to(FakeAuthAction),
        bind[DataRetrievalAction].to(getMandatoryEstablisherCompany),
        bind[DataRequiredAction].to(new DataRequiredActionImpl),
        bind[PostCodeLookupFormProvider].to(formProvider)
      )){ app =>

        val result = route(app, fakeRequest).get

        status(result) must be(SEE_OTHER)
        redirectLocation(result) mustEqual Some(onwardRoute.url)

      }
    }

  }
}
