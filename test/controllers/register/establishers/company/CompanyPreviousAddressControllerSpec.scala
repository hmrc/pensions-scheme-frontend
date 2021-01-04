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

package controllers.register.establishers.company

import audit.testdoubles.StubSuccessfulAuditService
import audit.{AddressAction, AddressEvent}
import controllers.ControllerSpecBase
import controllers.actions._
import forms.address.AddressFormProvider
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.company.{CompanyDetailsId, CompanyPreviousAddressId}
import models.address.{Address, TolerantAddress}
import models.{CompanyDetails, Index, NormalMode}
import org.scalatest.concurrent.ScalaFutures
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import services.FakeUserAnswersService
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.{CountryOptions, FakeCountryOptions, FakeNavigator, InputOption, UserAnswers}
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.manualAddress

class CompanyPreviousAddressControllerSpec extends ControllerSpecBase with ScalaFutures {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val formProvider = new AddressFormProvider(FakeCountryOptions())
  val form = formProvider()
  val companyName = "test company name"
  val index = Index(0)
  val options = Seq(InputOption("territory:AX", "Åland Islands"), InputOption("country:ZW", "Zimbabwe"))

  def countryOptions: CountryOptions = new CountryOptions(options)

  val address = Address("address line 1", "address line 2", None, None, None, "GB")

  val fakeAuditService = new StubSuccessfulAuditService()

  private val validData = Json.obj(
    EstablishersId.toString -> Json.arr(
      Json.obj(
        CompanyDetailsId.toString -> CompanyDetails(companyName),
        CompanyPreviousAddressId.toString -> address
      )
    )
  )

  private val view = injector.instanceOf[manualAddress]

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisherCompany): CompanyPreviousAddressController =
    new CompanyPreviousAddressController(
      frontendAppConfig,
      messagesApi,
      FakeUserAnswersService,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      formProvider,
      countryOptions,
      fakeAuditService,
      view,
      stubMessagesControllerComponents()
    )

  def viewAsString(form: Form[_] = form): String =
    view(
      form,
      ManualAddressViewModel(
        routes.CompanyPreviousAddressController.onSubmit(NormalMode, None, index),
        options,
        Message("messages__common__confirmPreviousAddress__h1", Message("messages__theEstablisher")),
        Message("messages__common__confirmPreviousAddress__h1", companyName)
      ),
      None
    )(fakeRequest, messages).toString

  "CompanyPreviousAddress Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode, None, index)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))

      val result = controller(getRelevantData).onPageLoad(NormalMode, None, index)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(address))
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(
        ("addressLine1", "address line 1"),
        ("addressLine2", "address line 2"),
        ("postCode", "AB12 3CD"),
        "country" -> "GB")
      val result = controller().onSubmit(NormalMode, None, index)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller().onSubmit(NormalMode, None, index)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode, None, index)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("addressLine1", "address line 1"), ("addressLine2", "address line 2"))
      val result = controller(dontGetAnyData).onSubmit(NormalMode, None, index)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "send an audit event when valid data is submitted" in {

      val existingAddress = Address(
        "existing-line-1",
        "existing-line-2",
        None,
        None,
        None,
        "existing-country"
      )

      val selectedAddress = TolerantAddress(None, None, None, None, None, None)

      val data =
        UserAnswers()
          .establishersCompanyPreviousAddress(index, existingAddress)
          .establishersIndividualPreviousAddressList(index, selectedAddress)
          .dataRetrievalAction

      val postRequest = fakeRequest.withFormUrlEncodedBody(
        ("addressLine1", "value 1"),
        ("addressLine2", "value 2"),
        ("postCode", "NE1 1NE"),
        "country" -> "GB"
      )

      fakeAuditService.reset()

      val result = controller(data).onSubmit(NormalMode, None, index)(postRequest)

      whenReady(result) {
        _ =>
          fakeAuditService.verifySent(
            AddressEvent(
              FakeAuthAction.externalId,
              AddressAction.LookupChanged,
              s"Establisher Company Previous Address: $companyName",
              Address(
                "value 1",
                "value 2",
                None,
                None,
                Some("NE1 1NE"),
                "GB"
              )
            )
          )
      }
    }
  }
}
