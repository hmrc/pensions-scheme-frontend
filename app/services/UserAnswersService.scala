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

import config.FrontendAppConfig
import connectors.{PensionSchemeVarianceLockConnector, SubscriptionCacheConnector, UpdateSchemeCacheConnector}
import identifiers.register.establishers.IsEstablisherAddressCompleteId
import identifiers.register.trustees.company.{CompanyAddressYearsId => TruesteeCompanyAddressYearsId, CompanyPreviousAddressId => TruesteeCompanyPreviousAddressId}
import identifiers.register.trustees.individual.{TrusteeAddressYearsId => TruesteeIndividualAddressYearsId, TrusteePreviousAddressId => TruesteeIndividualPreviousAddressId}
import identifiers.register.trustees.partnership.{PartnershipAddressYearsId => TruesteePartnershipAddressYearsId, PartnershipPreviousAddressId => TruesteePartnershipPreviousAddressId}
import identifiers.register.establishers.company.{CompanyAddressYearsId => EstablisherCompanyAddressYearsId, CompanyPreviousAddressId => EstablisherCompanyPreviousAddressId}
import identifiers.register.establishers.individual.{AddressYearsId => EstablisherIndividualAddressYearsId, PreviousAddressId => EstablisherIndividualPreviousAddressId}
import identifiers.register.establishers.partnership.{PartnershipAddressYearsId => EstablisherPartnershipAddressYearsId, PartnershipPreviousAddressId => EstablisherPartnershipPreviousAddressId}
import identifiers.register.establishers.partnership.partner.{IsPartnerAddressCompleteId, PartnerAddressYearsId, PartnerPreviousAddressId}
import identifiers.register.establishers.company.director.{DirectorAddressYearsId, DirectorPreviousAddressId, IsDirectorAddressCompleteId}
import identifiers.register.trustees.IsTrusteeAddressCompleteId
import identifiers.{EstablishersOrTrusteesChangedId, InsuranceDetailsChangedId, TypedIdentifier}
import javax.inject.{Inject, Singleton}
import models.AddressYears.{OverAYear, UnderAYear}
import models.address.Address
import models.requests.DataRequest
import models.{Mode, _}
import play.api.libs.json.{Format, JsResultException, JsValue, Json}
import play.api.mvc.AnyContent
import uk.gov.hmrc.http.HeaderCarrier
import utils.UserAnswers

import scala.concurrent.{ExecutionContext, Future}

trait UserAnswersService {

  protected def subscriptionCacheConnector: SubscriptionCacheConnector

  protected def updateSchemeCacheConnector: UpdateSchemeCacheConnector
  protected def lockConnector: PensionSchemeVarianceLockConnector
  protected def appConfig: FrontendAppConfig

  case class MissingSrnNumber() extends Exception

  def save[A, I <: TypedIdentifier[A]](mode: Mode, srn: Option[String], id: I, value: A)
                                      (implicit fmt: Format[A],
                                       ec: ExecutionContext,
                                       hc: HeaderCarrier,
                                       request: DataRequest[AnyContent]
                                      ): Future[JsValue] = {
    mode match {
      case NormalMode | CheckMode => subscriptionCacheConnector.save(request.externalId, id, value)
      case UpdateMode | CheckUpdateMode => lockAndCall(srn, updateSchemeCacheConnector.save(_, id, value))
    }
  }

  def save[A, I <: TypedIdentifier[A]](mode: Mode, srn: Option[String], id: I, value: A,
                                       changeId: TypedIdentifier[Boolean])
                                      (implicit fmt: Format[A],
                                       ec: ExecutionContext,
                                       hc: HeaderCarrier,
                                       request: DataRequest[AnyContent]): Future[JsValue] = {
    mode match {
      case NormalMode | CheckMode => subscriptionCacheConnector.save(request.externalId, id, value)
      case UpdateMode | CheckUpdateMode =>
        val answers = request.userAnswers
          .set(id)(value).flatMap {
          _.set(changeId)(true)
        }.asOpt.getOrElse(request.userAnswers)

        lockAndCall(srn, updateSchemeCacheConnector.upsert(_, answers.json))
    }
  }

  def remove[I <: TypedIdentifier[_]](mode: Mode, srn: Option[String], id: I)
                                     (implicit
                                      ec: ExecutionContext,
                                      hc: HeaderCarrier,
                                      request: DataRequest[AnyContent]
                                     ): Future[JsValue] =
    mode match {
      case NormalMode | CheckMode => subscriptionCacheConnector.remove(request.externalId, id)
      case UpdateMode | CheckUpdateMode => lockAndCall(srn, updateSchemeCacheConnector.remove(_, id))
    }

  def removeAll[I <: TypedIdentifier[_]](mode: Mode, srn: Option[String], id: I)
                                     (implicit
                                      ec: ExecutionContext,
                                      hc: HeaderCarrier,
                                      request: DataRequest[AnyContent]
                                     ): Future[JsValue] =
    mode match {
      case NormalMode | CheckMode => subscriptionCacheConnector.remove(request.externalId, id)
      case UpdateMode | CheckUpdateMode => lockAndCall(srn, updateSchemeCacheConnector.remove(_, id))
    }

  def upsert(mode: Mode, srn: Option[String], value: JsValue)(implicit ec: ExecutionContext, hc: HeaderCarrier,
                                                                  request: DataRequest[AnyContent]): Future[JsValue] =
    mode match {
      case NormalMode | CheckMode => subscriptionCacheConnector.upsert(request.externalId, value)
      case UpdateMode | CheckUpdateMode => lockAndCall(srn, updateSchemeCacheConnector.upsert(_, value))
    }

  def upsert(mode: Mode, srn: Option[String], value: JsValue,
             changeId: TypedIdentifier[Boolean])(implicit ec: ExecutionContext, hc: HeaderCarrier,
                                                              request: DataRequest[AnyContent]): Future[JsValue] =
    mode match {
      case NormalMode | CheckMode => subscriptionCacheConnector.upsert(request.externalId, value)
      case UpdateMode | CheckUpdateMode =>
        val answers = UserAnswers(value)
          .set(changeId)(true).asOpt.getOrElse(UserAnswers(value))
        lockAndCall(srn, updateSchemeCacheConnector.upsert(_, answers.json))
    }

  def lockAndCall(srn: Option[String], f: String => Future[JsValue])(implicit
                                                                     ec: ExecutionContext,
                                                                     hc: HeaderCarrier,
                                                                     request: DataRequest[AnyContent]
  ): Future[JsValue] = srn match {
    case Some(srnId) => lockConnector.lock(request.psaId.id, srnId).flatMap {
          case VarianceLock => f(srnId)
          case _ => Future(Json.obj())
      }

    case _ => Future.failed(throw new MissingSrnNumber)
  }

  def setCompleteFlag(mode: Mode, srn: Option[String], id: TypedIdentifier[Boolean], userAnswers: UserAnswers, value: Boolean)
                     (implicit fmt: Format[Boolean], ec: ExecutionContext, hc: HeaderCarrier, request: DataRequest[AnyContent]): Future[UserAnswers] = {

    userAnswers.set(id)(value).fold(
      invalid => Future.failed(JsResultException(invalid)),
      valid => Future.successful(valid)
    )

    mode match {
      case NormalMode | CheckMode => subscriptionCacheConnector.save(request.externalId, id, value) map UserAnswers
      case UpdateMode | CheckUpdateMode => srn match {
        case Some(srnId) => lockConnector.lock(request.psaId.id, srnId).flatMap {
          case VarianceLock => save(mode, srn, id, value) map UserAnswers
          case _ => Future.successful(request.userAnswers)
        }

        case _ =>
          case class MissingSrnNumber() extends Exception
          Future.failed(throw new MissingSrnNumber)
      }
    }
  }

  def setAddressCompleteFlagAfterAddressYear(mode: Mode, srn: Option[String], id: TypedIdentifier[AddressYears], addressYears: AddressYears, userAnswers: UserAnswers)
                                            (implicit ec: ExecutionContext, hc: HeaderCarrier, request: DataRequest[AnyContent]): Future[UserAnswers] = {

    val addressCompletedId = getAddressId[AddressYears](id)

    addressYears match{
      case OverAYear => addressCompletedId.fold(Future.successful(userAnswers)) { changeId =>
        val ua = userAnswers
          .set(changeId)(true).asOpt.getOrElse(userAnswers)
        upsert(mode, srn, ua.json).map(UserAnswers)
      }
      case UnderAYear => Future.successful(userAnswers)
    }
  }

  def setAddressCompleteFlagAfterPreviousAddress(mode: Mode, srn: Option[String], id: TypedIdentifier[Address], userAnswers: UserAnswers)
                                                (implicit ec: ExecutionContext, hc: HeaderCarrier, request: DataRequest[AnyContent]): Future[UserAnswers] = {

    val addressCompletedId = getAddressId[Address](id)

    addressCompletedId.fold(Future.successful(userAnswers))(id => save(mode, srn, id, true).map(UserAnswers))

  }

  def getAddressId[T](id: TypedIdentifier[T]):  Option[TypedIdentifier[Boolean]] = id match{
    case TruesteeCompanyAddressYearsId(index) => Some(IsTrusteeAddressCompleteId(index))
    case TruesteePartnershipAddressYearsId(index) => Some(IsTrusteeAddressCompleteId(index))
    case TruesteeIndividualAddressYearsId(index) => Some(IsTrusteeAddressCompleteId(index))
    case EstablisherCompanyAddressYearsId(index) => Some(IsEstablisherAddressCompleteId(index))
    case EstablisherPartnershipAddressYearsId(index) => Some(IsEstablisherAddressCompleteId(index))
    case EstablisherIndividualAddressYearsId(index) => Some(IsEstablisherAddressCompleteId(index))
    case PartnerAddressYearsId(establisherIndex, partnerIndex) => Some(IsPartnerAddressCompleteId(establisherIndex, partnerIndex))
    case DirectorAddressYearsId(establisherIndex, directorIndex) => Some(IsDirectorAddressCompleteId(establisherIndex, directorIndex))
    case TruesteeCompanyPreviousAddressId(index) => Some(IsTrusteeAddressCompleteId(index))
    case TruesteePartnershipPreviousAddressId(index) => Some(IsTrusteeAddressCompleteId(index))
    case TruesteeIndividualPreviousAddressId(index) => Some(IsTrusteeAddressCompleteId(index))
    case EstablisherCompanyPreviousAddressId(index) => Some(IsEstablisherAddressCompleteId(index))
    case EstablisherPartnershipPreviousAddressId(index) => Some(IsEstablisherAddressCompleteId(index))
    case EstablisherIndividualPreviousAddressId(index) => Some(IsEstablisherAddressCompleteId(index))
    case PartnerPreviousAddressId(establisherIndex, partnerIndex) => Some(IsPartnerAddressCompleteId(establisherIndex, partnerIndex))
    case DirectorPreviousAddressId(establisherIndex, directorIndex) => Some(IsDirectorAddressCompleteId(establisherIndex, directorIndex))
    case _ => None
  }
}

@Singleton
class UserAnswersServiceEstablishersAndTrusteesImpl @Inject()(override val subscriptionCacheConnector: SubscriptionCacheConnector,
                                       override val updateSchemeCacheConnector: UpdateSchemeCacheConnector,
                                       override val lockConnector: PensionSchemeVarianceLockConnector,
                                       override val appConfig: FrontendAppConfig
                                      ) extends UserAnswersService {

  override def save[A, I <: TypedIdentifier[A]](mode: Mode, srn: Option[String], id: I, value: A)
                                               (implicit fmt: Format[A], ec: ExecutionContext, hc: HeaderCarrier,
                                                request: DataRequest[AnyContent]): Future[JsValue] =
    save(mode, srn, id, value, EstablishersOrTrusteesChangedId)

  override def upsert(mode: Mode, srn: Option[String], value: JsValue)(implicit ec: ExecutionContext, hc: HeaderCarrier,
                                                                       request: DataRequest[AnyContent]): Future[JsValue] =
    upsert(mode, srn, value, EstablishersOrTrusteesChangedId)
}

@Singleton
class UserAnswersServiceInsuranceImpl @Inject()(override val subscriptionCacheConnector: SubscriptionCacheConnector,
                                                override val updateSchemeCacheConnector: UpdateSchemeCacheConnector,
                                                override val lockConnector: PensionSchemeVarianceLockConnector,
                                                override val appConfig: FrontendAppConfig
                                               ) extends UserAnswersService {
  override def save[A, I <: TypedIdentifier[A]](mode: Mode, srn: Option[String], id: I, value: A)
                                               (implicit fmt: Format[A], ec: ExecutionContext, hc: HeaderCarrier,
                                                request: DataRequest[AnyContent]): Future[JsValue] =
    save(mode, srn, id, value, InsuranceDetailsChangedId)

  override def upsert(mode: Mode, srn: Option[String], value: JsValue)(implicit ec: ExecutionContext, hc: HeaderCarrier,
                                                                       request: DataRequest[AnyContent]): Future[JsValue] =
    upsert(mode, srn, value, InsuranceDetailsChangedId)
}

@Singleton
class UserAnswersServiceImpl @Inject()(override val subscriptionCacheConnector: SubscriptionCacheConnector,
                                                override val updateSchemeCacheConnector: UpdateSchemeCacheConnector,
                                                override val lockConnector: PensionSchemeVarianceLockConnector,
                                                override val appConfig: FrontendAppConfig
                                               ) extends UserAnswersService
