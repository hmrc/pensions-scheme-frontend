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

import connectors.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.actions._
import controllers.behaviours.ControllerWithQuestionPageBehaviours
import forms.BankAccountDetailsFormProvider
import models.register.SortCode
import models.{BankAccountDetails, NormalMode}
import navigators.Navigator
import org.apache.commons.lang3.RandomUtils
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.{FakeNavigator, UserAnswers}
import views.html.bankAccountDetails


class BankAccountDetailsControllerSpec extends ControllerWithQuestionPageBehaviours {

  private val schemeName = "Test Scheme Name"
  private val formProvider = new BankAccountDetailsFormProvider()
  private val form = formProvider.apply()
  //scalastyle:off magic.number
  private val accountNo = RandomUtils.nextInt(10000000, 99999999).toString
  //scalastyle:on magic.number

  val bankDetails: BankAccountDetails = BankAccountDetails("test bank", "test account",
    SortCode("34", "45", "67"), accountNo)

  private val validData: UserAnswers = UserAnswers().schemeName(schemeName).bankAccountDetails(bankDetails)
  private val postRequest: FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest().withFormUrlEncodedBody(("bankName", "test bank"),
      ("accountName", "test account"),
      ("sortCode", "344567"),
      ("accountNumber", accountNo))


  private val view = injector.instanceOf[bankAccountDetails]

  private def viewAsString(form: Form[_] = form): Form[_] => String = form =>
    view(form, NormalMode, schemeName)(fakeRequest, messages).toString()

  private def controller(
    dataRetrievalAction: DataRetrievalAction = getEmptyData,
    authAction: AuthAction = FakeAuthAction,
    navigator: Navigator = FakeNavigator,
    cache: UserAnswersCacheConnector = FakeUserAnswersCacheConnector
  ): BankAccountDetailsController =
    new BankAccountDetailsController(
      frontendAppConfig,
      messagesApi,
      cache,
      navigator,
      authAction,
      dataRetrievalAction,
      new DataRequiredActionImpl(),
      formProvider,
      stubMessagesControllerComponents(),
      view
    )

  private def onPageLoadAction(dataRetrievalAction: DataRetrievalAction, authAction: AuthAction): Action[AnyContent] =
    controller(dataRetrievalAction, authAction).onPageLoad(NormalMode)

  private def onSubmitAction(navigator: Navigator)(dataRetrievalAction: DataRetrievalAction,
                                                                             authAction: AuthAction): Action[AnyContent] =
    controller(dataRetrievalAction, authAction, navigator).onSubmit(NormalMode)

  private def saveAction(cache: UserAnswersCacheConnector): Action[AnyContent] =
    controller(cache = cache).onSubmit(NormalMode)

  "BankAccountDetails Controller" must {

    behave like controllerWithOnPageLoadMethod(
      onPageLoadAction,
      getMandatorySchemeNameHs,
      validData.dataRetrievalAction,
      form,
      form.fill(bankDetails),
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
      identifiers.BankAccountDetailsId,
      bankDetails
    )
  }
}
