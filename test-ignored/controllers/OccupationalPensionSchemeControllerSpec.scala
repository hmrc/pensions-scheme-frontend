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
import controllers.actions._
import controllers.behaviours.ControllerWithQuestionPageBehaviours
import forms.OccupationalPensionSchemeFormProvider
import identifiers.{OccupationalPensionSchemeId, SchemeNameId}
import models.NormalMode
import navigators.Navigator
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import utils.{FakeNavigator, UserAnswers}
import views.html.occupationalPensionScheme

import scala.concurrent.ExecutionContext.Implicits.global

class OccupationalPensionSchemeControllerSpec extends ControllerWithQuestionPageBehaviours {

  import OccupationalPensionSchemeControllerSpec._

  "Occupational Pension Scheme Controller" when {

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
      OccupationalPensionSchemeId,
      true
    )
  }
}

object OccupationalPensionSchemeControllerSpec {

  private val formProvider = new OccupationalPensionSchemeFormProvider()
  private val form = formProvider.apply()
  private val validData: UserAnswers = UserAnswers(Json.obj(
    SchemeNameId.toString -> "Test Scheme Name")).occupationalPensionScheme(true)
  private val postRequest: FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest().withFormUrlEncodedBody(("value", "true"))

  private def viewAsString(base: SpecBase)(form: Form[_] = form): Form[_] => String = form =>
    occupationalPensionScheme(base.frontendAppConfig, form, NormalMode, Some("Test Scheme Name"))(base.fakeRequest, base.messages).toString()

  private def controller(base: ControllerSpecBase)(
    dataRetrievalAction: DataRetrievalAction = base.getEmptyData,
    authAction: AuthAction = FakeAuthAction,
    navigator: Navigator = FakeNavigator,
    cache: UserAnswersCacheConnector = FakeUserAnswersCacheConnector
  ): OccupationalPensionSchemeController =
    new OccupationalPensionSchemeController(
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