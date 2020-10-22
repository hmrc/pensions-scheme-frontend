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

package controllers.actions

import base.SpecBase
import connectors._
import identifiers.{IsPsaSuspendedId, SchemeSrnId, SchemeStatusId, UKBankAccountId}
import matchers.JsonMatchers
import models._
import models.requests.{AuthenticatedRequest, OptionalDataRequest}
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsNull, JsObject, Json}
import play.api.mvc.AnyContent
import uk.gov.hmrc.domain.PsaId
import utils.UserAnswers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DataRetrievalActionSpec extends SpecBase with MockitoSugar with ScalaFutures with JsonMatchers with BeforeAndAfterEach {

  private val srn = "123"
  private val srnOpt = Some(srn)
  private val psa = "A0000000"
  private val externalId = "id"
  private val authRequest: AuthenticatedRequest[AnyContent] = AuthenticatedRequest(fakeRequest, externalId, Some(PsaId(psa)))

  private val dataCacheConnector = mock[UserAnswersCacheConnector]
  private val viewCacheConnector = mock[SchemeDetailsReadOnlyCacheConnector]
  private val updateCacheConnector = mock[UpdateSchemeCacheConnector]
  private val lockRepoConnector = mock[PensionSchemeVarianceLockConnector]
  private val schemeDetailsConnector = mock[SchemeDetailsConnector]
  private val minimalPsaConnector = mock[MinimalPsaConnector]

  private val testData = Json.obj("test" -> "data")

  class Harness(dataConnector: UserAnswersCacheConnector = dataCacheConnector,
                viewConnector: SchemeDetailsReadOnlyCacheConnector = viewCacheConnector,
                updateConnector: UpdateSchemeCacheConnector = updateCacheConnector,
                lockConnector: PensionSchemeVarianceLockConnector = lockRepoConnector,
                schemeDetailsConnector: SchemeDetailsConnector = schemeDetailsConnector,
                minimalPsaConnector: MinimalPsaConnector = minimalPsaConnector,
                mode: Mode = NormalMode,
                srn: Option[String] = None,
                refreshData: Boolean = false
               ) extends
    DataRetrievalImpl(
      dataConnector = dataConnector,
      viewConnector = viewConnector,
      updateConnector = updateConnector,
      lockConnector = lockConnector,
      schemeDetailsConnector = schemeDetailsConnector,
      minimalPsaConnector = minimalPsaConnector,
      mode = mode,
      srn = srn,
      refreshData = refreshData
    ) {
    def callTransform[A](request: AuthenticatedRequest[A]): Future[OptionalDataRequest[A]] = transform(request)
  }

  private val statusOpen = "Open"
  private val statusPending = "Pending"

  private val answersFromDES = UserAnswers().set(SchemeStatusId)(statusOpen).asOpt.value
  private val answersFromDESWithSuspensionFlag = answersFromDES.set(IsPsaSuspendedId)(false).asOpt.value

  private def userAnswersDummy(status: String, srn: String) = UserAnswers().set(SchemeStatusId)(status).asOpt.value
    .set(SchemeSrnId)(srn).asOpt.value

  override def beforeEach(): Unit = {
    reset(dataCacheConnector, viewCacheConnector, updateCacheConnector,
      lockRepoConnector, schemeDetailsConnector, minimalPsaConnector)
    when(minimalPsaConnector.isPsaSuspended(any())(any(), any())).thenReturn(Future.successful(false))
    when(updateCacheConnector.upsert(any(), any())(any(), any()))
      .thenReturn(Future.successful(JsNull))
    when(viewCacheConnector.upsert(any(), any())(any(), any()))
      .thenReturn(Future.successful(JsNull))
    when(schemeDetailsConnector.getSchemeDetails(any(), any(), any())(any(), any()))
      .thenReturn(Future.successful(answersFromDES))
  }

  "transform" must {
    behave like requestTransformer(refreshData = false)

    "when refreshData is false and there is no data in the read-only cache in UpdateMode and " +
      s"lock is not held by psa set userAnswers to 'None' in the request" in {
      when(lockRepoConnector.isLockByPsaIdOrSchemeId(eqTo(psa), eqTo(srn))(any(), any())).thenReturn(Future(None))
      when(viewCacheConnector.fetch(eqTo(externalId))(any(), any())) thenReturn Future(None)
      val action = new Harness(viewConnector = viewCacheConnector,
        lockConnector = lockRepoConnector,
        mode = UpdateMode,
        srn = srnOpt,
        refreshData = false)

      val futureResult = action.callTransform(authRequest)

      whenReady(futureResult) { result =>
        result.userAnswers.isEmpty mustBe true
      }
    }

    "when refreshData is false and there is data in the read-only cache in UpdateMode and lock is not held by anyone " +
      "build a userAnswers object and add it to the request, acquire lock, save data to updateCache" in {
      val answers = UserAnswers().set(SchemeStatusId)(statusOpen).flatMap(_.set(SchemeSrnId)(srn)).asOpt.value.json
      when(lockRepoConnector.isLockByPsaIdOrSchemeId(eqTo(psa), eqTo(srn))(any(), any())).thenReturn(Future(None))
      when(viewCacheConnector.fetch(eqTo(externalId))(any(), any())) thenReturn Future.successful(Some(answers))

      val action = new Harness(
        viewConnector = viewCacheConnector,
        updateConnector = updateCacheConnector,
        lockConnector = lockRepoConnector,
        mode = UpdateMode,
        srn = srnOpt,
        refreshData = false)

      val futureResult = action.callTransform(authRequest)

      whenReady(futureResult) { result =>
        result.userAnswers.isDefined mustBe true
        result.viewOnly mustBe false
        result.userAnswers.get mustBe UserAnswers(answers)
      }
    }

    "when refreshData is false and there is data in the read-only cache in UpdateMode and lock is not held by anyone " +
      "status is not open, build a userAnswers object and add it to the request and set view only to true" in {
      val answers = UserAnswers().set(SchemeStatusId)(statusPending).asOpt.value.json
      when(lockRepoConnector.isLockByPsaIdOrSchemeId(eqTo(psa), eqTo(srn))(any(), any())).thenReturn(Future(None))
      when(viewCacheConnector.fetch(eqTo(externalId))(any(), any())) thenReturn
        Future.successful(Some(answers))

      val action = new Harness(
        viewConnector = viewCacheConnector,
        updateConnector = updateCacheConnector,
        lockConnector = lockRepoConnector,
        mode = UpdateMode,
        srn = srnOpt,
        refreshData = false)

      val futureResult = action.callTransform(authRequest)

      whenReady(futureResult) { result =>
        result.userAnswers.isDefined mustBe true
        result.viewOnly mustBe true
        result.userAnswers.get mustBe UserAnswers(answers)
      }
    }

    "when refreshData is false and there is data in the read-only cache in UpdateMode and lock is not held by anyone " +
      "status is open and srn is different from cached srn then no user answers is added to the request" in {
      val answers = UserAnswers().set(SchemeStatusId)(statusOpen).flatMap(_.set(SchemeSrnId)("existing-srn")).asOpt.value.json
      when(lockRepoConnector.isLockByPsaIdOrSchemeId(eqTo(psa), eqTo(srn))(any(), any())).thenReturn(Future(None))
      when(viewCacheConnector.fetch(eqTo(externalId))(any(), any())) thenReturn
        Future.successful(Some(answers))

      val action = new Harness(
        viewConnector = viewCacheConnector,
        updateConnector = updateCacheConnector,
        lockConnector = lockRepoConnector,
        mode = UpdateMode,
        srn = srnOpt,
        refreshData = false)

      val futureResult = action.callTransform(authRequest)

      whenReady(futureResult) { result =>
        result.userAnswers.isDefined mustBe false
      }
    }

    "when refreshData is false and there is data in the read-only cache in UpdateMode and lock is not held by anyone " +
      "status is Pending and srn is different from cached srn then no user answers is added to the request" in {
      val answers = UserAnswers().set(SchemeStatusId)(statusPending).flatMap(_.set(SchemeSrnId)("existing-srn")).asOpt.value.json
      when(lockRepoConnector.isLockByPsaIdOrSchemeId(eqTo(psa), eqTo(srn))(any(), any())).thenReturn(Future(None))
      when(viewCacheConnector.fetch(eqTo(externalId))(any(), any())) thenReturn
        Future.successful(Some(answers))

      val action = new Harness(
        viewConnector = viewCacheConnector,
        updateConnector = updateCacheConnector,
        lockConnector = lockRepoConnector,
        mode = UpdateMode,
        srn = srnOpt,
        refreshData = false)

      val futureResult = action.callTransform(authRequest)

      whenReady(futureResult) { result =>
        result.userAnswers.isDefined mustBe false
      }
    }


    "when refreshData is false and there is data in the read-only cache in UpdateMode and lock is not held by anyone " +
      "status is open and srn is same as cached srn then user answers is added to the request and viewOnly is false" in {
      val answers = UserAnswers().set(SchemeStatusId)(statusOpen).flatMap(_.set(SchemeSrnId)(srn)).asOpt.value.json
      when(lockRepoConnector.isLockByPsaIdOrSchemeId(eqTo(psa), eqTo(srn))(any(), any())).thenReturn(Future(None))
      when(viewCacheConnector.fetch(eqTo(externalId))(any(), any())) thenReturn
        Future.successful(Some(answers))

      val action = new Harness(
        viewConnector = viewCacheConnector,
        updateConnector = updateCacheConnector,
        lockConnector = lockRepoConnector,
        mode = UpdateMode,
        srn = srnOpt,
        refreshData = false)

      val futureResult = action.callTransform(authRequest)

      whenReady(futureResult) { result =>
        result.userAnswers.isDefined mustBe true
        result.viewOnly mustBe false
      }
    }

    "when refreshData is false and there is data in the read-only cache in UpdateMode and lock is not held by anyone " +
      "when the scheme SRN is found in the user answers cache fetch data from viewConnector to build a userAnswers object and add it to the request" in {
      val testData = Json.obj(SchemeSrnId.toString -> srn)

      when(lockRepoConnector.isLockByPsaIdOrSchemeId(eqTo(psa), eqTo(srn))(any(), any())).thenReturn(Future(Some(SchemeLock)))
      when(viewCacheConnector.fetch(eqTo(externalId))(any(), any())) thenReturn Future.successful(Some(testData))

      val action = new Harness(
        viewConnector = viewCacheConnector,
        updateConnector = updateCacheConnector,
        lockConnector = lockRepoConnector,
        mode = UpdateMode,
        srn = srnOpt,
        refreshData = false)

      val futureResult = action.callTransform(authRequest)

      whenReady(futureResult) { result =>
        result.userAnswers.isDefined mustBe true
        result.userAnswers.get mustBe UserAnswers(testData)
      }
    }

    behave like requestTransformer(refreshData = true)

    "when refreshData is true and there is no data in the read-only cache in UpdateMode and " +
      s"lock is not held by psa then build new user answers with data retrieved from scheme details" in {
      val uaInsertedIntoViewCache = userAnswersDummy(status = statusOpen, srn = srn).set(UKBankAccountId)(true).asOpt.value
      when(lockRepoConnector.isLockByPsaIdOrSchemeId(eqTo(psa), eqTo(srn))(any(), any())).thenReturn(Future(None))
      when(viewCacheConnector.fetch(eqTo(externalId))(any(), any())) thenReturn Future(None)
      when(viewCacheConnector.upsert(any(), any())(any(), any())).thenReturn(Future.successful(uaInsertedIntoViewCache.json))

      val jsonCaptor = ArgumentCaptor.forClass(classOf[JsObject])

      val action = new Harness(viewConnector = viewCacheConnector,
        lockConnector = lockRepoConnector,
        mode = UpdateMode,
        srn = srnOpt,
        refreshData = true)

      val futureResult = action.callTransform(authRequest)

      whenReady(futureResult) { result =>
        result.userAnswers mustBe Some(uaInsertedIntoViewCache)
        verify(schemeDetailsConnector, times(1)).getSchemeDetails(any(), any(), any())(any(), any())
        verify(viewCacheConnector, times(1)).upsert(any(), jsonCaptor.capture())(any(), any())
        jsonCaptor.getValue must containJson(answersFromDESWithSuspensionFlag.json.as[JsObject])
      }
    }

    "when refreshData is true and there is data in the read-only cache in UpdateMode and lock is not held by anyone " +
      "overwrite data with new user answers with data retrieved from scheme details and add it to the request" +
      "and set view only to false" in {
      val uaInsertedIntoViewCache = userAnswersDummy(status = statusOpen, srn = srn).set(UKBankAccountId)(true).asOpt.value
      val answersAlreadyInViewCache = UserAnswers().set(SchemeStatusId)(statusOpen).flatMap(_.set(SchemeSrnId)(srn)).asOpt.value.json
      when(lockRepoConnector.isLockByPsaIdOrSchemeId(eqTo(psa), eqTo(srn))(any(), any())).thenReturn(Future(None))
      when(viewCacheConnector.fetch(eqTo(externalId))(any(), any())) thenReturn Future.successful(Some(answersAlreadyInViewCache))
      when(viewCacheConnector.upsert(any(), any())(any(), any())).thenReturn(Future.successful(uaInsertedIntoViewCache.json))

      val jsonCaptor = ArgumentCaptor.forClass(classOf[JsObject])

      val action = new Harness(
        viewConnector = viewCacheConnector,
        updateConnector = updateCacheConnector,
        lockConnector = lockRepoConnector,
        mode = UpdateMode,
        srn = srnOpt,
        refreshData = true)

      val futureResult = action.callTransform(authRequest)

      whenReady(futureResult) { result =>
        result.userAnswers mustBe Some(uaInsertedIntoViewCache)
        result.viewOnly mustBe false
        verify(schemeDetailsConnector, times(1)).getSchemeDetails(any(), any(), any())(any(), any())
        verify(viewCacheConnector, times(1)).upsert(any(), jsonCaptor.capture())(any(), any())
        jsonCaptor.getValue must containJson(answersFromDESWithSuspensionFlag.json.as[JsObject])
      }
    }

    "when refreshData is true and there is data in the read-only cache in UpdateMode and lock is not held by anyone " +
      "status is not open, then build new user answers with data retrieved from scheme details and set view only to true" in {
      val uaInsertedIntoViewCache = userAnswersDummy(status = statusPending, srn = srn).set(UKBankAccountId)(true).asOpt.value
      val answersAlreadyInViewCache = UserAnswers().set(SchemeStatusId)(statusPending).flatMap(_.set(SchemeSrnId)(srn)).asOpt.value.json
      when(lockRepoConnector.isLockByPsaIdOrSchemeId(eqTo(psa), eqTo(srn))(any(), any())).thenReturn(Future(None))
      when(viewCacheConnector.fetch(eqTo(externalId))(any(), any())) thenReturn Future.successful(Some(answersAlreadyInViewCache))
      when(viewCacheConnector.upsert(any(), any())(any(), any())).thenReturn(Future.successful(uaInsertedIntoViewCache.json))

      val jsonCaptor = ArgumentCaptor.forClass(classOf[JsObject])

      val action = new Harness(
        viewConnector = viewCacheConnector,
        updateConnector = updateCacheConnector,
        lockConnector = lockRepoConnector,
        mode = UpdateMode,
        srn = srnOpt,
        refreshData = true)

      val futureResult = action.callTransform(authRequest)

      whenReady(futureResult) { result =>
        result.userAnswers mustBe Some(uaInsertedIntoViewCache)
        result.viewOnly mustBe true
        verify(schemeDetailsConnector, times(1)).getSchemeDetails(any(), any(), any())(any(), any())
        verify(viewCacheConnector, times(1)).upsert(any(), jsonCaptor.capture())(any(), any())
        jsonCaptor.getValue must containJson(answersFromDESWithSuspensionFlag.json.as[JsObject])
      }
    }

    "when refreshData is true and there is data in the read-only cache in UpdateMode and lock is not held by anyone " +
      "status is open and srn is different from cached srn then no user answers is added to the request and view only is true" in {
      val differentSrn = "different-srn"
      val answersFromDES = userAnswersDummy(statusOpen, srn = differentSrn)
      val answersFromDESWithSuspensionFlag = answersFromDES.set(IsPsaSuspendedId)(false).asOpt.value
      val uaInsertedIntoViewCache = answersFromDESWithSuspensionFlag.set(UKBankAccountId)(true).asOpt.value
      val answersAlreadyInViewCache = UserAnswers().set(SchemeStatusId)(statusPending).flatMap(_.set(SchemeSrnId)(srn)).asOpt.value.json


      when(lockRepoConnector.isLockByPsaIdOrSchemeId(eqTo(psa), eqTo(srn))(any(), any())).thenReturn(Future(None))
      when(viewCacheConnector.fetch(eqTo(externalId))(any(), any())) thenReturn Future.successful(Some(answersAlreadyInViewCache))
      when(viewCacheConnector.upsert(any(), any())(any(), any())).thenReturn(Future.successful(uaInsertedIntoViewCache.json))
      when(schemeDetailsConnector.getSchemeDetails(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(answersFromDES))
      val jsonCaptor = ArgumentCaptor.forClass(classOf[JsObject])

      val action = new Harness(
        viewConnector = viewCacheConnector,
        updateConnector = updateCacheConnector,
        lockConnector = lockRepoConnector,
        mode = UpdateMode,
        srn = srnOpt,
        refreshData = true)

      val futureResult = action.callTransform(authRequest)

      whenReady(futureResult) { result =>
        result.userAnswers mustBe None
        result.viewOnly mustBe true
        verify(schemeDetailsConnector, times(1)).getSchemeDetails(any(), any(), any())(any(), any())
        verify(viewCacheConnector, times(1)).upsert(any(), jsonCaptor.capture())(any(), any())
        jsonCaptor.getValue must containJson(answersFromDESWithSuspensionFlag.set(SchemeSrnId)(srn).asOpt.value.json.as[JsObject])
      }
    }

    "when refreshData is true and there is data in the read-only cache in UpdateMode and lock is not held by anyone " +
      "status is Pending and srn is different from cached srn then no user answers is added to the request" in {
      val answers = UserAnswers().set(SchemeStatusId)(statusPending).flatMap(_.set(SchemeSrnId)("existing-srn")).asOpt.value.json
      when(lockRepoConnector.isLockByPsaIdOrSchemeId(eqTo(psa), eqTo(srn))(any(), any())).thenReturn(Future(None))
      when(viewCacheConnector.fetch(eqTo(externalId))(any(), any())) thenReturn
        Future.successful(Some(answers))
      when(viewCacheConnector.upsert(any(), any())(any(), any())).thenReturn(Future.successful(userAnswersDummy(statusOpen, "different-srn").json))

      val action = new Harness(
        viewConnector = viewCacheConnector,
        updateConnector = updateCacheConnector,
        lockConnector = lockRepoConnector,
        mode = UpdateMode,
        srn = srnOpt,
        refreshData = true)

      val futureResult = action.callTransform(authRequest)

      whenReady(futureResult) { result =>
        result.userAnswers.isDefined mustBe false
      }
    }
  }

  //scalastyle:off method.length
  private def requestTransformer(refreshData: Boolean): Unit = {
    s"when refreshData is $refreshData and there is no data in the cache in NormalMode set userAnswers to 'None' in the request" in {
      when(dataCacheConnector.fetch(eqTo("id"))(any(), any())) thenReturn Future(None)
      val action = new Harness(dataCacheConnector, refreshData = refreshData)

      val futureResult = action.callTransform(authRequest)

      whenReady(futureResult) { result =>
        result.userAnswers.isEmpty mustBe true
      }
    }

    s"when refreshData is $refreshData and there is data in the cache in NormalMode build a userAnswers object and add it to the request" in {
      when(dataCacheConnector.fetch(eqTo("id"))(any(), any())) thenReturn Future.successful(Some(testData))
      val action = new Harness(dataCacheConnector, refreshData = refreshData)

      val futureResult = action.callTransform(authRequest)

      whenReady(futureResult) { result =>
        result.userAnswers.isDefined mustBe true
      }
    }

    s"when refreshData is $refreshData and there is no srn in UpdateMode set userAnswers to 'None' in the request" in {
      val action = new Harness(viewConnector = viewCacheConnector, lockConnector = lockRepoConnector, mode = UpdateMode, srn = None, refreshData = refreshData)

      val futureResult = action.callTransform(authRequest)

      whenReady(futureResult) { result =>
        result.userAnswers.isEmpty mustBe true
      }
    }

    s"when refreshData is $refreshData and there is data in the update cache in UpdateMode and " +
      s"lock is held by psa then a userAnswers object is built and added to the request" in {
      when(lockRepoConnector.isLockByPsaIdOrSchemeId(eqTo(psa), eqTo(srn))(any(), any())).thenReturn(Future(Some(VarianceLock)))
      when(updateCacheConnector.fetch(eqTo(srn))(any(), any())) thenReturn Future.successful(Some(Json.obj()))
      val action = new Harness(updateConnector = updateCacheConnector,
        lockConnector = lockRepoConnector,
        mode = UpdateMode,
        srn = srnOpt,
        refreshData = refreshData)

      val futureResult = action.callTransform(authRequest)

      whenReady(futureResult) { result =>
        result.userAnswers.isDefined mustBe true
      }
    }

    s"when refreshData is $refreshData and there is data in the read-only cache in UpdateMode and lock is not held by anyone " +
      "and status is Pending and srn is same as cached srn then user answers is added to the request and viewOnly is true" in {
      val answers = UserAnswers().set(SchemeStatusId)(statusPending).flatMap(_.set(SchemeSrnId)(srn)).asOpt.value.json
      when(lockRepoConnector.isLockByPsaIdOrSchemeId(eqTo(psa), eqTo(srn))(any(), any())).thenReturn(Future(None))
      when(viewCacheConnector.fetch(eqTo(externalId))(any(), any())) thenReturn
        Future.successful(Some(answers))

      val action = new Harness(
        viewConnector = viewCacheConnector,
        updateConnector = updateCacheConnector,
        lockConnector = lockRepoConnector,
        mode = UpdateMode,
        srn = srnOpt,
        refreshData = refreshData)

      val futureResult = action.callTransform(authRequest)

      whenReady(futureResult) { result =>
        result.userAnswers.isDefined mustBe true
        result.viewOnly mustBe true
      }
    }

    s"when refreshData is $refreshData and there is data in the read-only cache in UpdateMode and lock is not held by anyone " +
      "and there is data in the read-only cache in UpdateMode and lock is held by someone else and " +
      "the scheme SRN is not found in the user answers cache then return no user answers" in {
      when(lockRepoConnector.isLockByPsaIdOrSchemeId(eqTo(psa), eqTo(srn))(any(), any())).thenReturn(Future(Some(SchemeLock)))
      when(viewCacheConnector.fetch(eqTo(externalId))(any(), any())) thenReturn Future.successful(Some(testData))

      val action = new Harness(
        viewConnector = viewCacheConnector,
        updateConnector = updateCacheConnector,
        lockConnector = lockRepoConnector,
        mode = UpdateMode,
        srn = srnOpt,
        refreshData = refreshData)

      val futureResult = action.callTransform(authRequest)

      whenReady(futureResult) { result =>
        result.userAnswers.isDefined mustBe false
      }
    }

    s"when refreshData is $refreshData and no SRN is defined for UpdateMode then " +
      "set userAnswers to 'None' in the request" in {
      when(dataCacheConnector.fetch(eqTo("id"))(any(), any())) thenReturn Future(None)
      val action = new Harness(updateConnector = updateCacheConnector, lockConnector = lockRepoConnector, mode = UpdateMode, refreshData = refreshData)

      val futureResult = action.callTransform(authRequest)

      whenReady(futureResult) { result =>
        result.userAnswers.isEmpty mustBe true
      }
    }
  }
}

