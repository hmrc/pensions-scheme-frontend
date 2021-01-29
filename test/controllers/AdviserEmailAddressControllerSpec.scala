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

package controllers


import connectors.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.actions.{AuthAction, DataRequiredActionImpl, DataRetrievalAction, FakeAuthAction}
import controllers.behaviours.ControllerWithQuestionPageBehaviours
import forms.register.adviser.AdviserEmailFormProvider
import identifiers.AdviserEmailId
import models.NormalMode
import navigators.Navigator
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest

import utils.{FakeNavigator, UserAnswers}
import views.html.adviserEmailAddress

class AdviserEmailAddressControllerSpec extends ControllerSpecBase with ControllerWithQuestionPageBehaviours {

  val formProvider: AdviserEmailFormProvider = new AdviserEmailFormProvider()
  val form: Form[String] = formProvider.apply()

  private val emailAddress = "test@test.com"
  private val adviserName = "test scheme"
  private val minData = UserAnswers().adviserName(adviserName).dataRetrievalAction
  private val validData: UserAnswers = UserAnswers().adviserName(adviserName).adviserEmailAddress(emailAddress)

  private val postRequest: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest().withFormUrlEncodedBody(("email", emailAddress))

  private val view = injector.instanceOf[adviserEmailAddress]
  def viewAsString(form: Form[_]): Form[_] => String =
    form =>
      view(form,
        NormalMode,
        adviserName,
        None
      )(fakeRequest, messages).toString()

  private def controller(
    dataRetrievalAction: DataRetrievalAction = getEmptyData,
    authAction: AuthAction = FakeAuthAction,
    navigator: Navigator = FakeNavigator,
    cache: UserAnswersCacheConnector = FakeUserAnswersCacheConnector
  ): AdviserEmailAddressController =
    new AdviserEmailAddressController(
      frontendAppConfig,
      messagesApi,
      authAction,
      navigator,
      dataRetrievalAction,
      new DataRequiredActionImpl(),
      formProvider,
      cache,
      controllerComponents,
      view
    )

  def onPageLoadAction(dataRetrievalAction: DataRetrievalAction, authAction: AuthAction): Action[AnyContent] =
    controller(dataRetrievalAction, authAction).onPageLoad(NormalMode)

  def onSubmitAction(navigator: Navigator)(dataRetrievalAction: DataRetrievalAction, authAction: AuthAction): Action[AnyContent] =
    controller(dataRetrievalAction, authAction, navigator).onSubmit(NormalMode)

  def saveAction(cache: UserAnswersCacheConnector): Action[AnyContent] =
    controller(cache = cache).onSubmit(NormalMode)

  "AdviserEmailAddressController" when {

    behave like controllerWithOnPageLoadMethod(
      onPageLoadAction,
      minData,
      validData.dataRetrievalAction,
      form,
      form.fill(emailAddress),
      viewAsString(form)
    )

    behave like controllerWithOnPageLoadMethodMissingRequiredData(
      onPageLoadAction,
      getEmptyData
    )

    behave like controllerWithOnSubmitMethod(
      onSubmitAction(navigator),
      validData.dataRetrievalAction,
      form.bind(Map.empty[String, String]),
      viewAsString(form),
      postRequest
    )

    behave like controllerThatSavesUserAnswers(
      saveAction,
      postRequest,
      AdviserEmailId,
      emailAddress
    )
  }
}



