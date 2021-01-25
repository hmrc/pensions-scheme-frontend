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
import config.FrontendAppConfig
import connectors.PensionsSchemeConnector
import identifiers.PsaMinimalFlagsId
import models.PSAMinimalFlags._
import models.requests.OptionalDataRequest
import models.{PSAMinimalFlags, UpdateMode}
import org.mockito.Matchers.any
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import play.api.mvc.{Request, Result}
import play.api.test.Helpers._
import play.twirl.api.Html
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.play.bootstrap.http.FrontendErrorHandler
import utils.UserAnswers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AllowAccessActionSpec extends SpecBase with ScalaFutures with MockitoSugar {
  import AllowAccessActionSpec._

  "AllowAccessActionMain" must {
    behave like allowAccessAction(testHarness = generateTestHarnessForAllowAccessMain)

    behave like allowAccessWithRedirectBasedOnUserAnswers(
      testHarness = generateTestHarnessForAllowAccessMain,
      userAnswers = suspendedUserAnswers,
      description = "PSA is suspended",
      expectedResult = Some(controllers.register.routes.CannotMakeChangesController.onPageLoad(srn).url)
    )

    behave like allowAccessWithRedirectBasedOnUserAnswers(generateTestHarnessForAllowAccessMain, deceasedUserAnswers,
      "PSA is deceased",
      Some(frontendAppConfig.youMustContactHMRCUrl)
    )

    "redirect to task list page where association between psa id and srn and no user answers present but an srn IS present" in {
      val psc: PensionsSchemeConnector = mock[PensionsSchemeConnector]
      when(psc.checkForAssociation(any(), any())(any(),any(),any()))
        .thenReturn(Future.successful(Right(true)))

      val futureResult = generateTestHarnessForAllowAccessMain(srn, pensionsSchemeConnector)
        .test(OptionalDataRequest(fakeRequest, "id", None, Some(PsaId("A0000000"))))

      assertEqual(futureResult, Some(controllers.routes.SchemeTaskListController.onPageLoad(UpdateMode, srn).url))
    }
  }

  "AllowAccessActionTaskList" must {
    behave like allowAccessAction(testHarness = generateTestHarnessForAllowAccessTaskList)

    behave like allowAccessWithRedirectBasedOnUserAnswers(
      testHarness = generateTestHarnessForAllowAccessTaskList,
      userAnswers = suspendedUserAnswers,
      description = "PSA is suspended",
      expectedResult = None
    )

    behave like allowAccessWithRedirectBasedOnUserAnswers(generateTestHarnessForAllowAccessTaskList, deceasedUserAnswers, "PSA is deceased",
      Some(frontendAppConfig.youMustContactHMRCUrl))

    "allow access where association between psa id and srn and no user answers present but an srn IS present" in {
      val psc: PensionsSchemeConnector = mock[PensionsSchemeConnector]
      when(psc.checkForAssociation(any(), any())(any(),any(),any()))
        .thenReturn(Future.successful(Right(true)))

      val futureResult = generateTestHarnessForAllowAccessTaskList(srn, pensionsSchemeConnector)
        .test(OptionalDataRequest(fakeRequest, "id", None, Some(PsaId("A0000000"))))

      assertEqual(futureResult, None)
    }
  }

  "AllowAccessActionNoSuspendedCheck" must {
    behave like allowAccessAction(testHarness = generateTestHarnessForAllowAccessNoSuspendedCheck)

    behave like allowAccessWithRedirectBasedOnUserAnswers(
      testHarness = generateTestHarnessForAllowAccessNoSuspendedCheck,
      userAnswers = suspendedUserAnswers,
      description = "PSA is suspended",
      expectedResult = None
    )

    "redirect to task list page where association between psa id and srn and no user answers present but an srn IS present" in {
      val psc: PensionsSchemeConnector = mock[PensionsSchemeConnector]
      when(psc.checkForAssociation(any(), any())(any(),any(),any()))
        .thenReturn(Future.successful(Right(true)))

      val futureResult = generateTestHarnessForAllowAccessNoSuspendedCheck(srn, pensionsSchemeConnector)
        .test(OptionalDataRequest(fakeRequest, "id", None, Some(PsaId("A0000000"))))

      assertEqual(futureResult, Some(controllers.routes.SchemeTaskListController.onPageLoad(UpdateMode, srn).url))
    }
  }

  //scalastyle:off method.length
  def allowAccessAction(testHarness: (Option[String], PensionsSchemeConnector) => TestHarness): Unit = {
    "allow access where association between psa id and srn and user answers present and an srn IS present and viewonly mode" in {
      val psc: PensionsSchemeConnector = mock[PensionsSchemeConnector]
      when(psc.checkForAssociation(any(), any())(any(),any(),any()))
        .thenReturn(Future.successful(Right(true)))

      val futureResult = testHarness(srn, psc)
        .test(OptionalDataRequest(fakeRequest, "id", Some(UserAnswers(Json.obj())), Some(PsaId("A0000000")), viewOnly = true))

      assertEqual(futureResult, None)
    }

    "return NOT FOUND for user where NO association between psa id and both srn and user answers present" in {
      val psc: PensionsSchemeConnector = mock[PensionsSchemeConnector]
      when(psc.checkForAssociation(any(), any())(any(),any(),any()))
        .thenReturn(Future.successful(Right(false)))

      val futureResult = testHarness(srn, psc)
        .test(OptionalDataRequest(fakeRequest, "id", Some(UserAnswers(Json.obj())), Some(PsaId("A0000000"))))

      futureResult.foreach { result =>
        result.map {
          _.header.status
        } mustBe Some(NOT_FOUND)
      }
    }

    "allow access for user where association between psa id and both srn and user answers present" in {
      val psc: PensionsSchemeConnector = mock[PensionsSchemeConnector]
      when(psc.checkForAssociation(any(), any())(any(),any(),any()))
        .thenReturn(Future.successful(Right(true)))

      val futureResult = testHarness(srn, psc)
        .test(OptionalDataRequest(fakeRequest, "id", Some(UserAnswers(Json.obj())), Some(PsaId("A0000000"))))

      assertEqual(futureResult, None)
    }

    "allow access for user with no data" in {
      val futureResult = testHarness(None, pensionsSchemeConnector)
        .test(OptionalDataRequest(fakeRequest, "id", None, Some(PsaId("A0000000"))))

      assertEqual(futureResult, None)
    }

    "allow access to pages for user with no srn in Normal mode" in {
      val futureResult = testHarness(None, pensionsSchemeConnector)
        .test(OptionalDataRequest(fakeRequest, "id", Some(UserAnswers(Json.obj())), Some(PsaId("A0000000"))))

      assertEqual(futureResult, None)
    }

    "allow access to pages for users that are not suspended" in {
      val futureResult = testHarness(srn, pensionsSchemeConnector)
        .test(OptionalDataRequest(fakeRequest, "id", Some(notSuspendedUserAnswers), Some(PsaId("A0000000"))))

      assertEqual(futureResult, None)
    }
  }

  def allowAccessWithRedirectBasedOnUserAnswers(testHarness: (Option[String], PensionsSchemeConnector) => TestHarness,
                                                userAnswers: UserAnswers, description:String, expectedResult: => Option[String]):Unit = {
    s"respond correctly where association between psa id and srn and user answers present and an srn IS present and viewonly mode and $description" in {
      val psc: PensionsSchemeConnector = mock[PensionsSchemeConnector]
      when(psc.checkForAssociation(any(), any())(any(),any(),any()))
        .thenReturn(Future.successful(Right(true)))

      val futureResult = testHarness(srn, psc)
        .test(OptionalDataRequest(fakeRequest, "id", Some(userAnswers), Some(PsaId("A0000000")), viewOnly = true))

      assertEqual(futureResult, expectedResult)
    }

    s"respond correctly where association between psa id and srn and user answers present and an srn IS present and not viewonly mode and $description" in {
      val psc: PensionsSchemeConnector = mock[PensionsSchemeConnector]
      when(psc.checkForAssociation(any(), any())(any(),any(),any()))
        .thenReturn(Future.successful(Right(true)))

      val futureResult = testHarness(srn, psc)
        .test(OptionalDataRequest(fakeRequest, "id", Some(userAnswers), Some(PsaId("A0000000"))))

      assertEqual(futureResult, expectedResult)
    }
  }
}

object AllowAccessActionSpec extends SpecBase with ScalaFutures with MockitoSugar {
  private val config = injector.instanceOf[FrontendAppConfig]

  private val errorHandler = new FrontendErrorHandler {
    override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit request: Request[_]): Html = Html("")

    override def messagesApi: MessagesApi = injector.instanceOf[MessagesApi]
  }

  private val pensionsSchemeConnector: PensionsSchemeConnector = {
    val psc = mock[PensionsSchemeConnector]
    when(psc.checkForAssociation(any(), any())(any(),any(),any()))
      .thenReturn(Future.successful(Right(true)))
    psc
  }

  trait TestHarness {
    def test[A](request: OptionalDataRequest[A]): Future[Option[Result]]
  }

  class TestAllowAccessAction(srn: Option[String],
                              psc: PensionsSchemeConnector = pensionsSchemeConnector) extends AllowAccessActionMain(srn, psc, config, errorHandler) with TestHarness {
    def test[A](request: OptionalDataRequest[A]): Future[Option[Result]] = super.filter(request)

  }

  class TestAllowAccessActionTaskList(srn: Option[String], psc: PensionsSchemeConnector = pensionsSchemeConnector) extends
    AllowAccessActionTaskList(srn, psc, config, errorHandler)
    with TestHarness {
    def test[A](request: OptionalDataRequest[A]): Future[Option[Result]] = super.filter(request)
  }

  class TestAllowAccessActionNoSuspendedCheck(srn: Option[String], psc: PensionsSchemeConnector = pensionsSchemeConnector) extends
    AllowAccessActionNoSuspendedCheck(srn, psc, config, errorHandler) with TestHarness {
    def test[A](request: OptionalDataRequest[A]): Future[Option[Result]] = super.filter(request)
  }

  private val generateTestHarnessForAllowAccessMain: (Option[String], PensionsSchemeConnector) => TestHarness = new TestAllowAccessAction(_, _)
  private val generateTestHarnessForAllowAccessTaskList: (Option[String], PensionsSchemeConnector) => TestHarness = new TestAllowAccessActionTaskList(_, _)
  private val generateTestHarnessForAllowAccessNoSuspendedCheck: (Option[String], PensionsSchemeConnector) => TestHarness =
    new TestAllowAccessActionNoSuspendedCheck(_, _)

  private val srn = Some("S123")

  private val suspendedUserAnswers = UserAnswers(
    Json.obj(
      PsaMinimalFlagsId.toString -> Json.toJson(PSAMinimalFlags(isSuspended = true, isDeceased = false))
    )
  )

  private val deceasedUserAnswers = UserAnswers(
    Json.obj(
      PsaMinimalFlagsId.toString -> Json.toJson(PSAMinimalFlags(isSuspended = false, isDeceased = true))
    )
  )

  private val notSuspendedUserAnswers = UserAnswers(Json.obj(PsaMinimalFlagsId.toString -> PSAMinimalFlags(isSuspended = false, isDeceased = false)))

  private def assertEqual(futureResult: Future[Option[Result]], expectedResult: => Option[String]): Unit = {
    whenReady(futureResult) {
      case result@Some(_) =>
        result.map {
          _.header.status
        } mustBe Some(SEE_OTHER)
        result.flatMap {
          _.header.headers.get(LOCATION)
        } mustBe expectedResult
      case result => result mustBe expectedResult

    }
  }

}
