/*
 * Copyright 2024 HM Revenue & Customs
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

package services

import base.SpecBase
import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.*
import identifiers.TypedIdentifier
import identifiers.register.trustees
import identifiers.register.trustees.individual.TrusteeAddressId
import models.*
import models.address.Address
import models.requests.DataRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfter
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsArray, JsBoolean, JsObject, JsString, JsValue, Json}
import play.api.mvc.AnyContent
import play.api.mvc.Results.Ok
import uk.gov.hmrc.http.HeaderCarrier
import utils.{FakeDataRequest, UserAnswers}

import scala.concurrent.Future

class UserAnswersServiceSpec extends AsyncWordSpec with Matchers with MockitoSugar with BeforeAndAfter {

  import UserAnswersServiceSpec.*

  before(reset(subscriptionConnector))

  ".save" must {

    "save data with subscriptionConnector in NormalMode" in {

      when(subscriptionConnector.save(any(), any(), any())(any(), any(), any()))
        .thenReturn(Future(json))

      testServiceEstAndTrustees.save(NormalMode, EmptyOptionalSchemeReferenceNumber, FakeIdentifier, "foobar") map {
        _ mustEqual json
      }
    }

    "save data with updateSchemeCacheConnector in UpdateMode when logged in User holds the lock" in {

      when(lockConnector.lock(any(), any())(any(), any()))
        .thenReturn(Future(VarianceLock))

      when(viewConnector.removeAll(any())(any(), any()))
        .thenReturn(Future(Ok))

      when(updateConnector.upsert(any(), any())(any(), any()))
        .thenReturn(Future(json))

      testServiceInsurance.save(UpdateMode, OptionalSchemeReferenceNumber(Some(srn)), FakeIdentifier, "foobar") map { result =>
        result mustEqual json
      }
    }

    "not perform any action UpdateMode/ CheckUpdateMode when user does not hold the lock" in {

      when(lockConnector.lock(any(), any())(any(), any()))
        .thenReturn(Future(SchemeLock))

      testServiceEstAndTrustees.save(UpdateMode, OptionalSchemeReferenceNumber(Some(srn)), FakeIdentifier, "foobar") map { result =>
        result mustEqual Json.obj()
      }
    }

    "throw MissingSrnNumber exception in UpdateMode/ CheckUpdateMode if srn is missing" in {

      when(lockConnector.lock(any(), any())(any(), any()))
        .thenReturn(Future(SchemeLock))


      recoverToSucceededIf[testServiceEstAndTrustees.MissingSrnNumber.type] {
        testServiceEstAndTrustees.save(UpdateMode, EmptyOptionalSchemeReferenceNumber, FakeIdentifier, "foobar")
      }

    }

  }

  ".save without a change id" must {

    "save data with subscriptionConnector in NormalMode" in {

      when(subscriptionConnector.save(any(), any(), any())(any(), any(), any()))
        .thenReturn(Future(json))

      testServiceNotAnnotated.save(NormalMode, EmptyOptionalSchemeReferenceNumber, FakeIdentifier, "foobar") map {
        _ mustEqual json
      }
    }

    "save data with updateSchemeCacheConnector in UpdateMode when logged in User holds the lock" in {

      when(lockConnector.lock(any(), any())(any(), any()))
        .thenReturn(Future(VarianceLock))

      when(updateConnector.save(any(), any(), any())(any(), any(), any()))
        .thenReturn(Future(json))

      testServiceNotAnnotated.save(UpdateMode, OptionalSchemeReferenceNumber(Some(srn)), FakeIdentifier, "foobar") map { result =>
        result mustEqual json
      }
    }

  }

  ".upsert" must {

    "upsert data with subscriptionConnector in CheckMode" in {

      when(subscriptionConnector.upsert(any(), any())(any(), any()))
        .thenReturn(Future(json))

      testServiceNotAnnotated.upsert(NormalMode, EmptyOptionalSchemeReferenceNumber, json) map {
        _ mustEqual json
      }
    }

    "upsert data with updateSchemeCacheConnector in UpdateMode when logged in User holds the lock" in {

      when(lockConnector.lock(any(), any())(any(), any()))
        .thenReturn(Future(VarianceLock))

      when(updateConnector.upsert(any(), any())(any(), any()))
        .thenReturn(Future(json))

      testServiceNotAnnotated.upsert(UpdateMode, OptionalSchemeReferenceNumber(Some(srn)), json) map { result =>
        result mustEqual json
      }
    }

    "not perform any action UpdateMode/ CheckUpdateMode when user does not hold the lock" in {

      when(lockConnector.lock(any(), any())(any(), any()))
        .thenReturn(Future(SchemeLock))

      testServiceNotAnnotated.upsert(UpdateMode, OptionalSchemeReferenceNumber(Some(srn)), json) map { result =>
        result mustEqual Json.obj()
      }
    }

  }

  ".upsert with a change id" must {

    "upsert data with subscriptionConnector in CheckMode" in {

      when(subscriptionConnector.upsert(any(), any())(any(), any()))
        .thenReturn(Future(json))

      testServiceEstAndTrustees.upsert(NormalMode, EmptyOptionalSchemeReferenceNumber, json) map {
        _ mustEqual json
      }
    }

    "upsert data with updateSchemeCacheConnector in UpdateMode when logged in User holds the lock" in {

      when(lockConnector.lock(any(), any())(any(), any()))
        .thenReturn(Future(VarianceLock))

      when(viewConnector.removeAll(any())(any(), any()))
        .thenReturn(Future(Ok))

      when(updateConnector.upsert(any(), any())(any(), any()))
        .thenReturn(Future(json))

      testServiceInsurance.upsert(UpdateMode, OptionalSchemeReferenceNumber(OptionalSchemeReferenceNumber(Some(srn))), json) map { result =>
        result mustEqual json
      }
    }

    "not perform any action UpdateMode/ CheckUpdateMode when user does not hold the lock" in {

      when(lockConnector.lock(any(), any())(any(), any()))
        .thenReturn(Future(SchemeLock))

      testServiceEstAndTrustees.upsert(UpdateMode, OptionalSchemeReferenceNumber(OptionalSchemeReferenceNumber(Some(srn))), json) map { result =>
        result mustEqual Json.obj()
      }
    }

  }

  ".remove" must {
    "remove existing data in NormalMode" in {

      when(subscriptionConnector.remove(any(), any())(any(), any()))
        .thenReturn(Future(json))

      testServiceEstAndTrustees.remove(NormalMode, EmptyOptionalSchemeReferenceNumber, FakeIdentifier) map {
        _ mustEqual json
      }
    }

    "remove existing data in UpdateMode/ CheckUpdateMode when user holds the lock" in {

      val updatedJson = Json.obj(
        "other-key" -> "meh"
      )

      when(lockConnector.lock(any(), any())(any(), any()))
        .thenReturn(Future(VarianceLock))

      when(viewConnector.removeAll(any())(any(), any()))
        .thenReturn(Future(Ok))

      when(updateConnector.remove(any(), any())(any(), any()))
        .thenReturn(Future(updatedJson))

      testServiceEstAndTrustees.remove(UpdateMode, OptionalSchemeReferenceNumber(Some(srn)), FakeIdentifier) map {
        _ mustEqual updatedJson
      }
    }

    "not perform any action UpdateMode/ CheckUpdateMode when user does not hold the lock" in {
      when(lockConnector.lock(any(), any())(any(), any()))
        .thenReturn(Future(SchemeLock))

      testServiceEstAndTrustees.remove(UpdateMode, OptionalSchemeReferenceNumber(Some(srn)), FakeIdentifier) map {
        _ mustEqual Json.obj()
      }
    }
  }

  "setExistingAddress" when {
    val address = Address("line1", "line2", None, None, None, "GB")
    "in CheckUpdateMode" must {
      "save the existing address" in {
        val answers = UserAnswers().set(TrusteeAddressId(0))(address).asOpt.value
        val expectedAnswers = answers.set(trustees.ExistingCurrentAddressId(0))(address).asOpt.value

        testServiceEstAndTrustees.setExistingAddress(CheckUpdateMode, TrusteeAddressId(0), answers) mustEqual expectedAnswers
      }

      "not save the existing address and return the same user answers if there is no current address" in {
        val answers = UserAnswers()

        testServiceEstAndTrustees.setExistingAddress(CheckUpdateMode, TrusteeAddressId(0), answers) mustEqual answers
      }
    }

    "in Normal Mode" must {
      "not save the existing address and return the same user answers" in {
        val answers = UserAnswers()
        testServiceEstAndTrustees.setExistingAddress(CheckUpdateMode, TrusteeAddressId(0), answers) mustEqual answers
      }
    }
  }

  "removeEmptyObjectsAndIncompleteEntities" must {
    "only remove empty objects and objects containing only entityKind and isNew" in {

      val json: JsObject =
        Json.obj(
          "trustees" -> Json.arr(
            Json.obj(),
            Json.obj(
              "isTrusteeNew" -> true,
              "trusteeKind"  -> "individual"
            ),
            Json.obj(
              "isTrusteeNew" -> true,
              "trusteeKind"  -> "individual",
              "key"          -> "value"
            )
          )
        )

      val filteredJson: JsValue =
        testServiceEstAndTrustees.removeEmptyObjectsAndIncompleteEntities(
          json          = json,
          collectionKey = "trustees",
          keySet        = Set("isTrusteeNew", "trusteeKind"),
          externalId    = "externalId"
        )

      val parsedArray: collection.IndexedSeq[JsValue] =
        (filteredJson \ "trustees").get.as[JsArray].value

      parsedArray.size.mustBe(1)
      parsedArray.head.as[JsObject].value("isTrusteeNew").mustBe(JsBoolean(true))
      parsedArray.head.as[JsObject].value("trusteeKind").mustBe(JsString("individual"))
      parsedArray.head.as[JsObject].value("key").mustBe(JsString("value"))
    }
  }
}

object UserAnswersServiceSpec extends SpecBase with MockitoSugar {

  protected object FakeIdentifier extends TypedIdentifier[String] {
    override def toString: String = "fake-identifier"
  }

  protected object FakeAddressYearsIdentifier extends TypedIdentifier[AddressYears] {
    override def toString: String = "fake-identifier"
  }

  protected object FakePreviousAddressIdentifier extends TypedIdentifier[Address] {
    override def toString: String = "fake-identifier"
  }

  protected object FakeCompleteIdentifier extends TypedIdentifier[Boolean] {
    override def toString: String = "fake-complete"
  }

  protected implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val request: DataRequest[AnyContent] = FakeDataRequest(UserAnswers(Json.obj()))
  private val srn = SchemeReferenceNumber("S1234567890")

  val json: JsValue = Json.obj(
    FakeIdentifier.toString -> "fake value",
    "other-key" -> "meh"
  )

  class TestServiceNotAnnotated @Inject()(override val subscriptionCacheConnector: UserAnswersCacheConnector,
                                          override val updateSchemeCacheConnector: UpdateSchemeCacheConnector,
                                          override val lockConnector: PensionSchemeVarianceLockConnector,
                                          override val viewConnector: SchemeDetailsReadOnlyCacheConnector,
                                          override val appConfig: FrontendAppConfig
                                         ) extends UserAnswersService

  class TestServiceEstAndTrustees @Inject()(override val subscriptionCacheConnector: UserAnswersCacheConnector,
                                            override val updateSchemeCacheConnector: UpdateSchemeCacheConnector,
                                            override val lockConnector: PensionSchemeVarianceLockConnector,
                                            override val viewConnector: SchemeDetailsReadOnlyCacheConnector,
                                            override val appConfig: FrontendAppConfig
                                           ) extends UserAnswersServiceEstablishersAndTrusteesImpl(subscriptionCacheConnector, updateSchemeCacheConnector, lockConnector, viewConnector, appConfig)


  class TestServiceInsurance @Inject()(override val subscriptionCacheConnector: UserAnswersCacheConnector,
                                       override val updateSchemeCacheConnector: UpdateSchemeCacheConnector,
                                       override val lockConnector: PensionSchemeVarianceLockConnector,
                                       override val viewConnector: SchemeDetailsReadOnlyCacheConnector,
                                       override val appConfig: FrontendAppConfig
                                      ) extends UserAnswersServiceInsuranceImpl(subscriptionCacheConnector, updateSchemeCacheConnector, lockConnector, viewConnector, appConfig)

  protected val subscriptionConnector: UserAnswersCacheConnector = mock[SubscriptionCacheConnector]
  protected val updateConnector: UpdateSchemeCacheConnector = mock[UpdateSchemeCacheConnector]
  protected val lockConnector: PensionSchemeVarianceLockConnector = mock[PensionSchemeVarianceLockConnector]
  protected val viewConnector: SchemeDetailsReadOnlyCacheConnector = mock[SchemeDetailsReadOnlyCacheConnector]

  protected lazy val testServiceNotAnnotated: UserAnswersService =
    new TestServiceNotAnnotated(subscriptionConnector, updateConnector, lockConnector, viewConnector, frontendAppConfig)

  protected lazy val testServiceEstAndTrustees: TestServiceEstAndTrustees =
    new TestServiceEstAndTrustees(subscriptionConnector, updateConnector, lockConnector, viewConnector, frontendAppConfig)

  protected lazy val testServiceInsurance: TestServiceInsurance =
    new TestServiceInsurance(subscriptionConnector, updateConnector, lockConnector, viewConnector, frontendAppConfig)

}
