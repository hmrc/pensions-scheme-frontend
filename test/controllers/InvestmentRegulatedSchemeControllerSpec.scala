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

import base.SpecBase
import connectors.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.actions.{AuthAction, DataRequiredActionImpl, DataRetrievalAction, FakeAuthAction}
import controllers.behaviours.ControllerWithQuestionPageBehaviours
import forms.InvestmentRegulatedSchemeFormProvider
import identifiers.InvestmentRegulatedSchemeId
import models.NormalMode
import navigators.Navigator
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import utils.{FakeNavigator, UserAnswers}
import views.html.investmentRegulatedScheme

import scala.concurrent.ExecutionContext.Implicits.global

class InvestmentRegulatedSchemeControllerSpec extends ControllerWithQuestionPageBehaviours {

  import InvestmentRegulatedSchemeControllerSpec._
  "Investment regulated scheme Controller" when {

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

    behave like controllerThatSavesUserAnswers(
      saveAction(this),
      postRequest,
      InvestmentRegulatedSchemeId,
      true
    )
  }
}

object InvestmentRegulatedSchemeControllerSpec {
  private val schemeName = "Test Scheme Name"
  private val formProvider = new InvestmentRegulatedSchemeFormProvider()
  private val form = formProvider.apply()
  private val validData: UserAnswers = UserAnswers().schemeName(schemeName).investmentRegulated(true)
  private val postRequest: FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest().withFormUrlEncodedBody(("value", "true"))

  private def viewAsString(base: SpecBase)(form: Form[_] = form): Form[_] => String = form =>
    investmentRegulatedScheme(base.frontendAppConfig, form, NormalMode, Some(schemeName))(base.fakeRequest, base.messages).toString()

  private def controller(base: ControllerSpecBase)(
    dataRetrievalAction: DataRetrievalAction = base.getEmptyData,
    authAction: AuthAction = FakeAuthAction,
    navigator: Navigator = FakeNavigator,
    cache: UserAnswersCacheConnector = FakeUserAnswersCacheConnector
  ): InvestmentRegulatedSchemeController =
    new InvestmentRegulatedSchemeController(
      base.frontendAppConfig,
      base.messagesApi,
      cache,
      navigator,
      authAction,
      dataRetrievalAction,
      new DataRequiredActionImpl(),
      formProvider
    )

  private def onPageLoadAction(base: ControllerSpecBase)(dataRetrievalAction: DataRetrievalAction, authAction: AuthAction): Action[AnyContent] =
    controller(base)(dataRetrievalAction, authAction).onPageLoad(NormalMode)

  private def onSubmitAction(base: ControllerSpecBase, navigator: Navigator)(dataRetrievalAction: DataRetrievalAction,
                                                                             authAction: AuthAction): Action[AnyContent] =
    controller(base)(dataRetrievalAction, authAction, navigator).onSubmit(NormalMode)

  private def saveAction(base: ControllerSpecBase)(cache: UserAnswersCacheConnector): Action[AnyContent] =
    controller(base)(cache = cache).onSubmit(NormalMode)
}




