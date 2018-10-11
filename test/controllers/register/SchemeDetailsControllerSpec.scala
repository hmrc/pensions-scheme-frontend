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
import connectors.{FakeUserAnswersCacheConnector, PensionAdministratorConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.SchemeDetailsFormProvider
import identifiers.register.SchemeDetailsId
import models.register.{SchemeDetails, SchemeType}
import models.requests.OptionalDataRequest
import models.{NormalMode, PSAName}
import play.api.data.Form
import play.api.libs.json.{Json, Reads}
import play.api.mvc.AnyContent
import play.api.test.Helpers._
import uk.gov.hmrc.crypto.ApplicationCrypto
import uk.gov.hmrc.http.HeaderCarrier
import utils.{FakeNavigator, NameMatching, NameMatchingFactory}
import views.html.register.schemeDetails

import scala.concurrent.{ExecutionContext, Future}

class SchemeDetailsControllerSpec extends ControllerSpecBase {

  private def onwardRoute = controllers.routes.IndexController.onPageLoad()

  val formProvider = new SchemeDetailsFormProvider()
  val form = formProvider()

  val config: FrontendAppConfig = injector.instanceOf[FrontendAppConfig]
  val pensionAdministratorConnector: PensionAdministratorConnector = injector.instanceOf[PensionAdministratorConnector]

  object FakeNameMatchingFactory extends NameMatchingFactory(FakeUserAnswersCacheConnector, pensionAdministratorConnector, ApplicationCrypto, config) {
    override def nameMatching(schemeName: String)
                             (implicit request: OptionalDataRequest[AnyContent],
                              ec: ExecutionContext,
                              hc: HeaderCarrier, r: Reads[PSAName]): Future[NameMatching] = {
      Future.successful(NameMatching("value 1", "My PSA"))
    }
  }

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData): SchemeDetailsController =
    new SchemeDetailsController(
      frontendAppConfig,
      messagesApi,
      FakeUserAnswersCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      formProvider,
      FakeNameMatchingFactory
    )

  private def viewAsString(form: Form[_] = form) = schemeDetails(frontendAppConfig, form, NormalMode)(fakeRequest, messages).toString

  "SchemeDetails Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val validData = Json.obj(SchemeDetailsId.toString -> Json.toJson(SchemeDetails("value 1", SchemeType.SingleTrust)))
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))

      val result = controller(getRelevantData).onPageLoad(NormalMode)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(SchemeDetails("value 1", SchemeType.SingleTrust)))
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("schemeName", "value 1"), ("schemeType.type", "single"))

      val result = controller().onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors" when {
      "invalid data is submitted" in {
        val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
        val boundForm = form.bind(Map("value" -> "invalid value"))

        val result = controller().onSubmit(NormalMode)(postRequest)

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe viewAsString(boundForm)
      }

      "scheme name matches psa name" in {
        val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "My PSA"))
        val boundForm = form.bind(Map("value" -> "My PSA"))

        val result = controller().onSubmit(NormalMode)(postRequest)

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe viewAsString(boundForm)
      }
    }

  }
}
