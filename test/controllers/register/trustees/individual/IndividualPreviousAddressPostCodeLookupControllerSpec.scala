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

package controllers.register.trustees.individual

import base.CSRFRequest
import connectors.AddressLookupConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.address.PostCodeLookupFormProvider
import identifiers.register.trustees.TrusteesId
import identifiers.register.trustees.individual.TrusteeNameId
import models.address.TolerantAddress
import models.person.PersonName
import models.{Index, NormalMode}
import navigators.Navigator
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.Application
import play.api.http.Writeable
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.{Call, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{FakeUserAnswersService, UserAnswersService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.FakeNavigator
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel
import views.html.address.postcodeLookup

import scala.concurrent.{ExecutionContext, Future}

class IndividualPreviousAddressPostCodeLookupControllerSpec extends ControllerSpecBase with CSRFRequest with ScalaFutures {

  import IndividualPreviousAddressPostCodeLookupControllerSpec._

  "IndividualPreviousAddressPostCodeLookup Controller" must {
    "render postCodeLookup from a GET request" in {
      requestResult(
        implicit app => addToken(FakeRequest(routes.IndividualPreviousAddressPostcodeLookupController.onPageLoad(NormalMode, firstIndex, None))),
        (request, result) => {
          status(result) mustBe OK
          contentAsString(result) mustBe postcodeLookup(frontendAppConfig, form, viewModel, None)(request, messages).toString()
        }
      )
    }

    "redirect to next page on POST request" which {
      "returns a list of addresses from addressLookup given a postcode" in {
        requestResult(
          implicit app => addToken(FakeRequest(routes.IndividualPreviousAddressPostcodeLookupController.onSubmit(NormalMode, firstIndex, None))
            .withFormUrlEncodedBody("value" -> validPostcode)),
          (_, result) => {
            status(result) mustBe SEE_OTHER
            redirectLocation(result) mustBe Some(onwardRoute.url)
          }
        )
      }
    }
  }
}

object IndividualPreviousAddressPostCodeLookupControllerSpec extends ControllerSpecBase with MockitoSugar {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val firstIndex = Index(0)
  val formProvider = new PostCodeLookupFormProvider()
  val form = formProvider()
  val personDetails = PersonName("Firstname", "Last")
  val validPostcode = "ZZ1 1ZZ"
  val fakeNavigator = new FakeNavigator(desiredRoute = onwardRoute)
  val address = TolerantAddress(Some("address line 1"), Some("address line 2"), None, None, Some(validPostcode), Some("GB"))

  lazy val viewModel = PostcodeLookupViewModel(
    postCall = routes.IndividualPreviousAddressPostcodeLookupController.onSubmit(NormalMode, firstIndex, None),
    manualInputCall = routes.TrusteePreviousAddressController.onPageLoad(NormalMode, firstIndex, None),
    title = Message("messages__trustee_individual_previous_address__title"),
    heading = Message("messages__trustee_individual_previous_address__heading", personDetails.fullName),
    subHeading = Some(personDetails.fullName)
  )
  val retrieval = new FakeDataRetrievalAction(Some(
    Json.obj(
      TrusteesId.toString -> Json.arr(
        Json.obj(
          TrusteeNameId.toString -> personDetails
        )
      )
    )
  ))
  private val fakeAddressLookupConnector = new AddressLookupConnector {
    override def addressLookupByPostCode(postcode: String)(implicit hc: HeaderCarrier, ec: ExecutionContext):
    Future[Seq[TolerantAddress]] = {
      Future.successful(Seq(address))
    }
  }

  private def requestResult[T](request: Application => Request[T], test: (Request[_], Future[Result]) => Unit)(implicit writeable: Writeable[T]): Unit = {
    running(_.overrides(
      bind[AuthAction].to(FakeAuthAction),
      bind[DataRetrievalAction].toInstance(retrieval),
      bind[DataRequiredAction].to(new DataRequiredActionImpl),
      bind[AddressLookupConnector].toInstance(fakeAddressLookupConnector),
      bind(classOf[Navigator]).toInstance(fakeNavigator),
      bind[UserAnswersService].toInstance(FakeUserAnswersService),
      bind[PostCodeLookupFormProvider].to(formProvider)
    )) {
      app =>
        val req = request(app)
        val result = route[T](app, req).value
        test(req, result)
    }
  }
}
