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
import forms.BankAccountDetailsFormProvider
import models.register.SortCode
import models.{BankAccountDetails, NormalMode}
import navigators.Navigator
import org.apache.commons.lang3.RandomUtils
import java.time.LocalDate
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import utils.{FakeNavigator, UserAnswers}
import views.html.bankAccountDetails

import scala.concurrent.ExecutionContext.Implicits.global

class BankAccountDetailsControllerSpec extends ControllerWithQuestionPageBehaviours {

  import BankAccountDetailsControllerSpec._

  "BankAccountDetails Controller" must {

    behave like controllerWithOnPageLoadMethod(
      onPageLoadAction(this),
      getMandatorySchemeNameHs,
      validData.dataRetrievalAction,
      form,
      form.fill(bankDetails),
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
      identifiers.BankAccountDetailsId,
      bankDetails
    )
  }
}

object BankAccountDetailsControllerSpec {
  private val schemeName = "Test Scheme Name"
  private val formProvider = new BankAccountDetailsFormProvider()
  private val form = formProvider.apply()
  //scalastyle:off magic.number
  private val accountNo = RandomUtils.nextInt(10000000, 99999999).toString
  //scalastyle:on magic.number

  val bankDetails = BankAccountDetails("test bank", "test account",
    SortCode("34", "45", "67"), accountNo)

  private val validData: UserAnswers = UserAnswers().schemeName(schemeName).bankAccountDetails(bankDetails)
  private val postRequest: FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest().withFormUrlEncodedBody(("bankName", "test bank"),
      ("accountName", "test account"),
      ("sortCode", "344567"),
      ("accountNumber", accountNo))

  private def viewAsString(base: SpecBase)(form: Form[_] = form): Form[_] => String = form =>
    bankAccountDetails(base.frontendAppConfig, form, NormalMode, schemeName)(base.fakeRequest, base.messages).toString()

  private def controller(base: ControllerSpecBase)(
    dataRetrievalAction: DataRetrievalAction = base.getEmptyData,
    authAction: AuthAction = FakeAuthAction,
    navigator: Navigator = FakeNavigator,
    cache: UserAnswersCacheConnector = FakeUserAnswersCacheConnector
  ): BankAccountDetailsController =
    new BankAccountDetailsController(
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
