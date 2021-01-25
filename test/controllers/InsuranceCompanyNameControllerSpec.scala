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
import forms.InsuranceCompanyNameFormProvider
import identifiers.InsuranceCompanyNameId
import models.NormalMode
import navigators.Navigator
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, AnyContentAsFormUrlEncoded, Call}
import play.api.test.FakeRequest
import services.{FakeUserAnswersService, UserAnswersService}

import utils.{FakeNavigator, UserAnswers}
import views.html.insuranceCompanyName

class InsuranceCompanyNameControllerSpec extends ControllerWithQuestionPageBehaviours {

  val schemeName = "Test Scheme Name"
  val companyName = "test company name"

  private val formProvider = new InsuranceCompanyNameFormProvider()
  private val form = formProvider.apply()

  private val validData: UserAnswers = UserAnswers().schemeName(schemeName).insuranceCompanyName(companyName)

  private val postRequest: FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest().withFormUrlEncodedBody(("companyName", companyName))

  private def postCall: Call = controllers.routes.InsuranceCompanyNameController.onSubmit(NormalMode, None)

  private val view = injector.instanceOf[insuranceCompanyName]
  private def viewAsString(form: Form[_]): Form[_] => String = form =>
    view(form, NormalMode, Some(schemeName), postCall, None)(fakeRequest, messages).toString()

  private def controller(
    dataRetrievalAction: DataRetrievalAction = getEmptyData,
    authAction: AuthAction = FakeAuthAction,
    navigator: Navigator = FakeNavigator,
    cache: UserAnswersService = FakeUserAnswersService
  ): InsuranceCompanyNameController =
    new InsuranceCompanyNameController(
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
      view
    )

  private def onPageLoadAction(dataRetrievalAction: DataRetrievalAction, authAction: AuthAction): Action[AnyContent] =
    controller(dataRetrievalAction, authAction).onPageLoad(NormalMode, None)

  private def onSubmitAction(navigator: Navigator)(dataRetrievalAction: DataRetrievalAction,
                                                                             authAction: AuthAction): Action[AnyContent] =
    controller(dataRetrievalAction, authAction, navigator).onSubmit(NormalMode, None)

  private def saveAction(cache: UserAnswersService): Action[AnyContent] =
    controller(cache = cache).onSubmit(NormalMode, None)

  "InsuranceCompanyName Controller" must {

    behave like controllerWithOnPageLoadMethod(
      onPageLoadAction,
      getMandatorySchemeNameHs,
      validData.dataRetrievalAction,
      form,
      form.fill(companyName),
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
      InsuranceCompanyNameId,
      companyName
    )
  }
}


