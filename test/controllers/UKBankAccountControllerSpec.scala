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
import connectors.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.actions._
import controllers.behaviours.ControllerWithQuestionPageBehaviours
import forms.UKBankAccountFormProvider
import identifiers.IsAboutBankDetailsCompleteId
import models.NormalMode
import navigators.Navigator
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import utils.{FakeNavigator, FakeSectionComplete, SectionComplete, UserAnswers}
import views.html.uKBankAccount

import scala.concurrent.ExecutionContext.Implicits.global

class UKBankAccountControllerSpec extends ControllerWithQuestionPageBehaviours {

  import UKBankAccountControllerSpec._

  "UKBankAccount Controller" must {

    behave like controllerWithOnPageLoadMethod(
      onPageLoadAction(this),
      getMandatorySchemeNameHs,
      validData.dataRetrievalAction,
      form,
      form.fill(true),
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
      identifiers.UKBankAccountId,
      true
    )

    "calling onSubmit" must {
      "set the IsAboutBankDetailsCompleteId to false to change to in progress status" in {
        controller(this)(dataRetrievalAction = validData.dataRetrievalAction, sectionComplete = FakeSectionComplete).onSubmit(NormalMode)(postRequest)

        FakeSectionComplete.verify(IsAboutBankDetailsCompleteId, false)
      }
    }
  }
}

object UKBankAccountControllerSpec {
  private val schemeName = "Test Scheme Name"
  private val formProvider = new UKBankAccountFormProvider()
  private val form = formProvider.apply()
  private val validData: UserAnswers = UserAnswers().schemeName(schemeName).schemeUkBankAccount(true)
  private val postRequest: FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest().withFormUrlEncodedBody(("value", "true"))

  private def viewAsString(base: SpecBase)(form: Form[_] = form): Form[_] => String = form =>
    uKBankAccount(base.frontendAppConfig, form, NormalMode, schemeName)(base.fakeRequest, base.messages).toString()

  private def controller(base: ControllerSpecBase)(
    dataRetrievalAction: DataRetrievalAction = base.getEmptyData,
    authAction: AuthAction = FakeAuthAction,
    navigator: Navigator = FakeNavigator,
    cache: UserAnswersCacheConnector = FakeUserAnswersCacheConnector,
    sectionComplete: SectionComplete = FakeSectionComplete
  ): UKBankAccountController =
    new UKBankAccountController(
      base.frontendAppConfig,
      base.messagesApi,
      cache,
      navigator,
      authAction,
      dataRetrievalAction,
      new DataRequiredActionImpl(),
      formProvider,
      sectionComplete
    )

  private def onPageLoadAction(base: ControllerSpecBase)(dataRetrievalAction: DataRetrievalAction, authAction: AuthAction): Action[AnyContent] =
    controller(base)(dataRetrievalAction, authAction).onPageLoad(NormalMode)

  private def onSubmitAction(base: ControllerSpecBase, navigator: Navigator)(dataRetrievalAction: DataRetrievalAction,
                                                                             authAction: AuthAction): Action[AnyContent] =
    controller(base)(dataRetrievalAction, authAction, navigator).onSubmit(NormalMode)

  private def saveAction(base: ControllerSpecBase)(cache: UserAnswersCacheConnector): Action[AnyContent] =
    controller(base)(cache = cache).onSubmit(NormalMode)
}
