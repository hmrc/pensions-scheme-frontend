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

import audit.testdoubles.StubSuccessfulAuditService
import audit.{AddressAction, AddressEvent}
import controllers.actions._
import controllers.behaviours.ControllerWithQuestionPageBehaviours
import forms.address.AddressFormProvider
import identifiers._
import models.NormalMode
import models.address.{Address, TolerantAddress}
import navigators.Navigator
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import services.{FakeUserAnswersService, UserAnswersService}

import utils._
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.manualAddress

class InsurerConfirmAddressControllerSpec extends ControllerWithQuestionPageBehaviours {

  import InsurerConfirmAddressControllerSpec._

  val view = injector.instanceOf[manualAddress]

  def viewAsString(form: Form[_]): Form[_] => String =
    form =>
      view(
        form,ManualAddressViewModel(
          routes.InsurerConfirmAddressController.onSubmit(NormalMode, None),
          options,
          Message("messages__insurer_confirm_address__title"),
          Message("messages__common__confirmAddress__h1", insuranceCompanyName),
          None
        ),
        Some(schemeName)
      )(fakeRequest, messages).toString()

  private def controller(
                          dataRetrievalAction: DataRetrievalAction = getEmptyData,
                          authAction: AuthAction = FakeAuthAction,
                          navigator: Navigator = FakeNavigator,
                          cache: UserAnswersService = FakeUserAnswersService
                        ): InsurerConfirmAddressController =
    new InsurerConfirmAddressController(
      frontendAppConfig,
      messagesApi,
      cache,
      navigator,
      authAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl(),
      formProvider,
      countryOptions,
      fakeAuditService,
      controllerComponents,
      view
    )

  def onPageLoadAction(dataRetrievalAction: DataRetrievalAction, authAction: AuthAction): Action[AnyContent] =
    controller(dataRetrievalAction, authAction).onPageLoad(NormalMode, None)

  def onSubmitAction(navigator: Navigator)(dataRetrievalAction: DataRetrievalAction, authAction: AuthAction): Action[AnyContent] =
    controller(dataRetrievalAction, authAction, navigator).onSubmit(NormalMode, None)

  def saveAction(cachex: UserAnswersService): Action[AnyContent] =
    controller(cache = cachex).onSubmit(NormalMode, None)

  "InsurerConfirmAddressController" when {

    behave like controllerWithOnPageLoadMethod(
      onPageLoadAction,
      minData,
      validData.dataRetrievalAction,
      form,
      form.fill(insurerAddressData),
      viewAsString(form)
    )

    behave like controllerWithOnSubmitMethod(
      onSubmitAction(navigator),
      validData.dataRetrievalAction,
      form.bind(Map.empty[String, String]),
      viewAsString(form),
      postRequest
    )

    behave like controllerThatUpsertUserAnswersWithService(
      saveAction,
      postRequest,
      InsurerConfirmAddressId,
      insurerAddressData
    )

    "send an audit event when valid data is submitted" in {
      fakeAuditService.reset()
      val insurerUpdatedData = Address("address line updated", "address line 2", None, None, Some("AB1 1AB"), "country:AF")

      val validData: UserAnswers = UserAnswers().schemeName(schemeName)
        .insuranceCompanyName(insuranceCompanyName)
        .insurerConfirmAddress(insurerUpdatedData).insurerSelectAddress(selectedAddress)
      val result = controller(validData.dataRetrievalAction, FakeAuthAction).onSubmit(NormalMode, None)(postRequest)

      whenReady(result) {
        _ =>
          fakeAuditService.verifySent(
            AddressEvent(
              FakeAuthAction.externalId,
              AddressAction.Lookup,
              "Insurer Address",
              Address(
                "address line 1",
                "address line 2",
                None,
                None,
                Some("AB1 1AB"),
                "country:AF"
              )
            )
          ) mustBe true
      }
    }
  }
}

object InsurerConfirmAddressControllerSpec {

  private val schemeName = "test scheme"
  private val insuranceCompanyName = "test insurance company"
  val options = Seq(InputOption("territory:AE-AZ", "Abu Dhabi"), InputOption("country:AF", "Afghanistan"))
  val insurerAddressData = Address("address line 1", "address line 2", None, None, Some("AB1 1AB"), "country:AF")
  val selectedAddress = TolerantAddress(Some("address line 1"), Some("address line 2"), None, None, Some("AB1 1AB"), Some("country:AF"))
  private val minData = UserAnswers()
    .schemeName(schemeName)
    .insuranceCompanyName(insuranceCompanyName)
    .dataRetrievalAction
  private val validData: UserAnswers = UserAnswers().schemeName(schemeName)
    .insuranceCompanyName(insuranceCompanyName)
    .insurerConfirmAddress(insurerAddressData).insurerSelectAddress(selectedAddress)

  private val postRequest: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest().withFormUrlEncodedBody(("addressLine1", "address line 1"),
    ("addressLine2", "address line 2"), ("postCode", "AB1 1AB"), "country" -> "country:AF")

  def countryOptions: CountryOptions = new CountryOptions(options)
  val fakeAuditService = new StubSuccessfulAuditService()

  val formProvider: AddressFormProvider = new AddressFormProvider(countryOptions)
  val form: Form[Address] = formProvider.apply()

}
