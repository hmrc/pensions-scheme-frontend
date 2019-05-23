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

package controllers.actions

import base.SpecBase
import connectors.{PensionsSchemeConnector, SchemeDetailsReadOnlyCacheConnector}
import identifiers.IsPsaSuspendedId
import models.requests.OptionalDataRequest
import org.mockito.Matchers.any
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers._
import uk.gov.hmrc.domain.PsaId
import utils.UserAnswers

import scala.concurrent.Future

class AllowAccessActionSpec extends SpecBase with ScalaFutures with MockitoSugar {

  val pensionsSchemeConnector: PensionsSchemeConnector = {
    val psc = mock[PensionsSchemeConnector]
    when(psc.checkForAssociation(any(), any())(any(), any(), any()))
      .thenReturn(Future.successful(true))
    psc
  }

  val schemeDetailsReadOnlyCacheConnector: SchemeDetailsReadOnlyCacheConnector = {
    val sdrocc = mock[SchemeDetailsReadOnlyCacheConnector]
    when(sdrocc.fetch(any())(any(),any()))
      .thenReturn(Future.successful(None))
    sdrocc
  }

  class TestAllowAccessAction(srn: Option[String],
                              psc: PensionsSchemeConnector = pensionsSchemeConnector,
                              sdrocc: SchemeDetailsReadOnlyCacheConnector = schemeDetailsReadOnlyCacheConnector) extends AllowAccessAction(srn, psc, sdrocc) {
    override def filter[A](request: OptionalDataRequest[A]): Future[Option[Result]] = super.filter(request)
  }

  val srn = Some("S123")

  val suspendedUserAnswers = UserAnswers(Json.obj(IsPsaSuspendedId.toString -> true))
  val notSuspendedUserAnswers = UserAnswers(Json.obj(IsPsaSuspendedId.toString -> false))


  "AllowAccessAction" must {

    "return NOT FOUND for user where no associated between psa id and srn" in {
      val psc: PensionsSchemeConnector = mock[PensionsSchemeConnector]
      when(psc.checkForAssociation(any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(false))

      val action = new TestAllowAccessAction(srn = srn, psc = psc)

      val futureResult = action.filter(OptionalDataRequest(fakeRequest, "id", Some(UserAnswers(Json.obj())), PsaId("A0000000"), false))

      whenReady(futureResult) { result =>
        result.map {
          _.header.status
        } mustBe Some(NOT_FOUND)
      }
    }

    "allow access to pages for user with no data" in {

      val action = new TestAllowAccessAction(None)

      val futureResult = action.filter(OptionalDataRequest(fakeRequest, "id", None, PsaId("A0000000"), false))

      whenReady(futureResult) { result =>

        result.map {
          _.header.status
        } mustBe None
      }

    }

    "allow access to pages for user with no srn in Normal mode" in {

      val action = new TestAllowAccessAction(None)

      val futureResult = action.filter(OptionalDataRequest(fakeRequest, "id", Some(UserAnswers(Json.obj())), PsaId("A0000000"), false))

      whenReady(futureResult) { result =>

        result.map {
          _.header.status
        } mustBe None
      }


    }

    "allow access to pages for users that are not suspended" in {

      val action = new TestAllowAccessAction(srn)

      val futureResult = action.filter(OptionalDataRequest(fakeRequest, "id", Some(notSuspendedUserAnswers), PsaId("A0000000"), false))

      whenReady(futureResult) { result =>

        result.map {
          _.header.status
        } mustBe None
      }


    }

    "redirect to CannotMakeChanges page for suspended users" in {

      val action = new TestAllowAccessAction(srn)

      val futureResult = action.filter(OptionalDataRequest(fakeRequest, "id", Some(suspendedUserAnswers), PsaId("A0000000"), false))

      whenReady(futureResult) { result =>

        result.map {
          _.header.status
        } mustBe Some(SEE_OTHER)
        result.flatMap {
          _.header.headers.get(LOCATION)
        } mustBe Some(controllers.register.routes.CannotMakeChangesController.onPageLoad(srn).url)
      }

    }

  }

}
