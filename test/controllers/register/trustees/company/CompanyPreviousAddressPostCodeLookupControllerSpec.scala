/*
 * Copyright 2021 HM Revenue & Customs
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

import play.api.test.CSRFTokenHelper.addCSRFToken
import config.FrontendAppConfig
import connectors.AddressLookupConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.address.PostCodeLookupFormProvider
import identifiers.register.trustees.TrusteesId
import identifiers.register.trustees.company.CompanyDetailsId
import models.address.TolerantAddress
import models.{CompanyDetails, Index, NormalMode}
import navigators.Navigator
import org.mockito.Matchers
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{FakeUserAnswersService, UserAnswersService}
import utils.FakeNavigator
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel
import views.html.address.postcodeLookup

import scala.concurrent.Future

class CompanyPreviousAddressPostCodeLookupControllerSpec extends ControllerSpecBase with MockitoSugar with ScalaFutures {

  def onwardRoute: Call = routes.CompanyPreviousAddressListController.onPageLoad(NormalMode, Index(0), None)


  private val view = injector.instanceOf[postcodeLookup]
  val formProvider = new PostCodeLookupFormProvider()
  val form: Form[String] = formProvider()

  val firstIndex: Index = Index(0)

  val companyName: String = "test company name"
  val company: CompanyDetails = CompanyDetails(companyName)

  val retrieval = new FakeDataRetrievalAction(Some(
    Json.obj(TrusteesId.toString -> Json.arr(
      Json.obj(CompanyDetailsId.toString -> company)
    ))
  ))

  val fakeAddressLookupConnector: AddressLookupConnector = mock[AddressLookupConnector]

  "PreviousAddressPostcodeLookup Controller" must {

    "render postcodeLookup from GET request" in {

      val cacheConnector: UserAnswersService = mock[UserAnswersService]

      running(_.overrides(
        bind[FrontendAppConfig].to(frontendAppConfig),
        bind[Navigator].toInstance(FakeNavigator),
        bind[UserAnswersService].toInstance(cacheConnector),
        bind[AddressLookupConnector].toInstance(fakeAddressLookupConnector),
        bind[AuthAction].to(FakeAuthAction),
        bind[DataRetrievalAction].to(retrieval)
      )) {
        implicit app =>

          val controller = app.injector.instanceOf[CompanyPreviousAddressPostcodeLookupController]

          lazy val viewModel = PostcodeLookupViewModel(
            postCall = controller.postCall(NormalMode, firstIndex, None),
            manualInputCall = controller.manualAddressCall(NormalMode, firstIndex, None),
            title = Message(controller.title),
            heading = Message(controller.heading, company.companyName),
            subHeading = Some(company.companyName)
          )

          val request = addCSRFToken(FakeRequest(routes.CompanyPreviousAddressPostcodeLookupController.onPageLoad(NormalMode, firstIndex, None))
            .withHeaders("Csrf-Token" -> "nocheck"))

          val result = route(app, request).value

          status(result) must be(OK)

          contentAsString(result) mustEqual view(
            form,
            viewModel,
            None
          )(request, messages).toString
      }
    }

    "redirect to next page on POST request" which {
      "returns a list of addresses from addressLookup given a postcode" in {

        val validPostcode = "ZZ1 1ZZ"

        when(fakeAddressLookupConnector.addressLookupByPostCode(Matchers.eq(validPostcode))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(
            Seq(TolerantAddress(Some("address line 1"), Some("address line 2"), None, None, Some(validPostcode), Some("GB"))))
          )

        running(_.overrides(
          bind[FrontendAppConfig].to(frontendAppConfig),
          bind[MessagesApi].to(messagesApi),
          bind[UserAnswersService].toInstance(FakeUserAnswersService),
          bind[AddressLookupConnector].toInstance(fakeAddressLookupConnector),
          bind[AuthAction].to(FakeAuthAction),
          bind[DataRetrievalAction].to(retrieval),
          bind[DataRequiredAction].to(new DataRequiredActionImpl),
          bind[PostCodeLookupFormProvider].to(formProvider)
        )) {
          implicit app =>

            val fakeRequest = addCSRFToken(FakeRequest()
              .withFormUrlEncodedBody("postcode" -> validPostcode)
              .withHeaders("Csrf-Token" -> "nocheck"))

            val controller = app.injector.instanceOf[CompanyPreviousAddressPostcodeLookupController]
            val result = controller.onSubmit(NormalMode, firstIndex, None)(fakeRequest)

            status(result) must be(SEE_OTHER)
            redirectLocation(result).value mustEqual onwardRoute.url
        }
      }
    }
  }
}
