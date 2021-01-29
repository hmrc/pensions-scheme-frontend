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

import controllers.actions._
import controllers.behaviours.ControllerWithQuestionPageBehaviours
import forms.TypeOfBenefitsFormProvider
import identifiers.{SchemeNameId, TypeOfBenefitsId}
import models.FeatureToggle.Enabled
import models.FeatureToggleName.TCMP
import models.{NormalMode, TypeOfBenefits}
import navigators.Navigator
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, AnyContentAsFormUrlEncoded, Call}
import play.api.test.FakeRequest
import services.{FakeUserAnswersService, FeatureToggleService, UserAnswersService}
import utils.{FakeNavigator, UserAnswers}
import views.html.typeOfBenefits
import org.mockito.Matchers._
import org.mockito.Mockito._

import scala.concurrent.Future

class TypeOfBenefitsControllerSpec extends ControllerWithQuestionPageBehaviours with MockitoSugar {

  private val view = injector.instanceOf[typeOfBenefits]
  private val formProvider = new TypeOfBenefitsFormProvider()
  private val form = formProvider.apply("Test Scheme Name")
  private val validData: UserAnswers = UserAnswers(Json.obj(
    SchemeNameId.toString -> "Test Scheme Name")).typeOfBenefits(TypeOfBenefits.values.head)
  private val postRequest: FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest().withFormUrlEncodedBody(("value", TypeOfBenefits.values.head.toString))
  private val postCall: Call = routes.TypeOfBenefitsController.onSubmit(NormalMode, None)
  private val featureToggleService: FeatureToggleService = mock[FeatureToggleService]


  private def viewAsString(form: Form[_]): Form[_] => String = form =>
    view(form, postCall, Some("Test Scheme Name"))(fakeRequest, messages).toString()

  private def controller(
    dataRetrievalAction: DataRetrievalAction = getEmptyData,
    authAction: AuthAction = FakeAuthAction,
    navigator: Navigator = FakeNavigator,
    cache: UserAnswersService = FakeUserAnswersService
  ): TypeOfBenefitsController =
    new TypeOfBenefitsController(
      frontendAppConfig,
      messagesApi,
      cache,
      navigator,
      authAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl(),
      formProvider,
      controllerComponents,
      view,
      featureToggleService
    )

  private def onPageLoadAction(dataRetrievalAction: DataRetrievalAction, authAction: AuthAction): Action[AnyContent] =
    controller(dataRetrievalAction, authAction).onPageLoad(NormalMode, None)

  private def onSubmitAction(navigator: Navigator)(dataRetrievalAction: DataRetrievalAction,
                                                                             authAction: AuthAction): Action[AnyContent] =
    controller(dataRetrievalAction, authAction, navigator).onSubmit(NormalMode, None)

  private def saveAction: UserAnswersService => Action[AnyContent] = cache =>
    controller(cache = cache).onSubmit(NormalMode, None)

  "Type of benefits Controller" when {
    when(featureToggleService.get(any())(any(), any())).thenReturn(Future.successful(Enabled(TCMP)))

    behave like controllerWithOnPageLoadMethod(
      onPageLoadAction,
      getMandatorySchemeNameHs,
      validData.dataRetrievalAction,
      form,
      form.fill(TypeOfBenefits.values.head),
      viewAsString(form)
    )

    behave like controllerWithOnSubmitMethod(
      onSubmitAction(navigator),
      validData.dataRetrievalAction,
      form.bind(Map.empty[String, String]),
      viewAsString(form),
      postRequest
    )

    behave like controllerThatSavesUserAnswersWithService(
      saveAction,
      postRequest,
      TypeOfBenefitsId,
      TypeOfBenefits.values.head
    )
  }
}

