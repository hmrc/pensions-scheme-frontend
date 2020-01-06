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

  import AdviserEmailAddressControllerSpec._

  "AdviserEmailAddressController" when {

    behave like controllerWithOnPageLoadMethod(
      onPageLoadAction(this),
      minData,
      validData.dataRetrievalAction,
      form,
      form.fill(emailAddress),
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
      AdviserEmailId,
      emailAddress
    )
  }

}

object AdviserEmailAddressControllerSpec {
  implicit val global = scala.concurrent.ExecutionContext.Implicits.global
  val formProvider: AdviserEmailFormProvider = new AdviserEmailFormProvider()
  val form: Form[String] = formProvider.apply()

  private val emailAddress = "test@test.com"
  private val adviserName = "test scheme"
  private val minData = UserAnswers().adviserName(adviserName).dataRetrievalAction
  private val validData: UserAnswers = UserAnswers().adviserName(adviserName).adviserEmailAddress(emailAddress)

  private val postRequest: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest().withFormUrlEncodedBody(("email", emailAddress))

  def viewAsString(base: SpecBase)(form: Form[_]): Form[_] => String =
    form =>
      adviserEmailAddress(
        base.frontendAppConfig,
        form,
        NormalMode,
        adviserName,
        None
      )(base.fakeRequest, base.messages).toString()

  private def controller(base: ControllerSpecBase)(
    dataRetrievalAction: DataRetrievalAction = base.getEmptyData,
    authAction: AuthAction = FakeAuthAction,
    navigator: Navigator = FakeNavigator,
    cache: UserAnswersCacheConnector = FakeUserAnswersCacheConnector
  ): AdviserEmailAddressController =
    new AdviserEmailAddressController(
      base.frontendAppConfig,
      base.messagesApi,
      authAction,
      navigator,
      dataRetrievalAction,
      new DataRequiredActionImpl(),
      formProvider,
      cache
    )

  def onPageLoadAction(base: ControllerSpecBase)(dataRetrievalAction: DataRetrievalAction, authAction: AuthAction): Action[AnyContent] =
    controller(base)(dataRetrievalAction, authAction).onPageLoad(NormalMode)

  def onSubmitAction(base: ControllerSpecBase, navigator: Navigator)(dataRetrievalAction: DataRetrievalAction, authAction: AuthAction): Action[AnyContent] =
    controller(base)(dataRetrievalAction, authAction, navigator).onSubmit(NormalMode)

  def saveAction(base: ControllerSpecBase)(cache: UserAnswersCacheConnector): Action[AnyContent] =
    controller(base)(cache = cache).onSubmit(NormalMode)
}



