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
import connectors._
import identifiers.MinimalPsaDetailsId
import models._
import models.requests.AuthenticatedRequest
import org.mockito.Matchers
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, Result}
import uk.gov.hmrc.domain.PsaId

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AllowAccessForNonSuspendedUsersActionSpec extends SpecBase with MockitoSugar with ScalaFutures {

  private val srn = "123"
  private val srnOpt = Some(srn)
  private val psa = "A0000000"
  private val schemeVariance = SchemeVariance(psa, srn)
  private val authRequest: AuthenticatedRequest[AnyContent] = AuthenticatedRequest(fakeRequest, "id", PsaId(psa))

  private val dataCacheConnector = mock[UserAnswersCacheConnector]
  private val viewCacheConnector = mock[SchemeDetailsReadOnlyCacheConnector]
  private val updateCacheConnector = mock[UpdateSchemeCacheConnector]
  private val lockRepoConnector = mock[PensionSchemeVarianceLockConnector]

  private val minimalPSA = MinimalPSA(email = "",
    isPsaSuspended = false,
    organisationName = None,
    individualDetails = None
  )

  private val testData = Json.obj(MinimalPsaDetailsId.toString -> minimalPSA)


  class Harness(lockConnector: PensionSchemeVarianceLockConnector,
                schemeDetailsReadOnlyCacheConnector: UserAnswersCacheConnector,
                updateConnector: UpdateSchemeCacheConnector, optionSRN: Option[String]) extends AllowAccessForNonSuspendedUsersAction(
    lockConnector,
    schemeDetailsReadOnlyCacheConnector,
    updateConnector,
    optionSRN) {
    def callFilter[A](request: AuthenticatedRequest[A]):Future[Option[Result]] = filter(request)
  }

  "AllowAccessForNonSuspendedUsersActionSpec" when {
    "there is no lock and the minimal details are in the readonly cache and the suspended flag is false" must {
      "respond with None" in {
        when(lockRepoConnector.isLockByPsaIdOrSchemeId(Matchers.any(), Matchers.any())(any(), any())).thenReturn(Future(None))
        when(viewCacheConnector.fetch(Matchers.any())(any(), any())) thenReturn Future.successful(Some(testData))

        val action = new Harness(lockRepoConnector, viewCacheConnector, updateCacheConnector, Some(srn))

        val futureResult = action.callFilter(authRequest)
        whenReady(futureResult) { result =>
          result mustBe None
        }



      }
    }

  }
}
