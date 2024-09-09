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


import connectors.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.actions.{AuthAction, DataRequiredActionImpl, DataRetrievalAction, FakeAuthAction}
import controllers.behaviours.ControllerWithQuestionPageBehaviours
import forms.register.adviser.AdviserPhoneFormProvider
import identifiers.AdviserPhoneId
import models.NormalMode
import navigators.Navigator
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest

import utils.{FakeNavigator, UserAnswers}
import views.html.adviserPhone

class AdviserPhoneControllerSpec extends ControllerSpecBase with ControllerWithQuestionPageBehaviours {

  private val view = injector.instanceOf[adviserPhone]
  val formProvider: AdviserPhoneFormProvider = new AdviserPhoneFormProvider()
  val form: Form[String] = formProvider.apply()
  private val schemeName = "Scheme Name"

  private val phone = "0000"
  private val adviserName = "test scheme"
  private val minData = UserAnswers().adviserName(adviserName).schemeName(schemeName).dataRetrievalAction
  private val validData: UserAnswers = UserAnswers().adviserName(adviserName).adviserPhone(phone).schemeName(schemeName)

  private val postRequest: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest().withFormUrlEncodedBody(("phone", phone))

  def viewAsString(form: Form[_]): Form[_] => String =
    form =>
     view(
        form,
        NormalMode,
        adviserName,
        schemeName,
        srn
      )(fakeRequest, messages).toString()

  private def controller(
    dataRetrievalAction: DataRetrievalAction = getEmptyData,
    authAction: AuthAction = FakeAuthAction,
    navigator: Navigator = FakeNavigator,
    cache: UserAnswersCacheConnector = FakeUserAnswersCacheConnector
  ): AdviserPhoneController =
    new AdviserPhoneController(
      frontendAppConfig,
      messagesApi,
      cache,
      navigator,
      authAction,
      dataRetrievalAction,
      new DataRequiredActionImpl(),
      formProvider,
      controllerComponents,
      view
    )

  def onPageLoadAction(dataRetrievalAction: DataRetrievalAction, authAction: AuthAction): Action[AnyContent] =
    controller(dataRetrievalAction, authAction).onPageLoad(NormalMode, srn)

  def onSubmitAction(navigator: Navigator)(dataRetrievalAction: DataRetrievalAction, authAction: AuthAction): Action[AnyContent] =
    controller(dataRetrievalAction, authAction, navigator).onSubmit(NormalMode, srn)

  def saveAction(cache: UserAnswersCacheConnector): Action[AnyContent] =
    controller(cache = cache).onSubmit(NormalMode, srn)

  "AdviserPhoneController" when {

    behave like controllerWithOnPageLoadMethod(
      onPageLoadAction,
      minData,
      validData.dataRetrievalAction,
      form,
      form.fill(phone),
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
      AdviserPhoneId,
      phone
    )
  }
}



