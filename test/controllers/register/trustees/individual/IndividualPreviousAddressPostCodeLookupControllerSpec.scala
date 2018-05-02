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

package controllers.register.trustees.individual

import base.CSRFRequest
import connectors.{AddressLookupConnector, DataCacheConnector, FakeDataCacheConnector}
import play.api.test.Helpers._
import controllers.ControllerSpecBase
import controllers.actions._
import forms.address.PostCodeLookupFormProvider
import identifiers.register.trustees.TrusteesId
import identifiers.register.trustees.individual.TrusteeDetailsId
import models.address.{Address, AddressRecord, TolerantAddress}
import models.{Index, NormalMode}
import models.person.PersonDetails
import org.joda.time.LocalDate
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.Application
import play.api.http.Writeable
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.{Call, Request, Result}
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier
import utils.annotations.TrusteesIndividual
import utils.{FakeNavigator, Navigator}
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel
import views.html.address.postcodeLookup

import scala.concurrent.{ExecutionContext, Future}

class IndividualPreviousAddressPostCodeLookupControllerSpec extends ControllerSpecBase with CSRFRequest with ScalaFutures {

  import IndividualPreviousAddressPostCodeLookupControllerSpec._

  "IndividualPreviousAddressPostCodeLookup Controller" must {
    "render postCodeLookup from a GET request" in {
      requestResult(
        implicit app => addToken(FakeRequest(routes.IndividualPreviousAddressPostcodeLookupController.onPageLoad(NormalMode, firstIndex))),
        (request, result) => {
          status(result) mustBe OK
          contentAsString(result) mustBe postcodeLookup(frontendAppConfig, form, viewModel)(request, messages).toString()
        }
      )
    }

    "redirect to next page on POST request" which {
      "returns a list of addresses from addressLookup given a postcode" in {
        requestResult(
          implicit app => addToken(FakeRequest(routes.IndividualPreviousAddressPostcodeLookupController.onSubmit(NormalMode, firstIndex))
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
  val personDetails = PersonDetails("Firstname", Some("Middle"), "Last", LocalDate.now())
  val validPostcode = "ZZ1 1ZZ"
  val fakeNavigator = new FakeNavigator(desiredRoute = onwardRoute)
  val address = TolerantAddress(Some("address line 1"), Some("address line 2"), None, None, Some(validPostcode), Some("GB"))

  lazy val viewModel = PostcodeLookupViewModel(
    postCall = routes.IndividualPreviousAddressPostcodeLookupController.onSubmit(NormalMode, firstIndex),
    manualInputCall = routes.TrusteePreviousAddressController.onPageLoad(NormalMode, firstIndex),
    title = Message("messages__trustee_individual_previous_address__title"),
    heading = Message("messages__trustee_individual_previous_address__heading"),
    subHeading = Some(personDetails.fullName),
    hint = Some(Message("messages__common_individual_postCode_lookup__lede")),
    enterPostcode = Message("messages__trustee_individualPostCodeLookup__enter_postcode")
  )
  val retrieval = new FakeDataRetrievalAction(Some(
    Json.obj(
      TrusteesId.toString -> Json.arr(
        Json.obj(
          TrusteeDetailsId.toString -> personDetails
        )
      )
    )
  ))
  private val fakeAddressLookupConnector = new AddressLookupConnector {
    override def addressLookupByPostCode(postcode: String)(implicit hc: HeaderCarrier, ec: ExecutionContext):
    Future[Option[Seq[TolerantAddress]]] = {
      Future.successful(Some(Seq(address)))
    }
  }

  private def requestResult[T](request: (Application) => Request[T], test: (Request[_], Future[Result]) => Unit)(implicit writeable: Writeable[T]): Unit = {
    running(_.overrides(
      bind[AuthAction].to(FakeAuthAction),
      bind[DataRetrievalAction].toInstance(retrieval),
      bind[DataRequiredAction].to(new DataRequiredActionImpl),
      bind[AddressLookupConnector].toInstance(fakeAddressLookupConnector),
      bind(classOf[Navigator]).qualifiedWith(classOf[TrusteesIndividual]).toInstance(fakeNavigator),
      bind[DataCacheConnector].toInstance(FakeDataCacheConnector),
      bind[PostCodeLookupFormProvider].to(formProvider)
    )) {
      app =>
        val req = request(app)
        val result = route[T](app, req).value
        test(req, result)
    }
  }
}
