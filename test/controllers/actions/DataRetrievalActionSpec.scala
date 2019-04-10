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
import models.requests.{AuthenticatedRequest, OptionalDataRequest}
import models._
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import uk.gov.hmrc.domain.PsaId
import utils.UserAnswers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DataRetrievalActionSpec extends SpecBase with MockitoSugar with ScalaFutures {

  private val srn = "123"
  private val srnOpt = Some(srn)
  private val psa = "A0000000"
  private val schemeVariance = SchemeVariance(psa, srn)
  private val authRequest: AuthenticatedRequest[AnyContent] = AuthenticatedRequest(fakeRequest, "id", PsaId(psa))

  private val dataCacheConnector = mock[UserAnswersCacheConnector]
  private val viewCacheConnector = mock[SchemeDetailsReadOnlyCacheConnector]
  private val updateCacheConnector = mock[UpdateSchemeCacheConnector]
  private val lockRepoConnector = mock[PensionSchemeVarianceLockConnector]

  private val testData = Json.obj("test" -> "data")

  class Harness(dataConnector: UserAnswersCacheConnector = dataCacheConnector,
                viewConnector: SchemeDetailsReadOnlyCacheConnector = viewCacheConnector,
                updateConnector: UpdateSchemeCacheConnector = updateCacheConnector,
                lockConnector: PensionSchemeVarianceLockConnector = lockRepoConnector,
                mode: Mode = NormalMode,
                srn: Option[String] = None) extends
    DataRetrievalImpl(dataConnector, viewConnector, updateConnector, lockConnector, mode, srn) {
    def callTransform[A](request: AuthenticatedRequest[A]): Future[OptionalDataRequest[A]] = transform(request)
  }

  "Data Retrieval Action" when {
    "there is no data in the cache in NormalMode" must {
      "set userAnswers to 'None' in the request" in {
        when(dataCacheConnector.fetch(eqTo("id"))(any(), any())) thenReturn Future(None)
        val action = new Harness(dataCacheConnector)

        val futureResult = action.callTransform(authRequest)

        whenReady(futureResult) { result =>
          result.userAnswers.isEmpty mustBe true
        }
      }
    }

    "there is data in the cache in NormalMode" must {
      "build a userAnswers object and add it to the request" in {
        when(dataCacheConnector.fetch(eqTo("id"))(any(), any())) thenReturn Future.successful(Some(testData))
        val action = new Harness(dataCacheConnector)

        val futureResult = action.callTransform(authRequest)

        whenReady(futureResult) { result =>
          result.userAnswers.isDefined mustBe true
        }
      }
    }

    "there is no data in the update cache in UpdateMode and lock is held by psa" must {
      "set userAnswers to 'None' in the request" in {
        when(lockRepoConnector.isLockByPsaIdOrSchemeId(eqTo(psa), eqTo(srn))(any(), any())).thenReturn(Future(Some(VarianceLock)))
        when(updateCacheConnector.fetch(eqTo(srn))(any(), any())) thenReturn Future(None)
        val action = new Harness(updateConnector = updateCacheConnector, lockConnector = lockRepoConnector, mode = UpdateMode, srn = srnOpt)

        val futureResult = action.callTransform(authRequest)

        whenReady(futureResult) { result =>
          result.userAnswers.isEmpty mustBe true
        }
      }
    }

    "there is data in the update cache in UpdateMode and lock is held by psa" must {
      "build a userAnswers object and add it to the request" in {

        when(lockRepoConnector.isLockByPsaIdOrSchemeId(eqTo(psa), eqTo(srn))(any(), any())).thenReturn(Future(Some(VarianceLock)))
        when(updateCacheConnector.fetch(eqTo(srn))(any(), any())) thenReturn Future.successful(Some(Json.obj()))
        val action = new Harness(updateConnector = updateCacheConnector, lockConnector = lockRepoConnector, mode = UpdateMode, srn = srnOpt)

        val futureResult = action.callTransform(authRequest)

        whenReady(futureResult) { result =>
          result.userAnswers.isDefined mustBe true
        }
      }
    }

    "there is no data in the read-only cache in UpdateMode and lock is not held by psa" must {
      "set userAnswers to 'None' in the request" in {
        when(lockRepoConnector.isLockByPsaIdOrSchemeId(eqTo(psa), eqTo(srn))(any(), any())).thenReturn(Future(None))
        when(viewCacheConnector.fetch(eqTo("id"))(any(), any())) thenReturn Future(None)
        val action = new Harness(viewConnector = viewCacheConnector, lockConnector = lockRepoConnector, mode = UpdateMode, srn = srnOpt)

        val futureResult = action.callTransform(authRequest)

        whenReady(futureResult) { result =>
          result.userAnswers.isEmpty mustBe true
        }
      }
    }

    "there is data in the read-only cache in UpdateMode and lock is not held by anyone" must {
      "build a userAnswers object and add it to the request, acquire lock, save data to updateCache" in {
        when(lockRepoConnector.lock(eqTo(psa), eqTo(srn))(any(), any())) thenReturn Future(VarianceLock)
        when(viewCacheConnector.fetch(eqTo("id"))(any(), any())) thenReturn Future.successful(Some(testData))
        when(updateCacheConnector.upsert(eqTo(srn), eqTo(UserAnswers(testData).json))(any(), any())) thenReturn Future.successful(testData)

        val action = new Harness(
          viewConnector = viewCacheConnector,
          updateConnector = updateCacheConnector,
          lockConnector = lockRepoConnector,
          mode = UpdateMode,
          srn = srnOpt)

        val futureResult = action.callTransform(authRequest)

        whenReady(futureResult) { result =>
          result.userAnswers.isDefined mustBe true
          result.userAnswers.get mustBe UserAnswers(testData)
        }
      }
    }

    "there is data in the read-only cache in UpdateMode and lock is held by someone else" must {
      "fetch data from viewConnector to build a userAnswers object and add it to the request" in {
        when(lockRepoConnector.lock(eqTo(psa), eqTo(srn))(any(), any())) thenReturn Future(SchemeLock)
        when(viewCacheConnector.fetch(eqTo("id"))(any(), any())) thenReturn Future.successful(Some(testData))

        val action = new Harness(
          viewConnector = viewCacheConnector,
          updateConnector = updateCacheConnector,
          lockConnector = lockRepoConnector,
          mode = UpdateMode,
          srn = srnOpt)

        val futureResult = action.callTransform(authRequest)

        whenReady(futureResult) { result =>
          result.userAnswers.isDefined mustBe true
          result.userAnswers.get mustBe UserAnswers(testData)
        }
      }
    }

    "there is data in the read-only cache in UpdateMode and lock is not held by anyone but disableLock flag is true" must {
      "fetch data from viewConnector to build a userAnswers object and add it to the request" in {
        when(lockRepoConnector.lock(eqTo(psa), eqTo(srn))(any(), any())) thenReturn Future(SchemeLock)
        when(viewCacheConnector.fetch(eqTo("id"))(any(), any())) thenReturn Future.successful(Some(testData))

        val action = new Harness(
          viewConnector = viewCacheConnector,
          updateConnector = updateCacheConnector,
          lockConnector = lockRepoConnector,
          mode = UpdateMode,
          srn = srnOpt)

        val futureResult = action.callTransform(authRequest)

        whenReady(futureResult) { result =>
          result.userAnswers.isDefined mustBe true
          result.userAnswers.get mustBe UserAnswers(testData)
        }
      }
    }
  }
}
