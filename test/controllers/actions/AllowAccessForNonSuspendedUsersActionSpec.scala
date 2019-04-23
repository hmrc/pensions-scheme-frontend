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
import identifiers.MinimalPsaDetailsId
import models.requests.OptionalDataRequest
import models.{requests, _}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContent, Result}
import uk.gov.hmrc.domain.PsaId
import utils.UserAnswers

import scala.concurrent.Future

class AllowAccessForNonSuspendedUsersActionSpec extends SpecBase with MockitoSugar with ScalaFutures {

  private val srn = "123"
  private val psa = "A0000000"

  private def minimalPSA(isSuspended: Boolean) = MinimalPSA(email = "",
    isPsaSuspended = isSuspended,
    organisationName = None,
    individualDetails = None
  )

  private def testData(isSuspended: Boolean) = Json.obj(MinimalPsaDetailsId.toString -> minimalPSA(isSuspended = isSuspended))

  private def optionUserAnswers(isSuspended: Boolean) = Some(UserAnswers(testData(isSuspended)))

  private def authRequest(isSuspended: Boolean): OptionalDataRequest[AnyContent] =
    requests.OptionalDataRequest(fakeRequest, "id", optionUserAnswers(isSuspended = isSuspended), PsaId(psa))

  private def authRequestNoData: OptionalDataRequest[AnyContent] =
    requests.OptionalDataRequest(fakeRequest, "id", None, PsaId(psa))

  class Harness(optionSRN: Option[String]) extends AllowAccessForNonSuspendedUsersAction(
    optionSRN) {
    def callFilter[A](request: OptionalDataRequest[A]): Future[Option[Result]] = filter(request)
  }

  "AllowAccessForNonSuspendedUsersActionSpec" must {
    "respond with None when the minimal details are in the cache and the suspended flag is false" in {
      val action = new Harness(Some(srn))
      whenReady(action.callFilter(authRequest(isSuspended = false)))(_ mustBe None)
    }

    "respond with suspended page when the minimal details are in the cache and the suspended flag is true" in {
      val action = new Harness(Some(srn))
      whenReady(action.callFilter(authRequest(isSuspended = true)))(
        _ mustBe Some(Redirect(controllers.register.routes.CannotMakeChangesController.onPageLoad(srn))))
    }

    "respond with redirect to PSA scheme details controller when there is no lock and the minimal details are NOT in the readonly cache" in {
      val action = new Harness(Some(srn))
      whenReady(action.callFilter(authRequestNoData))(_ mustBe Some(Redirect(controllers.routes.PSASchemeDetailsController.onPageLoad(srn))))
    }

    "respond with redirect to session expired controller when there is no SRN" in {
      val action = new Harness(None)
      whenReady(action.callFilter(authRequestNoData))(_ mustBe Some(Redirect(controllers.routes.SessionExpiredController.onPageLoad())))
    }
  }
}
