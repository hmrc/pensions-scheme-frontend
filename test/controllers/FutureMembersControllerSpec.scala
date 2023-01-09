/*
 * Copyright 2023 HM Revenue & Customs
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
import forms.FutureMembersFormProvider
import identifiers.FutureMembersId
import models.{Members, NormalMode}
import navigators.Navigator
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest

import utils.{FakeNavigator, UserAnswers}
import views.html.futureMembers

class FutureMembersControllerSpec extends ControllerWithQuestionPageBehaviours {

  private val view = injector.instanceOf[futureMembers]
  private val schemeName = "Test Scheme Name"
  private val formProvider = new FutureMembersFormProvider()
  private val form = formProvider.apply(schemeName)
  private val validData: UserAnswers = UserAnswers().schemeName(schemeName).futureMembers(Members.values.head)
  private val postRequest: FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest().withFormUrlEncodedBody(("value", Members.values.head.toString))

  private def viewAsString(form: Form[_]): Form[_] => String = form =>
    view(form, NormalMode, schemeName)(fakeRequest, messages).toString()

  private def controller(
    dataRetrievalAction: DataRetrievalAction = getEmptyData,
    authAction: AuthAction = FakeAuthAction,
    navigator: Navigator = FakeNavigator,
    cache: UserAnswersCacheConnector = FakeUserAnswersCacheConnector
  ): FutureMembersController =
    new FutureMembersController(
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

  private def onPageLoadAction(dataRetrievalAction: DataRetrievalAction, authAction: AuthAction): Action[AnyContent] =
    controller(dataRetrievalAction, authAction).onPageLoad(NormalMode)

  private def onSubmitAction(navigator: Navigator)(dataRetrievalAction: DataRetrievalAction,
                                                                             authAction: AuthAction): Action[AnyContent] =
    controller(dataRetrievalAction, authAction, navigator).onSubmit(NormalMode)

  private def saveAction(cache: UserAnswersCacheConnector): Action[AnyContent] =
    controller(cache = cache).onSubmit(NormalMode)

  "Future Members Controller" when {

    behave like controllerWithOnPageLoadMethod(
      onPageLoadAction,
      getMandatorySchemeNameHs,
      validData.dataRetrievalAction,
      form,
      form.fill(Members.values.head),
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
      FutureMembersId,
      Members.values.head
    )
  }
}


