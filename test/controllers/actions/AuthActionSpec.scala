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

package controllers.actions

import base.SpecBase
import controllers.routes
import models.AuthEntity
import play.api.mvc._
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class AuthActionSpec extends SpecBase {

  import AuthActionSpec._

  "Auth Action" when {

    "the user has valid credentials" must {
      "return OK" in {
        val authAction = new AuthActionImpl(fakeAuthConnector(authRetrievals), frontendAppConfig, parser)
        val controller = new Harness(authAction, authEntity = None)

        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe OK
      }
    }

    "the user hasn't enrolled in PODS" must {
      "redirect the user to pension administrator frontend" in {
        val authAction = new AuthActionImpl(fakeAuthConnector(emptyAuthRetrievals), frontendAppConfig, parser)
        val controller = new Harness(authAction, authEntity = None)

        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.YouNeedToRegisterController.onPageLoad().url)
      }
    }

    "erroneous retrievals are obtained" must {
      "redirect the user to unauthorised controller" in {
        val authAction = new AuthActionImpl(fakeAuthConnector(erroneousRetrievals), frontendAppConfig, parser)
        val controller = new Harness(authAction, authEntity = None)

        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.UnauthorisedController.onPageLoad().url)
      }
    }

    "the user hasn't logged in" must {
      "redirect the user to log in " in {
        val authAction = new AuthActionImpl(fakeAuthConnector(Future.failed(new MissingBearerToken)), frontendAppConfig, parser)
        val controller = new Harness(authAction, authEntity = None)
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result).get must startWith(frontendAppConfig.loginUrl)
      }
    }

    "the user's session has expired" must {
      "redirect the user to log in " in {
        val authAction = new AuthActionImpl(fakeAuthConnector(Future.failed(new BearerTokenExpired)), frontendAppConfig, parser)
        val controller = new Harness(authAction, authEntity = None)
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result).get must startWith(frontendAppConfig.loginUrl)
      }
    }

    "the user doesn't have sufficient enrolments" must {
      "redirect the user to the unauthorised page" in {
        val authAction = new AuthActionImpl(fakeAuthConnector(Future.failed(new InsufficientEnrolments)), frontendAppConfig, parser)
        val controller = new Harness(authAction, authEntity = None)
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
      }
    }

    "the user doesn't have sufficient confidence level" must {
      "redirect the user to the unauthorised page" in {
        val authAction = new AuthActionImpl(fakeAuthConnector(Future.failed(new InsufficientConfidenceLevel)), frontendAppConfig, parser)
        val controller = new Harness(authAction, authEntity = None)
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
      }
    }

    "the user used an unaccepted auth provider" must {
      "redirect the user to the unauthorised page" in {
        val authAction = new AuthActionImpl(fakeAuthConnector(Future.failed(new UnsupportedAuthProvider)), frontendAppConfig, parser)
        val controller = new Harness(authAction, authEntity = None)
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
      }
    }

    "the user has an unsupported affinity group" must {
      "redirect the user to the unauthorised page" in {
        val authAction = new AuthActionImpl(fakeAuthConnector(Future.failed(new UnsupportedAffinityGroup)), frontendAppConfig, parser)
        val controller = new Harness(authAction, authEntity = None)
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
      }
    }

    "the user has an unsupported credential role" must {
      "redirect the user to the unauthorised page" in {
        val authAction = new AuthActionImpl(fakeAuthConnector(Future.failed(new UnsupportedCredentialRole)), frontendAppConfig, parser)
        val controller = new Harness(authAction, authEntity = None)
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
      }
    }
  }
}

object AuthActionSpec extends SpecBase {

  private def fakeAuthConnector(stubbedRetrievalResult: Future[_]) = new AuthConnector {

    def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] = {
      stubbedRetrievalResult.map(_.asInstanceOf[A])(ec)
    }
  }

  private def authRetrievals = Future.successful(new ~(Some("id"), Enrolments(Set(
    Enrolment("HMRC-PODS-ORG", Seq(EnrolmentIdentifier("PSAID", "A2100000")), "", None)
  ))))
  private def emptyAuthRetrievals = Future.successful(new ~(Some("id"), Enrolments(Set())))
  private def erroneousRetrievals = Future.successful(new ~(None, Enrolments(Set())))

  class Harness(authAction: AuthAction,
                val controllerComponents: MessagesControllerComponents = controllerComponents,
                authEntity: Option[AuthEntity] = None) extends BaseController {
    def onPageLoad(): Action[AnyContent] = authAction.apply(authEntity = authEntity) { _ => Ok }
  }

  private val parser = app.injector.instanceOf[BodyParsers.Default]

}
