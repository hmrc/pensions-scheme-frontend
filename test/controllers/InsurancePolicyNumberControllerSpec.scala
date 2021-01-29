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

import base.SpecBase
import base.SpecBase.controllerComponents
import controllers.actions._
import controllers.behaviours.ControllerWithQuestionPageBehaviours
import forms.InsurancePolicyNumberFormProvider
import forms.mappings.Transforms
import identifiers.InsurancePolicyNumberId
import models.NormalMode
import navigators.Navigator
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import services.{FakeUserAnswersService, UserAnswersService}
import utils.{FakeNavigator, UserAnswers}
import views.html.insurancePolicyNumber

import scala.concurrent.ExecutionContext.Implicits.global

class InsurancePolicyNumberControllerSpec extends ControllerWithQuestionPageBehaviours with Transforms {

  import InsurancePolicyNumberControllerSpec._

  "InsurancePolicyNumber Controller" must {
    behave like controllerWithOnPageLoadMethod(
      onPageLoadAction(this),
      mandatoryData.dataRetrievalAction,
      validData.dataRetrievalAction,
      form,
      form.fill(policyNumber),
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
      InsurancePolicyNumberId,
      noSpaceWithUpperCaseTransform(policyNumber)
    )
  }
}
object InsurancePolicyNumberControllerSpec {

  val policyNumber = "test policy number"
  val companyName = "test company name"
  private def postUrl = routes.InsurancePolicyNumberController.onSubmit(NormalMode, None)
  private val formProvider = new InsurancePolicyNumberFormProvider()
  private val form = formProvider.apply()
  private val mandatoryData = UserAnswers().insuranceCompanyName(companyName)

  private val validData: UserAnswers = UserAnswers().insuranceCompanyName(companyName).insurancePolicyNumber(policyNumber)

  private val postRequest: FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest().withFormUrlEncodedBody(("policyNumber", policyNumber))

  private def viewAsString(base: SpecBase)(form: Form[_]): Form[_] => String = form => {
    val view = base.injector.instanceOf[insurancePolicyNumber]
    view(form, NormalMode, Some(companyName), None, postUrl, None)(base.fakeRequest, base.messages).toString()
  }

  private def controller(base: ControllerSpecBase)(
    dataRetrievalAction: DataRetrievalAction = base.getEmptyData,
    authAction: AuthAction = FakeAuthAction,
    navigator: Navigator = FakeNavigator,
    cache: UserAnswersService = FakeUserAnswersService
  ): InsurancePolicyNumberController = {
    val view = base.injector.instanceOf[insurancePolicyNumber]
    new InsurancePolicyNumberController(
      base.frontendAppConfig,
      base.messagesApi,
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
  }

  private def onPageLoadAction(base: ControllerSpecBase)(dataRetrievalAction: DataRetrievalAction, authAction: AuthAction): Action[AnyContent] =
    controller(base)(dataRetrievalAction, authAction).onPageLoad(NormalMode, None)

  private def onSubmitAction(base: ControllerSpecBase, navigator: Navigator)(dataRetrievalAction: DataRetrievalAction,
                                                                             authAction: AuthAction): Action[AnyContent] =
    controller(base)(dataRetrievalAction, authAction, navigator).onSubmit(NormalMode, None)

  private def saveAction(base: ControllerSpecBase)(cache: UserAnswersService): Action[AnyContent] =
    controller(base)(cache = cache).onSubmit(NormalMode, None)
}



