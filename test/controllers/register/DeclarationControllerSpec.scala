/*
 * Copyright 2018 HM Revenue & Customs
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

package controllers.register

import config.FrontendAppConfig
import connectors.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.DeclarationFormProvider
import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.establishers.individual.EstablisherDetailsId
import identifiers.register.{DeclarationDormantId, DeclarationDutiesId, SchemeDetailsId}
import models.CompanyDetails
import models.person.PersonDetails
import models.register.{DeclarationDormant, SchemeDetails, SchemeType}
import org.joda.time.LocalDate
import play.api.data.Form
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Call
import play.api.test.Helpers._
import utils.{FakeNavigator, UserAnswers}
import views.html.register.declaration

class DeclarationControllerSpec extends ControllerSpecBase {

  import DeclarationControllerSpec._

  "Declaration Controller with hub and spoke switched off" must {

    "return OK and the correct view" when {
      "individual journey" in {
        val result = controller(individual).onPageLoad()(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(isCompany = false, isDormant = false)
      }
      "non-dormant company establisher" in {
        val result = controller(nonDormantCompany).onPageLoad()(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(isCompany = true, isDormant = false)
      }
      "dormant company establisher" in {
        val result = controller(dormantCompany).onPageLoad()(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(isCompany = true, isDormant = true)
      }
      "master trust" in {

        val data = new FakeDataRetrievalAction(Some(UserAnswers()
          .set(SchemeDetailsId)(SchemeDetails("Test Scheme Name", SchemeType.MasterTrust))
          .asOpt
          .value
          .json
        ))

        val result = controller(data).onPageLoad()(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(isCompany = false, isDormant = false, showMasterTrustDeclaration = true)
      }
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody("agree" -> "agreed")

      val result = controller(nonDormantCompany).onSubmit()(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors" when {
      "invalid data is submitted in individual journey" in {
        val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
        val boundForm = form.bind(Map("value" -> "invalid value"))

        val result = controller(individual).onSubmit()(postRequest)

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe viewAsString(boundForm, isCompany = false, isDormant = false)
      }
      "invalid data is submitted in company journey" in {
        val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
        val boundForm = form.bind(Map("value" -> "invalid value"))

        val result = controller(nonDormantCompany).onSubmit()(postRequest)

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe viewAsString(boundForm, isCompany = true, isDormant = false)
      }
    }

    "redirect to Session Expired" when {
      "no existing data is found" when {
        "GET" in {
          val result = controller(dontGetAnyData).onPageLoad()(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
        "POST" in {
          val postRequest = fakeRequest.withFormUrlEncodedBody(("value", ""))
          val result = controller(dontGetAnyData).onSubmit()(postRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
      }
    }

  }

}

object DeclarationControllerSpec extends ControllerSpecBase {

  override def frontendAppConfig: FrontendAppConfig = new GuiceApplicationBuilder().configure(
    Map(
      "features.allowMasterTrust" -> true,
      "features.is-hub-enabled" -> false
    )
  ).build().injector.instanceOf[FrontendAppConfig]

  private def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  private val formProvider = new DeclarationFormProvider()
  private val form = formProvider()
  private val schemeName = "Test Scheme Name"

  private def controller(dataRetrievalAction: DataRetrievalAction): DeclarationController =
    new DeclarationController(
      frontendAppConfig,
      messagesApi,
      FakeUserAnswersCacheConnector,
      new FakeNavigator(onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider
    )

  private def viewAsString(form: Form[_] = form, isCompany: Boolean, isDormant: Boolean,
                           showMasterTrustDeclaration: Boolean = false, hasWorkingKnowledge: Boolean = false): String =
    declaration(
      frontendAppConfig,
      form,
      schemeName,
      isCompany,
      isDormant,
      showMasterTrustDeclaration,
      hasWorkingKnowledge
    )(fakeRequest, messages).toString

  private val individual =
    UserAnswers()
      .schemeDetails()
      .individualEstablisher()
      .asDataRetrievalAction()

  private val nonDormantCompany =
    UserAnswers()
      .schemeDetails()
      .companyEstablisher()
      .dormant(false)
      .asDataRetrievalAction()

  private val dormantCompany =
    UserAnswers()
      .schemeDetails()
      .companyEstablisher()
      .dormant(true)
      .asDataRetrievalAction()

  private implicit class UserAnswersOps(answers: UserAnswers) {

    def schemeDetails(): UserAnswers = {
      answers.set(SchemeDetailsId)(SchemeDetails("Test Scheme Name", SchemeType.SingleTrust)).asOpt.value
    }

    def companyEstablisher(): UserAnswers = {
      answers.set(CompanyDetailsId(0))(CompanyDetails("test-company-name", None, None)).asOpt.value
    }

    def individualEstablisher(): UserAnswers = {
      answers.set(EstablisherDetailsId(0))(PersonDetails("test-first-name", None, "test-last-name", LocalDate.now())).asOpt.value
    }

    def dormant(dormant: Boolean): UserAnswers = {
      val declarationDormant = if (dormant) DeclarationDormant.Yes else DeclarationDormant.No
      answers.set(DeclarationDormantId)(declarationDormant).asOpt.value
    }

    def asDataRetrievalAction(): DataRetrievalAction = {
      new FakeDataRetrievalAction(Some(answers.json))
    }
  }

}

class DeclarationHsControllerSpec extends ControllerSpecBase {
  override def frontendAppConfig: FrontendAppConfig = new GuiceApplicationBuilder().configure(
    Map(
      "features.allowMasterTrust" -> true,
      "features.is-hub-enabled" -> true
    )
  ).build().injector.instanceOf[FrontendAppConfig]

  private implicit class UserAnswersOps(answers: UserAnswers) {
    def schemeDetails(): UserAnswers = {
      answers.set(SchemeDetailsId)(SchemeDetails("Test Scheme Name", SchemeType.SingleTrust)).asOpt.value
    }

    def individualEstablisher(): UserAnswers = {
      answers.set(EstablisherDetailsId(0))(PersonDetails("test-first-name", None, "test-last-name", LocalDate.now())).asOpt.value
    }

    def asDataRetrievalAction(): DataRetrievalAction = {
      new FakeDataRetrievalAction(Some(answers.json))
    }
  }

  private val schemeName = "Test Scheme Name"
  private val formProvider = new DeclarationFormProvider()
  private val individual =
    UserAnswers()
      .schemeDetails()
      .individualEstablisher()
      .set(DeclarationDutiesId)(false)
      .asOpt
      .value
      .asDataRetrievalAction()

  private def viewAsString(form: Form[_] = formProvider(), isCompany: Boolean, isDormant: Boolean,
                           showMasterTrustDeclaration: Boolean = false, hasWorkingKnowledge: Boolean = false): String =
    declaration(
      frontendAppConfig,
      form,
      schemeName,
      isCompany,
      isDormant,
      showMasterTrustDeclaration,
      hasWorkingKnowledge
    )(fakeRequest, messages).toString

  private def controller(dataRetrievalAction: DataRetrievalAction): DeclarationController =
    new DeclarationController(
      frontendAppConfig,
      messagesApi,
      FakeUserAnswersCacheConnector,
      new FakeNavigator(controllers.routes.IndexController.onPageLoad()),
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider
    )

  "Declaration Controller with hub and spoke switched on" must {
    "return OK and the correct view" when {
      "individual journey" in {
        val result = controller(individual).onPageLoad()(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(isCompany = false, isDormant = false)
      }
    }
  }
}
