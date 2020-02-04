/*
 * Copyright 2020 HM Revenue & Customs
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

import base.SpecBase
import controllers.actions.{AuthAction, DataRequiredActionImpl, DataRetrievalAction, FakeAllowAccessProvider, FakeAuthAction}
import controllers.behaviours.ControllerWithQuestionPageBehaviours
import forms.BenefitsSecuredByInsuranceFormProvider
import identifiers.{BenefitsSecuredByInsuranceId, SchemeNameId}
import models.NormalMode
import navigators.Navigator
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import services.{FakeUserAnswersService, UserAnswersService}
import utils.{FakeNavigator, UserAnswers}
import views.html.benefitsSecuredByInsurance

import scala.concurrent.ExecutionContext.Implicits.global

class BenefitsSecuredByInsuranceControllerSpec extends ControllerWithQuestionPageBehaviours {

  import BenefitsSecuredByInsuranceControllerSpec._

  "Benefits secured by insurance Controller" when {

    behave like controllerWithOnPageLoadMethod(
      onPageLoadAction(this),
      getMandatorySchemeNameHs,
      validData.dataRetrievalAction,
      form,
      form.fill(true),
      viewAsString(this)(form)
    )

    behave like controllerWithOnSubmitMethod(
      onSubmitAction(this, navigator),
      validData.dataRetrievalAction,
      form.bind(Map.empty[String, String]),
      viewAsString(this)(form),
      postRequest
    )

    behave like controllerThatSavesUserAnswersWithService(
      saveAction(this),
      postRequest,
      BenefitsSecuredByInsuranceId,
      true
    )
  }
}

object BenefitsSecuredByInsuranceControllerSpec {
  private val formProvider = new BenefitsSecuredByInsuranceFormProvider()
  private val form = formProvider.apply()
  private val validData: UserAnswers = UserAnswers(Json.obj(
    SchemeNameId.toString -> "Test Scheme Name")).benefitsSecuredByInsurance(true)
  private val postRequest: FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest().withFormUrlEncodedBody(("value", "true"))
  private val postCall = controllers.routes.BenefitsSecuredByInsuranceController.onSubmit(NormalMode, None)

  private def viewAsString(base: SpecBase)(form: Form[_] = form): Form[_] => String = form =>
    benefitsSecuredByInsurance(base.frontendAppConfig, form, NormalMode, Some("Test Scheme Name"), postCall, None)(base.fakeRequest, base.messages).toString()

  private def controller(base: ControllerSpecBase)(
    dataRetrievalAction: DataRetrievalAction = base.getEmptyData,
    authAction: AuthAction = FakeAuthAction,
    navigator: Navigator = FakeNavigator,
    cache: UserAnswersService = FakeUserAnswersService
  ): BenefitsSecuredByInsuranceController =
    new BenefitsSecuredByInsuranceController(
      base.frontendAppConfig,
      base.messagesApi,
      cache,
      navigator,
      authAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl(),
      formProvider
    )

  private def onPageLoadAction(base: ControllerSpecBase)(dataRetrievalAction: DataRetrievalAction, authAction: AuthAction): Action[AnyContent] =
    controller(base)(dataRetrievalAction, authAction).onPageLoad(NormalMode, None)

  private def onSubmitAction(base: ControllerSpecBase, navigator: Navigator)(dataRetrievalAction: DataRetrievalAction,
                                                                             authAction: AuthAction): Action[AnyContent] =
    controller(base)(dataRetrievalAction, authAction, navigator).onSubmit(NormalMode, None)

  private def saveAction(base: ControllerSpecBase)(cache: UserAnswersService): Action[AnyContent] =
    controller(base)(cache = cache).onSubmit(NormalMode, None)
}


