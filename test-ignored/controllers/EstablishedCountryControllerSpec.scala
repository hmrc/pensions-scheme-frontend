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
import forms.EstablishedCountryFormProvider
import identifiers.EstablishedCountryId
import models.NormalMode
import navigators.Navigator
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import utils.{CountryOptions, FakeNavigator, InputOption, UserAnswers}
import views.html.establishedCountry

import scala.concurrent.ExecutionContext.Implicits.global

class EstablishedCountryControllerSpec extends ControllerWithQuestionPageBehaviours {

  import EstablishedCountryControllerSpec._

  "EstablishedCountry Controller" must {

    behave like controllerWithOnPageLoadMethod(
      onPageLoadAction(this),
      getMandatorySchemeNameHs,
      validData.dataRetrievalAction,
      form,
      form.fill(testAnswer),
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
      EstablishedCountryId,
      testAnswer
    )
  }
}

object EstablishedCountryControllerSpec {
  val options = Seq(InputOption("territory:AE-AZ", "Abu Dhabi"), InputOption("country:AF", "Afghanistan"))

  val schemeName = "Test Scheme Name"
  val testAnswer = "territory:AE-AZ"

  def countryOptions: CountryOptions = new CountryOptions(options)
  private val formProvider = new EstablishedCountryFormProvider(countryOptions)
  private val form = formProvider.apply()

  private val validData: UserAnswers = UserAnswers().schemeName(schemeName).establishedCountry(testAnswer)

  private val postRequest: FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest().withFormUrlEncodedBody(("value", testAnswer))

  private def viewAsString(base: SpecBase)(form: Form[_] = form): Form[_] => String = form =>
    establishedCountry(base.frontendAppConfig, form, NormalMode, options, schemeName)(base.fakeRequest, base.messages).toString()

  private def controller(base: ControllerSpecBase)(
    dataRetrievalAction: DataRetrievalAction = base.getEmptyData,
    authAction: AuthAction = FakeAuthAction,
    navigator: Navigator = FakeNavigator,
    cache: UserAnswersCacheConnector = FakeUserAnswersCacheConnector
  ): EstablishedCountryController =
    new EstablishedCountryController(
      base.frontendAppConfig,
      base.messagesApi,
      cache,
      navigator,
      authAction,
      dataRetrievalAction,
      new DataRequiredActionImpl(),
      formProvider,
      countryOptions
    )

  private def onPageLoadAction(base: ControllerSpecBase)(dataRetrievalAction: DataRetrievalAction, authAction: AuthAction): Action[AnyContent] =
    controller(base)(dataRetrievalAction, authAction).onPageLoad(NormalMode)

  private def onSubmitAction(base: ControllerSpecBase, navigator: Navigator)(dataRetrievalAction: DataRetrievalAction,
                                                                             authAction: AuthAction): Action[AnyContent] =
    controller(base)(dataRetrievalAction, authAction, navigator).onSubmit(NormalMode)

  private def saveAction(base: ControllerSpecBase)(cache: UserAnswersCacheConnector): Action[AnyContent] =
    controller(base)(cache = cache).onSubmit(NormalMode)
}
