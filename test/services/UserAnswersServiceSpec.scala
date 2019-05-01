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

package services

import base.SpecBase
import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.{PensionSchemeVarianceLockConnector, SubscriptionCacheConnector, UpdateSchemeCacheConnector}
import identifiers.TypedIdentifier
import identifiers.register.establishers.IsEstablisherAddressCompleteId
import identifiers.register.establishers.individual.{AddressYearsId => EstablisherIndividualAddressYearsId, PreviousAddressId => EstablisherIndividualPreviousAddressId}
import identifiers.register.establishers.partnership.partner.{IsPartnerAddressCompleteId, PartnerAddressYearsId, PartnerPreviousAddressId}
import identifiers.register.trustees.IsTrusteeAddressCompleteId
import identifiers.register.trustees.company.{CompanyAddressYearsId => TruesteeCompanyAddressYearsId, CompanyPreviousAddressId => TruesteeCompanyPreviousAddressId}
import models.AddressYears.{OverAYear, UnderAYear}
import models._
import models.address.Address
import models.requests.DataRequest
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{AsyncWordSpec, BeforeAndAfter, MustMatchers}
import play.api.libs.json.{Format, JsValue, Json}
import play.api.mvc.AnyContent
import uk.gov.hmrc.http.HeaderCarrier
import utils.{FakeDataRequest, UserAnswers}

import scala.concurrent.{ExecutionContext, Future}

class UserAnswersServiceSpec extends AsyncWordSpec with MustMatchers with MockitoSugar with BeforeAndAfter{

  import UserAnswersServiceSpec._

  before(reset(subscriptionConnector))

  ".save" must {

    "save data with subscriptionConnector in NormalMode" in {

      when(subscriptionConnector.save(any(), any(), any())(any(), any(), any()))
        .thenReturn(Future(json))

      testService.save(NormalMode, None, FakeIdentifier, "foobar") map {
        _ mustEqual json
      }
    }

    "save data with updateSchemeCacheConnector in UpdateMode when logged in User holds the lock" in {

      when(lockConnector.lock(any(), any())(any(), any()))
        .thenReturn(Future(VarianceLock))

      when(updateConnector.upsert(any(), any())(any(), any()))
        .thenReturn(Future(json))

      testService.save(UpdateMode, Some(srn), FakeIdentifier, "foobar") map {result =>
        result mustEqual json
      }
    }

    "not perform any action UpdateMode/ CheckUpdateMode when user does not hold the lock" in {

      when(lockConnector.lock(any(), any())(any(), any()))
        .thenReturn(Future(SchemeLock))

      testService.save(UpdateMode, Some(srn), FakeIdentifier, "foobar") map {result =>
        result mustEqual Json.obj()
      }
    }

  }

  ".upsert" must {

    "upsert data with subscriptionConnector in CheckMode" in {

      when(subscriptionConnector.upsert(any(), any())(any(), any()))
        .thenReturn(Future(json))

      testService.upsert(CheckMode, None, json) map {
        _ mustEqual json
      }
    }

    "upsert data with updateSchemeCacheConnector in UpdateMode when logged in User holds the lock" in {

      when(lockConnector.lock(any(), any())(any(), any()))
        .thenReturn(Future(VarianceLock))

      when(updateConnector.upsert(any(), any())(any(), any()))
        .thenReturn(Future(json))

      testService.upsert(UpdateMode, Some(srn), json) map {result =>
        result mustEqual json
      }
    }

    "not perform any action UpdateMode/ CheckUpdateMode when user does not hold the lock" in {

      when(lockConnector.lock(any(), any())(any(), any()))
        .thenReturn(Future(SchemeLock))

      testService.upsert(UpdateMode, Some(srn), json) map { result =>
        result mustEqual Json.obj()
      }
    }

  }

  ".remove" must {
    "remove existing data in NormalMode" in {

      when(subscriptionConnector.remove(any(), any())(any(), any()))
        .thenReturn(Future(json))

      testService.remove(NormalMode, None, FakeIdentifier) map {
        _ mustEqual json
      }
    }

    "remove existing data in UpdateMode/ CheckUpdateMode when user holds the lock" in {

      val updatedJson = Json.obj(
        "other-key" -> "meh"
      )

      when(lockConnector.lock(any(), any())(any(), any()))
        .thenReturn(Future(VarianceLock))

      when(updateConnector.remove(any(), any())(any(), any()))
        .thenReturn(Future(updatedJson))

      testService.remove(UpdateMode, Some(srn), FakeIdentifier) map {
        _ mustEqual updatedJson
      }
    }

    "not perform any action UpdateMode/ CheckUpdateMode when user does not hold the lock" in {
      when(lockConnector.lock(any(), any())(any(), any()))
        .thenReturn(Future(SchemeLock))

      testService.remove(UpdateMode, Some(srn), FakeIdentifier) map {
        _ mustEqual Json.obj()
      }
    }
  }

  ".setAddressCompleteFlagAfterAddressYear" must {

    "save flag with correct id for TruesteeCompanyAddressYearsId" in {

      when(subscriptionConnector.save[Boolean, TypedIdentifier[Boolean]](any(), Matchers.eq(IsTrusteeAddressCompleteId(0)), Matchers.eq(true))(any(), any(), any()))
        .thenReturn(Future(json))

      testService.setAddressCompleteFlagAfterAddressYear(NormalMode, None, TruesteeCompanyAddressYearsId(0), OverAYear, UserAnswers(json)) map {
        _ mustEqual UserAnswers(json)
      }
    }

    "save flag with correct id for trustee EstablisherIndividualAddressYearsId" in {

      when(subscriptionConnector.save[Boolean, TypedIdentifier[Boolean]](any(), Matchers.eq(IsEstablisherAddressCompleteId(0)), Matchers.eq(true))(any(), any(), any()))
        .thenReturn(Future(json))

      testService.setAddressCompleteFlagAfterAddressYear(NormalMode, None, EstablisherIndividualAddressYearsId(0), OverAYear, UserAnswers(json)) map {
        _ mustEqual UserAnswers(json)
      }
    }

    "save flag with correct id for establisher partner" in {

      when(subscriptionConnector.save[Boolean, TypedIdentifier[Boolean]](any(), Matchers.eq(IsPartnerAddressCompleteId(0, 0)), Matchers.eq(true))(any(), any(), any()))
        .thenReturn(Future(json))

      testService.setAddressCompleteFlagAfterAddressYear(NormalMode, None, PartnerAddressYearsId(0, 0), OverAYear, UserAnswers(json)) map {
        _ mustEqual UserAnswers(json)
      }
    }

    "save flag with false if under year selected" in {

      when(subscriptionConnector.save[Boolean, TypedIdentifier[Boolean]](any(), Matchers.eq(IsPartnerAddressCompleteId(0, 0)), Matchers.eq(false))(any(), any(), any()))
        .thenReturn(Future(json))

      testService.setAddressCompleteFlagAfterAddressYear(NormalMode, None, PartnerAddressYearsId(0, 0), UnderAYear, UserAnswers(json)) map {
        _ mustEqual UserAnswers(json)
      }
    }

  }

  ".setAddressCompleteFlagAfterPreviousAddress" must {

    "save flag with correct id for TruesteeCompanyPreviousAddressId" in {

      when(subscriptionConnector.save[Boolean, TypedIdentifier[Boolean]](any(), Matchers.eq(IsTrusteeAddressCompleteId(0)), Matchers.eq(true))(any(), any(), any()))
        .thenReturn(Future(json))

      testService.setAddressCompleteFlagAfterPreviousAddress(NormalMode, None, TruesteeCompanyPreviousAddressId(0), UserAnswers(json)) map {
        _ mustEqual UserAnswers(json)
      }
    }

    "save flag with correct id for trustee EstablisherIndividualPreviousAddressId" in {

      when(subscriptionConnector.save[Boolean, TypedIdentifier[Boolean]](any(), Matchers.eq(IsEstablisherAddressCompleteId(0)), Matchers.eq(true))(any(), any(), any()))
        .thenReturn(Future(json))

      testService.setAddressCompleteFlagAfterPreviousAddress(NormalMode, None, EstablisherIndividualPreviousAddressId(0), UserAnswers(json)) map {
        _ mustEqual UserAnswers(json)
      }
    }

    "save flag with correct id for establisher PartnerPreviousAddressId" in {

      when(subscriptionConnector.save[Boolean, TypedIdentifier[Boolean]](any(), Matchers.eq(IsPartnerAddressCompleteId(0, 0)), Matchers.eq(true))(any(), any(), any()))
        .thenReturn(Future(json))

      testService.setAddressCompleteFlagAfterPreviousAddress(NormalMode, None, PartnerPreviousAddressId(0, 0), UserAnswers(json)) map {
        _ mustEqual UserAnswers(json)
      }
    }

    "dont save flag if not macthing addresss id" in {

      case class TempAddressId() extends TypedIdentifier[Address]

      testService.setAddressCompleteFlagAfterPreviousAddress(NormalMode, None, TempAddressId(), UserAnswers(json)) map { result =>
        verify(subscriptionConnector, never).save(any(), any(), any())(any(), any(), any())
        result mustEqual UserAnswers(json)
      }
    }
  }
}

object UserAnswersServiceSpec extends SpecBase with MockitoSugar {

  protected object FakeIdentifier extends TypedIdentifier[String] {
    override def toString: String = "fake-identifier"
  }

  protected object FakeChangeIdentifier extends TypedIdentifier[Boolean] {
    override def toString: String = "fake-change-identifier"
  }

  protected implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val request: DataRequest[AnyContent] = FakeDataRequest(UserAnswers(Json.obj()))
  private val srn = "S1234567890"

  val json = Json.obj(
    FakeIdentifier.toString -> "fake value",
    "other-key" -> "meh"
  )

  class TestService @Inject()(override val subscriptionCacheConnector: SubscriptionCacheConnector,
                              override val updateSchemeCacheConnector: UpdateSchemeCacheConnector,
                              override val lockConnector: PensionSchemeVarianceLockConnector,
                              override val appConfig: FrontendAppConfig
                             ) extends UserAnswersService {

    override def save[A, I <: TypedIdentifier[A]](mode: Mode, srn: Option[String], id: I, value: A)
                                                 (implicit fmt: Format[A], ec: ExecutionContext, hc: HeaderCarrier,
                                                  request: DataRequest[AnyContent]): Future[JsValue] = {
      save(mode, srn, id, value, FakeChangeIdentifier)
    }
  }

  protected val subscriptionConnector: SubscriptionCacheConnector = mock[SubscriptionCacheConnector]
  protected val updateConnector: UpdateSchemeCacheConnector = mock[UpdateSchemeCacheConnector]
  protected val lockConnector: PensionSchemeVarianceLockConnector = mock[PensionSchemeVarianceLockConnector]


  protected lazy val testService: UserAnswersService = new TestService(subscriptionConnector,
    updateConnector, lockConnector, frontendAppConfig)


}