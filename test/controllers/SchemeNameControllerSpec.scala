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

import config.FrontendAppConfig
import connectors.{FakeUserAnswersCacheConnector, PensionAdministratorConnector}
import controllers.actions._
import forms.register.SchemeNameFormProvider
import identifiers.SchemeNameId
import models.NormalMode
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier

import utils.{FakeNavigator, NameMatching, NameMatchingFactory}
import views.html.schemeName

import scala.concurrent.{ExecutionContext, Future}

class SchemeNameControllerSpec extends ControllerSpecBase with MockitoSugar {
  private def onwardRoute = controllers.routes.IndexController.onPageLoad()
  private val scheme = "A scheme"
  private val psaName = "Mr Maxwell"
  val formProvider = new SchemeNameFormProvider()
  val form: Form[String] = formProvider()

  val config: FrontendAppConfig = injector.instanceOf[FrontendAppConfig]
  val pensionAdministratorConnector: PensionAdministratorConnector = injector.instanceOf[PensionAdministratorConnector]
  val mockPensionAdministratorConnector: PensionAdministratorConnector = mock[PensionAdministratorConnector]

  object FakeNameMatchingFactory extends NameMatchingFactory(pensionAdministratorConnector) {
    override def nameMatching(schemeName: String)
                             (implicit ec: ExecutionContext,
                              hc: HeaderCarrier): Future[NameMatching] = {
      Future.successful(NameMatching("value 1", "My PSA"))
    }
  }

  object FakeNameMatchingFactoryWithMatch extends NameMatchingFactory(pensionAdministratorConnector) {
    override def nameMatching(schemeName: String)
                             (implicit ec: ExecutionContext,
                              hc: HeaderCarrier): Future[NameMatching] = {
      Future.successful(NameMatching("My PSA", "My PSA"))
    }
  }

  private val view = injector.instanceOf[schemeName]

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData, nameMatchingFactory:NameMatchingFactory = FakeNameMatchingFactory): SchemeNameController =
    new SchemeNameController(
      messagesApi,
      FakeUserAnswersCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      formProvider,
      nameMatchingFactory,
      mockPensionAdministratorConnector,
      controllerComponents,
      view
    )

  private def viewAsString(form: Form[_] = form) = view(form, NormalMode, scheme)(fakeRequest, messages).toString

  "SchemeName Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val validData = Json.obj(SchemeNameId.toString -> "value 1")
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))

      val result = controller(getRelevantData).onPageLoad(NormalMode)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill("value 1"))
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("schemeName", "value 1"))

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
        val postRequest = fakeRequest.withFormUrlEncodedBody(("schemeName", "My PSA"))
        val boundForm = form
          .withError(
            "schemeName",
            "messages__error__scheme_name_psa_name_match", psaName
          )

        when(mockPensionAdministratorConnector.getPSAName(any(), any())).thenReturn(Future.successful(psaName))

        val result = controller(nameMatchingFactory = FakeNameMatchingFactoryWithMatch).onSubmit(NormalMode)(postRequest)

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe viewAsString(boundForm)
      }
    }

  }
}
