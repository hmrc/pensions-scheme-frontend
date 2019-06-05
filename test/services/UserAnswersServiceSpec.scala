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
import connectors.{PensionSchemeVarianceLockConnector, SchemeDetailsReadOnlyCacheConnector, SubscriptionCacheConnector, UpdateSchemeCacheConnector}
import identifiers.TypedIdentifier
import identifiers.register.establishers.IsEstablisherCompleteId
import identifiers.register.establishers.company.IsCompanyCompleteId
import identifiers.register.establishers.company.director.{DirectorDetailsId, IsDirectorCompleteId}
import identifiers.register.establishers.partnership.partner._
import identifiers.register.establishers.partnership.{IsPartnershipCompleteId, partner}
import identifiers.register.trustees
import identifiers.register.trustees.company.{CompanyAddressYearsId => TruesteeCompanyAddressYearsId, CompanyPreviousAddressId => TruesteeCompanyPreviousAddressId}
import identifiers.register.trustees.individual.{TrusteeAddressId, TrusteeAddressYearsId}
import identifiers.register.trustees.{IsTrusteeCompleteId, IsTrusteeNewId}
import models.AddressYears.{OverAYear, UnderAYear}
import models._
import models.address.Address
import models.person.PersonDetails
import models.requests.DataRequest
import org.joda.time.LocalDate
import org.mockito.Matchers.any
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{AsyncWordSpec, BeforeAndAfter, MustMatchers}
import play.api.libs.json.{Format, JsValue, Json}
import play.api.mvc.AnyContent
import play.api.mvc.Results.Ok
import uk.gov.hmrc.http.HeaderCarrier
import utils.{FakeDataRequest, UserAnswers}

import scala.concurrent.{ExecutionContext, Future}

class UserAnswersServiceSpec extends AsyncWordSpec with MustMatchers with MockitoSugar with BeforeAndAfter {

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

      when(viewConnector.removeAll(any())(any(), any()))
        .thenReturn(Future(Ok))

      when(updateConnector.upsert(any(), any())(any(), any()))
        .thenReturn(Future(json))

      testService.save(UpdateMode, Some(srn), FakeIdentifier, "foobar") map { result =>
        result mustEqual json
      }
    }

    "not perform any action UpdateMode/ CheckUpdateMode when user does not hold the lock" in {

      when(lockConnector.lock(any(), any())(any(), any()))
        .thenReturn(Future(SchemeLock))

      testService.save(UpdateMode, Some(srn), FakeIdentifier, "foobar") map { result =>
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

      when(viewConnector.removeAll(any())(any(), any()))
        .thenReturn(Future(Ok))

      when(updateConnector.upsert(any(), any())(any(), any()))
        .thenReturn(Future(json))

      testService.upsert(UpdateMode, Some(srn), json) map { result =>
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

      when(viewConnector.removeAll(any())(any(), any()))
        .thenReturn(Future(Ok))

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

  ".setCompleteForAddress" must {

    "return correct user answers with establisher complete flag and company complete flag if company is complete and all directors are complete" in {
      val answers = UserAnswers().set(IsDirectorCompleteId(0, 0))(true).flatMap(_.set(DirectorDetailsId(0, 0))(PersonDetails("sr", None, "test", new LocalDate()))).asOpt.value
      val expectedAnswers = answers.set(IsEstablisherCompleteId(0))(true).flatMap(
        _.set(IsCompanyCompleteId(0))(true)).asOpt.value

      testService.setCompleteForAddress(Some(IsCompanyCompleteId(0)), answers, UpdateMode, Some(srn)) mustEqual expectedAnswers
    }

    "return correct user answers with only company complete flag if company is complete and all directors are not complete" in {
      val answers = UserAnswers().set(IsDirectorCompleteId(0, 0))(false).flatMap(
        _.set(DirectorDetailsId(0, 0))(PersonDetails("sr", None, "test", new LocalDate()))).asOpt.value
      val expectedAnswers = answers.set(IsCompanyCompleteId(0))(true).asOpt.value
      testService.setCompleteForAddress(Some(IsCompanyCompleteId(0)), answers, UpdateMode, Some(srn)) mustEqual expectedAnswers
    }

    "return correct user answers with establisher complete flag and partnership complete flag if partnership is complete and all partners are complete" in {
      val answers = UserAnswers().set(IsPartnerCompleteId(0, 0))(true).flatMap(
        _.set(PartnerDetailsId(0, 0))(PersonDetails("sr", None, "test", new LocalDate()))).asOpt.value
      val expectedAnswers = answers.set(IsEstablisherCompleteId(0))(true).flatMap(
        _.set(IsPartnershipCompleteId(0))(true)).asOpt.value

      testService.setCompleteForAddress(Some(IsPartnershipCompleteId(0)), answers, UpdateMode, Some(srn)) mustEqual expectedAnswers
    }

    "return correct user answers with only partnership complete flag if partnership is complete and all partners are not complete" in {
      val answers = UserAnswers().set(IsPartnerCompleteId(0, 0))(false).flatMap(
        _.set(PartnerDetailsId(0, 0))(PersonDetails("sr", None, "test", new LocalDate()))).asOpt.value
      val expectedAnswers = answers.set(IsPartnershipCompleteId(0))(true).asOpt.value

      testService.setCompleteForAddress(Some(IsPartnershipCompleteId(0)), answers, UpdateMode, Some(srn)) mustEqual expectedAnswers
    }

    "return correct user answers with establisher complete flag if partnership is complete and all partners are complete" in {
      val answers = UserAnswers().set(PartnerDetailsId(0, 0))(PersonDetails("sr", None, "test", new LocalDate())).flatMap(
        _.set(IsPartnershipCompleteId(0))(true)
      ).asOpt.value
      val expectedAnswers = answers.set(IsEstablisherCompleteId(0))(true).flatMap(_.set(IsPartnerCompleteId(0, 0))(true)).asOpt.value

      testService.setCompleteForAddress(Some(partner.IsPartnerCompleteId(0, 0)), answers, UpdateMode, Some(srn)) mustEqual expectedAnswers
    }

    "return correct user answers with only partner complete flag if partnership is not complete but all partners are complete" in {
      val answers = UserAnswers().set(PartnerDetailsId(0, 0))(PersonDetails("sr", None, "test", new LocalDate())
      ).asOpt.value
      val expectedAnswers = answers.set(partner.IsPartnerCompleteId(0, 0))(true).asOpt.value

      testService.setCompleteForAddress(Some(partner.IsPartnerCompleteId(0, 0)), answers, UpdateMode, Some(srn)) mustEqual expectedAnswers
    }

    "return correct user answers with establisher complete flag if company is complete and all directors are complete" in {
      val answers = UserAnswers().set(DirectorDetailsId(0, 0))(PersonDetails("sr", None, "test", new LocalDate())).flatMap(
        _.set(IsCompanyCompleteId(0))(true)
      ).asOpt.value
      val expectedAnswers = answers.set(IsEstablisherCompleteId(0))(true).flatMap(_.set(IsDirectorCompleteId(0, 0))(true)).asOpt.value

      testService.setCompleteForAddress(Some(IsDirectorCompleteId(0, 0)), answers, UpdateMode, Some(srn)) mustEqual expectedAnswers
    }

    "return correct user answers with only director complete flag if company is not complete but all directors are complete" in {
      val answers = UserAnswers().set(DirectorDetailsId(0, 0))(PersonDetails("sr", None, "test", new LocalDate())
      ).asOpt.value
      val expectedAnswers = answers.set(IsDirectorCompleteId(0, 0))(true).asOpt.value

      testService.setCompleteForAddress(Some(IsDirectorCompleteId(0, 0)), answers, UpdateMode, Some(srn)) mustEqual expectedAnswers
    }

    "return the user answers with establisher complete flag if establisher individual is complete" in {
      val answers = UserAnswers(Json.obj())
      val expectedAnswers = answers.set(IsEstablisherCompleteId(0))(true).asOpt.value

      testService.setCompleteForAddress(Some(IsEstablisherCompleteId(0)), answers, UpdateMode, Some(srn)) mustEqual expectedAnswers
    }
  }

  "setAddressCompleteFlagAfterPreviousAddress" when {
    "in UpdateMode" must {
      "save the complete flag if the trustee is not new" in {
        val answers = UserAnswers().set(IsTrusteeNewId(0))(false).asOpt.value
        val expectedAnswers = answers.set(IsTrusteeCompleteId(0))(true).asOpt.value
        when(lockConnector.lock(any(), any())(any(), any()))
          .thenReturn(Future(VarianceLock))

        when(viewConnector.removeAll(any())(any(), any()))
          .thenReturn(Future(Ok))

        when(updateConnector.upsert(any(), any())(any(), any()))
          .thenReturn(Future(expectedAnswers.json))

        testService.setAddressCompleteFlagAfterPreviousAddress(UpdateMode, Some(srn), TruesteeCompanyPreviousAddressId(0), answers) map {
          _ mustEqual expectedAnswers
        }
      }

      "not save the complete flag and return the same useramswers if the trustee is new" in {
        val answers = UserAnswers().set(IsTrusteeNewId(0))(true).asOpt.value
        when(lockConnector.lock(any(), any())(any(), any()))
          .thenReturn(Future(VarianceLock))

        when(updateConnector.upsert(any(), any())(any(), any()))
          .thenReturn(Future(answers.json))

        testService.setAddressCompleteFlagAfterPreviousAddress(UpdateMode, Some(srn), TruesteeCompanyPreviousAddressId(0), answers) map {
          _ mustEqual answers
        }
      }
    }

    "in Normal Mode" must {
      "not save the complete flag and return the same useramswers if the trustee is new" in {
        val answers = UserAnswers()
        when(lockConnector.lock(any(), any())(any(), any()))
          .thenReturn(Future(VarianceLock))

        when(updateConnector.upsert(any(), any())(any(), any()))
          .thenReturn(Future(answers.json))

        testService.setAddressCompleteFlagAfterPreviousAddress(NormalMode, Some(srn), TruesteeCompanyPreviousAddressId(0), answers) map {
          _ mustEqual answers
        }
      }
    }
  }

  "setAddressCompleteFlagAfterAddressYear" when {
    "in UpdateMode" must {
      "save the complete flag if the trustee is not new and address years is over a year" in {
        val answers = UserAnswers().set(IsTrusteeNewId(0))(false).flatMap(
          _.set(TrusteeAddressYearsId(0))(OverAYear)).asOpt.value
        val expectedAnswers = answers.set(IsTrusteeCompleteId(0))(true).asOpt.value
        when(lockConnector.lock(any(), any())(any(), any()))
          .thenReturn(Future(VarianceLock))

        when(updateConnector.upsert(any(), any())(any(), any()))
          .thenReturn(Future(expectedAnswers.json))

        testService.setAddressCompleteFlagAfterAddressYear(UpdateMode, Some(srn), TruesteeCompanyAddressYearsId(0), OverAYear, answers) map {
          _ mustEqual expectedAnswers
        }
      }

      "not save the complete flag and return the same user answers if the address years in under a year" in {
        val answers = UserAnswers().set(TruesteeCompanyAddressYearsId(0))(UnderAYear).asOpt.value
        when(lockConnector.lock(any(), any())(any(), any()))
          .thenReturn(Future(VarianceLock))

        when(updateConnector.upsert(any(), any())(any(), any()))
          .thenReturn(Future(answers.json))

        testService.setAddressCompleteFlagAfterAddressYear(UpdateMode, Some(srn), TruesteeCompanyAddressYearsId(0), UnderAYear, answers) map {
          _ mustEqual answers
        }
      }

      "not save the complete flag and return the same user answers if the trustee is new" in {
        val answers = UserAnswers().set(IsTrusteeNewId(0))(true).asOpt.value
        when(lockConnector.lock(any(), any())(any(), any()))
          .thenReturn(Future(VarianceLock))

        when(updateConnector.upsert(any(), any())(any(), any()))
          .thenReturn(Future(answers.json))

        testService.setAddressCompleteFlagAfterAddressYear(UpdateMode, Some(srn), TruesteeCompanyAddressYearsId(0), OverAYear, answers) map {
          _ mustEqual answers
        }
      }
    }

    "in Normal Mode" must {
      "not save the complete flag and return the same useramswers if the trustee is new" in {
        val answers = UserAnswers()
        when(lockConnector.lock(any(), any())(any(), any()))
          .thenReturn(Future(VarianceLock))

        when(updateConnector.upsert(any(), any())(any(), any()))
          .thenReturn(Future(answers.json))

        testService.setAddressCompleteFlagAfterAddressYear(UpdateMode, Some(srn), TruesteeCompanyAddressYearsId(0), OverAYear, answers) map {
          _ mustEqual answers
        }
      }
    }
  }

  "setExistingAddress" when {
    val address = Address("line1", "line2", None, None, None, "GB")
    "in CheckUpdateMode" must {
      "save the existing address" in {
        val answers = UserAnswers().set(TrusteeAddressId(0))(address).asOpt.value
        val expectedAnswers = answers.set(trustees.ExistingCurrentAddressId(0))(address).asOpt.value

        testService.setExistingAddress(CheckUpdateMode, TrusteeAddressId(0), answers) mustEqual expectedAnswers
      }

      "not save the existing address and return the same user answers if there is no current address" in {
        val answers = UserAnswers()

        testService.setExistingAddress(CheckUpdateMode, TrusteeAddressId(0), answers) mustEqual answers
      }
    }
  }

  "in Normal Mode" must {
    "not save the existing address and return the same user answers" in {
      val answers = UserAnswers()
      testService.setExistingAddress(CheckUpdateMode, TrusteeAddressId(0), answers) mustEqual answers
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
                              override val viewConnector: SchemeDetailsReadOnlyCacheConnector,
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
  protected val viewConnector: SchemeDetailsReadOnlyCacheConnector = mock[SchemeDetailsReadOnlyCacheConnector]


  protected lazy val testService: UserAnswersService = new TestService(subscriptionConnector,
    updateConnector, lockConnector, viewConnector, frontendAppConfig)


}