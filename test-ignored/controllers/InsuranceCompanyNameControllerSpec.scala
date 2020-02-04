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

import scala.concurrent.ExecutionContext.Implicits.global

class InsuranceCompanyNameControllerSpec extends ControllerWithQuestionPageBehaviours {

  import InsuranceCompanyNameControllerSpec._

  "InsuranceCompanyName Controller" must {

    behave like controllerWithOnPageLoadMethod(
      onPageLoadAction(this),
      getMandatorySchemeNameHs,
      validData.dataRetrievalAction,
      form,
      form.fill(companyName),
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
      InsuranceCompanyNameId,
      companyName
    )
  }
}
object InsuranceCompanyNameControllerSpec extends SpecBase {

  val schemeName = "Test Scheme Name"
  val companyName = "test company name"

  private val formProvider = new InsuranceCompanyNameFormProvider()
  private val form = formProvider.apply()

  private val validData: UserAnswers = UserAnswers().schemeName(schemeName).insuranceCompanyName(companyName)

  private val postRequest: FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest().withFormUrlEncodedBody(("companyName", companyName))

  private val postCall: Call = controllers.routes.InsuranceCompanyNameController.onSubmit(NormalMode, None)

  private def viewAsString(base: SpecBase)(form: Form[_] = form): Form[_] => String = form =>
    insuranceCompanyName(base.frontendAppConfig, form, NormalMode, Some(schemeName), postCall, None)(base.fakeRequest, base.messages).toString()

  private def controller(base: ControllerSpecBase)(
    dataRetrievalAction: DataRetrievalAction = base.getEmptyData,
    authAction: AuthAction = FakeAuthAction,
    navigator: Navigator = FakeNavigator,
    cache: UserAnswersService = FakeUserAnswersService
  ): InsuranceCompanyNameController =
    new InsuranceCompanyNameController(
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


