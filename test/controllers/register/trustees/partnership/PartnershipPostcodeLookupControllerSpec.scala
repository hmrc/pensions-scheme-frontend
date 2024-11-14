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

package controllers.register.trustees.partnership

import connectors.AddressLookupConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.address.PostCodeLookupFormProvider
import identifiers.register.trustees.TrusteesId
import identifiers.register.trustees.partnership.PartnershipDetailsId
import models.address.TolerantAddress
import models.{EmptyOptionalSchemeReferenceNumber, Index, NormalMode, PartnershipDetails}
import navigators.Navigator
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.concurrent.ScalaFutures
import play.api.data.Form
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.CSRFTokenHelper.addCSRFToken
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{FakeUserAnswersService, UserAnswersService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.FakeNavigator
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel
import views.html.address.postcodeLookup

import scala.concurrent.{ExecutionContext, Future}

class PartnershipPostcodeLookupControllerSpec extends ControllerSpecBase with ScalaFutures {

  import PartnershipPostcodeLookupControllerSpec._

  private val view = injector.instanceOf[postcodeLookup]

  "IndividualPostCodeLookup Controller" must {
    "render postCodeLookup from a GET request" in {
      running(_.overrides(
        bind[AuthAction].to(FakeAuthAction),
        bind[DataRetrievalAction].toInstance(retrieval),
        bind[DataRequiredAction].to(new DataRequiredActionImpl),
        bind[AddressLookupConnector].toInstance(fakeAddressLookupConnector),
        bind(classOf[Navigator]).toInstance(fakeNavigator),
        bind[UserAnswersService].toInstance(FakeUserAnswersService),
        bind[PostCodeLookupFormProvider].to(formProvider)
      )) {
        implicit app =>
          val request = addCSRFToken(FakeRequest())
          val controller = app.injector.instanceOf[PartnershipPostcodeLookupController]
          val result = controller.onPageLoad(NormalMode, firstIndex, EmptyOptionalSchemeReferenceNumber)(request)
          status(result) mustBe OK
          contentAsString(result) mustBe view(form, viewModel, None)(request, messages).toString()
      }
    }

    "redirect to next page on POST request" which {
      "returns a list of addresses from addressLookup given a postcode" in {
        running(_.overrides(
          bind[AuthAction].to(FakeAuthAction),
          bind[DataRetrievalAction].toInstance(retrieval),
          bind[DataRequiredAction].to(new DataRequiredActionImpl),
          bind[AddressLookupConnector].toInstance(fakeAddressLookupConnector),
          bind(classOf[Navigator]).toInstance(fakeNavigator),
          bind[UserAnswersService].toInstance(FakeUserAnswersService),
          bind[PostCodeLookupFormProvider].to(formProvider)
        )) {
          implicit app =>
            val request = addCSRFToken(FakeRequest()
              .withFormUrlEncodedBody("postcode" -> validPostcode))
            val controller = app.injector.instanceOf[PartnershipPostcodeLookupController]
            val result = controller.onSubmit( NormalMode,  Index(0), EmptyOptionalSchemeReferenceNumber)(request)
            status(result) mustBe SEE_OTHER
            redirectLocation(result) mustBe Some(onwardRoute.url)
        }

      }
    }
  }
}

object PartnershipPostcodeLookupControllerSpec extends ControllerSpecBase with MockitoSugar {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad

  val firstIndex: Index = Index(0)
  val formProvider = new PostCodeLookupFormProvider()
  val form: Form[String] = formProvider()
  val partnershipDetails: PartnershipDetails = PartnershipDetails("test partnership name")
  val validPostcode = "ZZ1 1ZZ"
  lazy val fakeNavigator = new FakeNavigator(desiredRoute = onwardRoute)
  val address: TolerantAddress = TolerantAddress(Some("address line 1"), Some("address line 2"), None, None, Some(validPostcode), Some("GB"))

  lazy val viewModel: PostcodeLookupViewModel = PostcodeLookupViewModel(
    postCall = routes.PartnershipPostcodeLookupController.onSubmit(NormalMode, firstIndex, EmptyOptionalSchemeReferenceNumber),
    manualInputCall = routes.PartnershipAddressController.onPageLoad(NormalMode, firstIndex, EmptyOptionalSchemeReferenceNumber),
    title = Message("messages__partnershipPostcodeLookup__title"),
    heading = Message("messages__partnershipPostcodeLookup__heading", partnershipDetails.name),
    subHeading = Some(partnershipDetails.name)
  )

  val retrieval = new FakeDataRetrievalAction(Some(
    Json.obj(
      TrusteesId.toString -> Json.arr(
        Json.obj(
          PartnershipDetailsId.toString -> partnershipDetails
        )
      )
    )
  ))
  private val fakeAddressLookupConnector = new AddressLookupConnector {
    override def addressLookupByPostCode(postcode: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[TolerantAddress]] = {
      Future.successful(Seq(address))
    }
  }
}
