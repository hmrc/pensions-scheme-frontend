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

import audit.testdoubles.StubSuccessfulAuditService
import base.SpecBase
import connectors.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.actions._
import controllers.behaviours.ControllerWithQuestionPageBehaviours
import forms.address.AddressFormProvider
import identifiers._
import models.NormalMode
import models.address.Address
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import utils._
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.manualAddress

class InsurerConfirmAddressControllerSpec extends ControllerWithQuestionPageBehaviours {

  import InsurerConfirmAddressControllerSpec._

  "InsurerConfirmAddressController" when {

    behave like controllerWithOnPageLoadMethod(
      onPageLoadAction(this),
      minData,
      validData.dataRetrievalAction,
      form,
      form.fill(insurerAddressData),
      viewAsString(this)(form)
    )

    behave like controllerWithOnPageLoadMethodMissingRequiredData(
      onPageLoadAction(this),
      getEmptyData
    )

    behave like controllerWithOnSubmitMethod(
      onSubmitAction(this, navigator),
      validData.dataRetrievalAction,
      form.bind(Map.empty[String, String]),
      viewAsString(this)(form),
      postRequest
    )

    behave like controllerThatSavesUserAnswers(
      saveAction(this),
      postRequest,
      InsurerConfirmAddressId,
      insurerAddressData
    )
  }
}

object InsurerConfirmAddressControllerSpec {

  implicit val global = scala.concurrent.ExecutionContext.Implicits.global
  private val schemeName = "test scheme"
  val options = Seq(InputOption("territory:AE-AZ", "Abu Dhabi"), InputOption("country:AF", "Afghanistan"))
  val insurerAddressData = Address("address line 1", "address line 2", Some("test town"), Some("test county"), Some("test post code"), "GB")
  private val minData = UserAnswers().schemeName(schemeName).dataRetrievalAction
  private val validData: UserAnswers = UserAnswers().schemeName(schemeName).insurerConfirmAddress(insurerAddressData)

  private val postRequest: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest().withFormUrlEncodedBody(("addressLine1", "value 1"),
    ("addressLine2", "value 2"), ("postCode", "AB1 1AB"), "country" -> "GB")

  def countryOptions: CountryOptions = new CountryOptions(options)
  val fakeAuditService = new StubSuccessfulAuditService()

  val formProvider: AddressFormProvider = new AddressFormProvider(countryOptions)
  val form: Form[Address] = formProvider.apply()

  def viewAsString(base: SpecBase)(form: Form[_]): Form[_] => String =
    form =>
      manualAddress(
        base.frontendAppConfig,
        form,ManualAddressViewModel(
          routes.InsurerConfirmAddressController.onSubmit(NormalMode),
          options,
          Message("messages__benefits_insurance_addr__title"),
          Message("messages__benefits_insurance_addr__h1"),
          Some(schemeName)
        )
      )(base.fakeRequest, base.messages).toString()

  private def controller(base: ControllerSpecBase)(
    dataRetrievalAction: DataRetrievalAction = base.getEmptyData,
    authAction: AuthAction = FakeAuthAction,
    navigator: Navigator = FakeNavigator,
    cache: UserAnswersCacheConnector = FakeUserAnswersCacheConnector
  ): InsurerConfirmAddressController =
    new InsurerConfirmAddressController(
      base.frontendAppConfig,
      base.messagesApi,
      cache,
      navigator,
      authAction,
      dataRetrievalAction,
      new DataRequiredActionImpl(),
      formProvider,
      countryOptions,
      fakeAuditService
    )

  def onPageLoadAction(base: ControllerSpecBase)(dataRetrievalAction: DataRetrievalAction, authAction: AuthAction): Action[AnyContent] =
    controller(base)(dataRetrievalAction, authAction).onPageLoad(NormalMode)

  def onSubmitAction(base: ControllerSpecBase, navigator: Navigator)(dataRetrievalAction: DataRetrievalAction, authAction: AuthAction): Action[AnyContent] =
    controller(base)(dataRetrievalAction, authAction, navigator).onSubmit(NormalMode)

  def saveAction(base: ControllerSpecBase)(cache: UserAnswersCacheConnector): Action[AnyContent] =
    controller(base)(cache = cache).onSubmit(NormalMode)
}
