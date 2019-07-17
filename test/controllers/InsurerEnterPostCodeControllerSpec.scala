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
import connectors.{AddressLookupConnector, FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.actions._
import forms.address.PostCodeLookupFormProvider
import identifiers.{InsurerConfirmAddressId, InsurerSelectAddressId, SchemeNameId}
import models.{CheckUpdateMode, NormalMode}
import models.address.{Address, TolerantAddress}
import models.person.PersonDetails
import org.joda.time.LocalDate
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import play.api.Application
import play.api.http.Writeable
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.{Call, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, route, _}
import services.{FakeUserAnswersService, UserAnswersService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.annotations.{AboutBenefitsAndInsurance, InsuranceService}
import utils.{FakeNavigator, Navigator}
import viewmodels.address.PostcodeLookupViewModel
import views.html.address.postcodeLookup

import scala.concurrent.{ExecutionContext, Future}

class InsurerEnterPostCodeControllerSpec extends ControllerSpecBase with CSRFRequest with ScalaFutures {

  import InsurerEnterPostCodeControllerSpec._

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  "InsurerEnterPostCodeController Controller" must {
    "render postCodeLookup from a GET request" in {
      requestResult(
        implicit app => addToken(FakeRequest(routes.InsurerEnterPostcodeController.onPageLoad(NormalMode, None))),
        (request, result) => {

          val viewModel = PostcodeLookupViewModel(
            routes.InsurerEnterPostcodeController.onSubmit(NormalMode, None),
            routes.InsurerConfirmAddressController.onClick(NormalMode, None),
            Messages("messages__insurer_enter_postcode__title"),
            "messages__insurer_enter_postcode__h1",
            None
          )

          status(result) mustBe OK
          contentAsString(result) mustBe postcodeLookup(frontendAppConfig, form, viewModel, Some("Test Scheme Name"))(request, messages).toString()
        }
      )
    }

    "redirect to next page on POST request" which {
      "returns a list of addresses from addressLookup given a postcode" in {
        requestResult(
          implicit app => addToken(FakeRequest(routes.InsurerEnterPostcodeController.onSubmit(NormalMode, None))
            .withFormUrlEncodedBody("value" -> validPostcode)),
          (_, result) => {
            status(result) mustBe SEE_OTHER
            redirectLocation(result) mustBe Some(onwardRoute.url)
          }
        )
      }
    }

    "clear saved address and selected address in list" when {
      "user clicks on manual entry link" in {
        requestResult(
          implicit app => addToken(FakeRequest(routes.InsurerConfirmAddressController.onClick(CheckUpdateMode, srn))),
          (_, result) => {
            status(result) mustBe SEE_OTHER
            redirectLocation(result) mustBe Some(routes.InsurerConfirmAddressController.onPageLoad(CheckUpdateMode, srn).url)
          }, preSavedAddress
        )
      }
    }
  }
}

object InsurerEnterPostCodeControllerSpec extends OptionValues {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val formProvider = new PostCodeLookupFormProvider()
  val form = formProvider()
  val personDetails = PersonDetails("Firstname", Some("Middle"), "Last", LocalDate.now())
  val validPostcode = "ZZ1 1ZZ"
  val fakeNavigator = new FakeNavigator(desiredRoute = onwardRoute)
  val tolerantAddress = TolerantAddress(Some("address line 1"), Some("address line 2"), None, None, Some(validPostcode), Some("GB"))
  val address = Address("address line 1", "address line 2", None, None, Some(validPostcode), "GB")
  val srn = Some("123")

  val retrieval = new FakeDataRetrievalAction(Some(
    Json.obj(SchemeNameId.toString -> "Test Scheme Name")
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

  private def requestResult[T](request: Application => Request[T], test: (Request[_], Future[Result]) => Unit,
                               data: DataRetrievalAction = retrieval)
                              (implicit writeable: Writeable[T]): Unit = {
    running(_.overrides(
      bind[AuthAction].to(FakeAuthAction),
      bind[DataRetrievalAction].toInstance(data),
      bind[DataRequiredAction].to(new DataRequiredActionImpl),
      bind[AddressLookupConnector].toInstance(fakeAddressLookupConnector),
      bind(classOf[Navigator]).qualifiedWith(classOf[AboutBenefitsAndInsurance]).toInstance(fakeNavigator),
      bind[UserAnswersService].qualifiedWith(classOf[InsuranceService]).toInstance(FakeUserAnswersService),
      bind[PostCodeLookupFormProvider].to(formProvider)
    )) {
      app =>
        val req = request(app)
        val result = route[T](app, req).value
        test(req, result)
    }
  }

}
