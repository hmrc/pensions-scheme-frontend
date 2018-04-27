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

package controllers.register.trustees.company

import base.CSRFRequest
import config.FrontendAppConfig
import connectors.{AddressLookupConnector, DataCacheConnector, FakeDataCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import forms.address.PostCodeLookupFormProvider
import identifiers.register.trustees.TrusteesId
import identifiers.register.trustees.company.CompanyDetailsId
import models.address.TolerantAddress
import models.{CompanyDetails, Index, NormalMode}
import org.mockito.Matchers
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.{FakeNavigator, Navigator}
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel
import views.html.address.postcodeLookup

import scala.concurrent.Future

class CompanyPreviousAddressPostCodeLookupControllerSpec extends ControllerSpecBase with CSRFRequest with MockitoSugar with ScalaFutures  {

  def onwardRoute = routes.CompanyPreviousAddressListController.onPageLoad(NormalMode,Index(0))


  val formProvider = new PostCodeLookupFormProvider()
  val form = formProvider()

  val firstIndex = Index(0)

  val companyName: String = "test company name"
  val company = CompanyDetails(companyName, None, None)

  val retrieval = new FakeDataRetrievalAction(Some(
    Json.obj(TrusteesId.toString -> Json.arr(
      Json.obj(CompanyDetailsId.toString -> company)
    ))
  ))

  val fakeAddressLookupConnector: AddressLookupConnector = mock[AddressLookupConnector]

  "PreviousAddressPostcodeLookup Controller" must {

    "render postcodeLookup from GET request" in {

      val cacheConnector: DataCacheConnector = mock[DataCacheConnector]

      running(_.overrides(
        bind[FrontendAppConfig].to(frontendAppConfig),
        bind[Navigator].toInstance(FakeNavigator),
        bind[DataCacheConnector].toInstance(cacheConnector),
        bind[AddressLookupConnector].toInstance(fakeAddressLookupConnector),
        bind[AuthAction].to(FakeAuthAction),
        bind[DataRetrievalAction].to(retrieval)
      )) {
        implicit app =>

          val controller = app.injector.instanceOf[CompanyPreviousAddressPostcodeLookupController]

          lazy val viewModel = PostcodeLookupViewModel(
            postCall = controller.postCall(NormalMode, firstIndex),
            manualInputCall = controller.manualAddressCall(NormalMode, firstIndex),
            title = Message(controller.title),
            heading = Message(controller.heading),
            subHeading = Some(company.companyName)
          )

          def viewAsString(form: Form[_] = form) = postcodeLookup(frontendAppConfig, form, viewModel)(fakeRequest, messages).toString

          val request = addToken(FakeRequest(routes.CompanyPreviousAddressPostcodeLookupController.onPageLoad(NormalMode, firstIndex))
            .withHeaders("Csrf-Token" -> "nocheck"))

          val result = route(app, request).value

          status(result) must be(OK)

          contentAsString(result) mustEqual postcodeLookup(
            frontendAppConfig,
            form,
            viewModel
          )(request, messages).toString
      }
    }

    "redirect to next page on POST request" which {
      "returns a list of addresses from addressLookup given a postcode" in {

        val call: Call = routes.CompanyPreviousAddressPostcodeLookupController.onSubmit(NormalMode, firstIndex)

        val validPostcode = "ZZ1 1ZZ"

        when(fakeAddressLookupConnector.addressLookupByPostCode(Matchers.eq(validPostcode))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(
            Some(Seq(TolerantAddress(Some("address line 1"), Some("address line 2"), None, None, Some(validPostcode), Some("GB"))))
          ))

        running(_.overrides(
          bind[FrontendAppConfig].to(frontendAppConfig),
          bind[MessagesApi].to(messagesApi),
          bind[DataCacheConnector].toInstance(FakeDataCacheConnector),
          bind[AddressLookupConnector].toInstance(fakeAddressLookupConnector),
          bind[AuthAction].to(FakeAuthAction),
          bind[DataRetrievalAction].to(retrieval),
          bind[DataRequiredAction].to(new DataRequiredActionImpl),
          bind[PostCodeLookupFormProvider].to(formProvider)
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
}
