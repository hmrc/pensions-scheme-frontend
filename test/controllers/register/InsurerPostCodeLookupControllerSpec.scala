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

package controllers.register

import base.CSRFRequest
import connectors.{AddressLookupConnector, DataCacheConnector, FakeDataCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import forms.address.PostCodeLookupFormProvider
import identifiers.register.SchemeDetailsId
import models.NormalMode
import models.address.{Address, AddressRecord}
import models.person.PersonDetails
import models.register.SchemeDetails
import models.register.SchemeType.SingleTrust
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
import uk.gov.hmrc.http.HeaderCarrier
import utils.annotations.Register
import utils.{FakeNavigator, Navigator}
import viewmodels.address.PostcodeLookupViewModel
import views.html.address.postcodeLookup

import scala.concurrent.{ExecutionContext, Future}

class InsurerPostCodeLookupControllerSpec extends ControllerSpecBase with CSRFRequest with ScalaFutures {

  import InsurerPostCodeLookupControllerSpec._

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  "IndividualPostCodeLookup Controller" must {
    "render postCodeLookup from a GET request" in {
      requestResult(
        implicit app => addToken(FakeRequest(routes.InsurerPostCodeLookupController.onPageLoad(NormalMode))),
        (request, result) => {

          val viewModel = PostcodeLookupViewModel(
            routes.InsurerPostCodeLookupController.onSubmit(NormalMode),
            routes.InsurerAddressController.onSubmit(NormalMode),
            Messages("messages__benefits_insurance_addr__title"),
            "messages__benefits_insurance_addr__title",
            Some("Test Scheme Name"),
            None
          )

          status(result) mustBe OK
          contentAsString(result) mustBe postcodeLookup(frontendAppConfig, form, viewModel)(request, messages).toString()
        }
      )
    }

    "redirect to next page on POST request" which {
      "returns a list of addresses from addressLookup given a postcode" in {
        requestResult(
          implicit app => addToken(FakeRequest(routes.InsurerPostCodeLookupController.onSubmit(NormalMode))
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

object InsurerPostCodeLookupControllerSpec extends OptionValues {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val formProvider = new PostCodeLookupFormProvider()
  val form = formProvider()
  val personDetails = PersonDetails("Firstname", Some("Middle"), "Last", LocalDate.now())
  val validPostcode = "ZZ1 1ZZ"
  val fakeNavigator = new FakeNavigator(desiredRoute = onwardRoute)
  val address = Address("address line 1", "address line 2", None, None, Some(validPostcode), "GB")

  val retrieval = new FakeDataRetrievalAction(Some(
    Json.obj(SchemeDetailsId.toString -> SchemeDetails("Test Scheme Name", SingleTrust))
  ))

  private val fakeAddressLookupConnector = new AddressLookupConnector {
    override def addressLookupByPostCode(postcode: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Seq[AddressRecord]]] = {
      Future.successful(Some(Seq(AddressRecord(address))))
    }
  }

  private def requestResult[T](request: (Application) => Request[T], test: (Request[_], Future[Result]) => Unit)
                              (implicit writeable: Writeable[T]): Unit = {
    running(_.overrides(
      bind[AuthAction].to(FakeAuthAction),
      bind[DataRetrievalAction].toInstance(retrieval),
      bind[DataRequiredAction].to(new DataRequiredActionImpl),
      bind[AddressLookupConnector].toInstance(fakeAddressLookupConnector),
      bind(classOf[Navigator]).qualifiedWith(classOf[Register]).toInstance(fakeNavigator),
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