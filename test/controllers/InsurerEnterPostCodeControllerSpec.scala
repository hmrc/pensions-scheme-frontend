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

import connectors.AddressLookupConnector
import controllers.actions._
import forms.address.PostCodeLookupFormProvider
import identifiers.{InsuranceCompanyNameId, InsurerConfirmAddressId, InsurerSelectAddressId, SchemeNameId}
import models.NormalMode
import models.address.{Address, TolerantAddress}
import navigators.Navigator
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.CSRFTokenHelper.addCSRFToken
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, _}
import services.{FakeUserAnswersService, UserAnswersService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.FakeNavigator
import utils.annotations.{AboutBenefitsAndInsurance, InsuranceService}
import viewmodels.address.PostcodeLookupViewModel
import views.html.address.postcodeLookup

import scala.concurrent.{ExecutionContext, Future}

class InsurerEnterPostCodeControllerSpec extends ControllerSpecBase with ScalaFutures {

  import InsurerEnterPostCodeControllerSpec._

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad
  private val view = injector.instanceOf[postcodeLookup]

  "InsurerEnterPostCodeController Controller" must {
    "render postCodeLookup from a GET request" in {
      running(_.overrides(
        bind[AuthAction].to(FakeAuthAction),
        bind[DataRetrievalAction].toInstance(retrieval),
        bind[DataRequiredAction].to(new DataRequiredActionImpl),
        bind[AddressLookupConnector].toInstance(fakeAddressLookupConnector),
        bind(classOf[Navigator]).qualifiedWith(classOf[AboutBenefitsAndInsurance]).toInstance(fakeNavigator),
        bind[UserAnswersService].qualifiedWith(classOf[InsuranceService]).toInstance(FakeUserAnswersService),
        bind[PostCodeLookupFormProvider].to(formProvider),
        bind(classOf[AllowAccessActionProvider]).toInstance(FakeAllowAccessProvider(srn))
      )) {
        implicit app =>

          val request = addCSRFToken(FakeRequest())
          val controller = app.injector.instanceOf[InsurerEnterPostcodeController]
          val result = controller.onPageLoad(NormalMode, srn)(request)

          val viewModel = PostcodeLookupViewModel(
            routes.InsurerEnterPostcodeController.onSubmit(NormalMode, srn),
            routes.InsurerConfirmAddressController.onPageLoad(NormalMode, srn),
            Messages("messages__insurer_enter_postcode__h1", Messages("messages__theInsuranceCompany")),
            Messages("messages__insurer_enter_postcode__h1",insurerName), None,
            srn = srn
          )

          status(result) mustBe OK
          contentAsString(result) mustBe view(form, viewModel, Some("Test Scheme Name"))(request, messages).toString()
        }
    }

    "redirect to next page on POST request" which {
      "returns a list of addresses from addressLookup given a postcode" in {
        running(_.overrides(
          bind[AuthAction].to(FakeAuthAction),
          bind[DataRetrievalAction].toInstance(retrieval),
          bind[DataRequiredAction].to(new DataRequiredActionImpl),
          bind[AddressLookupConnector].toInstance(fakeAddressLookupConnector),
          bind(classOf[Navigator]).qualifiedWith(classOf[AboutBenefitsAndInsurance]).toInstance(fakeNavigator),
          bind[UserAnswersService].qualifiedWith(classOf[InsuranceService]).toInstance(FakeUserAnswersService),
          bind[PostCodeLookupFormProvider].to(formProvider)
        )) {
          implicit app =>
          val request = addCSRFToken(FakeRequest().withFormUrlEncodedBody("postcode" -> validPostcode))
          val controller = app.injector.instanceOf[InsurerEnterPostcodeController]
          val result = controller.onSubmit(NormalMode, srn)(request)
            status(result) mustBe SEE_OTHER
            redirectLocation(result) mustBe Some(onwardRoute.url)
          }
      }
    }
  }
}

object InsurerEnterPostCodeControllerSpec extends OptionValues {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad

  val formProvider = new PostCodeLookupFormProvider()
  val form = formProvider()
  val validPostcode = "ZZ1 1ZZ"
  val fakeNavigator = new FakeNavigator(desiredRoute = onwardRoute)
  val tolerantAddress = TolerantAddress(Some("address line 1"), Some("address line 2"), None, None, Some(validPostcode), Some("GB"))
  val address = Address("address line 1", "address line 2", None, None, Some(validPostcode), "GB")
  val srn = Some("123")
  val insurerName = "the insurer"

  val retrieval = new FakeDataRetrievalAction(Some(
    Json.obj(SchemeNameId.toString -> "Test Scheme Name",InsuranceCompanyNameId.toString ->insurerName)
  ))

  val preSavedAddress = new FakeDataRetrievalAction(Some(
    Json.obj(InsurerConfirmAddressId.toString -> address,
      InsurerSelectAddressId.toString -> tolerantAddress)
  ))

  private val fakeAddressLookupConnector = new AddressLookupConnector {
    override def addressLookupByPostCode(postcode: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[TolerantAddress]] = {
      Future.successful(Seq(tolerantAddress))
    }
  }

}
