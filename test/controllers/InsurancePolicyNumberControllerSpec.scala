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
import controllers.actions._
import controllers.behaviours.ControllerWithQuestionPageBehaviours
import forms.InsurancePolicyNumberFormProvider
import identifiers.InsurancePolicyNumberId
import models.NormalMode
import models.requests.OptionalDataRequest
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, AnyContentAsFormUrlEncoded, Result}
import play.api.test.FakeRequest
import services.{FakeUserAnswersService, UserAnswersService}
import utils.{FakeNavigator, Navigator, UserAnswers}
import views.html.insurancePolicyNumber

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class InsurancePolicyNumberControllerSpec extends ControllerWithQuestionPageBehaviours {

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

    behave like controllerThatSavesUserAnswersWithService(
      saveAction(this),
      postRequest,
      InsurancePolicyNumberId,
      policyNumber
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

  private def viewAsString(base: SpecBase)(form: Form[_] = form): Form[_] => String = form =>
    insurancePolicyNumber(base.frontendAppConfig, form, NormalMode, companyName, None, postUrl)(base.fakeRequest, base.messages).toString()

  private val allowAccess: AllowAccessForNonSuspendedUsersActionProvider = new AllowAccessForNonSuspendedUsersActionProvider {
    override def apply(srn: Option[String]): AllowAccessForNonSuspendedUsersAction = new AllowAccessForNonSuspendedUsersAction(srn) {
      override protected def filter[A](request: OptionalDataRequest[A]): Future[Option[Result]] = Future.successful(None)
    }
  }

  private def controller(base: ControllerSpecBase)(
    dataRetrievalAction: DataRetrievalAction = base.getEmptyData,
    authAction: AuthAction = FakeAuthAction,
    navigator: Navigator = FakeNavigator,
    cache: UserAnswersService = FakeUserAnswersService
  ): InsurancePolicyNumberController =
    new InsurancePolicyNumberController(
      base.frontendAppConfig,
      base.messagesApi,
      cache,
      navigator,
      authAction,
      dataRetrievalAction,
      new DataRequiredActionImpl(),
      formProvider,
      allowAccess
    )

  private def onPageLoadAction(base: ControllerSpecBase)(dataRetrievalAction: DataRetrievalAction, authAction: AuthAction): Action[AnyContent] =
    controller(base)(dataRetrievalAction, authAction).onPageLoad(NormalMode, None)

  private def onSubmitAction(base: ControllerSpecBase, navigator: Navigator)(dataRetrievalAction: DataRetrievalAction,
                                                                             authAction: AuthAction): Action[AnyContent] =
    controller(base)(dataRetrievalAction, authAction, navigator).onSubmit(NormalMode, None)

  private def saveAction(base: ControllerSpecBase)(cache: UserAnswersService): Action[AnyContent] =
    controller(base)(cache = cache).onSubmit(NormalMode, None)
}
