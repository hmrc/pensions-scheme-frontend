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

package services

import config.FrontendAppConfig
import connectors.{PensionSchemeVarianceLockConnector, SchemeDetailsReadOnlyCacheConnector,
  UpdateSchemeCacheConnector, UserAnswersCacheConnector}
import identifiers._
import identifiers.register.establishers.IsEstablisherNewId
import identifiers.register.establishers.company.director._
import identifiers.register.establishers.company.{CompanyAddressYearsId => EstablisherCompanyAddressYearsId,
  CompanyPreviousAddressId => EstablisherCompanyPreviousAddressId}
import identifiers.register.establishers.individual.{AddressYearsId => EstablisherIndividualAddressYearsId,
  PreviousAddressId => EstablisherIndividualPreviousAddressId}
import identifiers.register.establishers.partnership.partner._
import identifiers.register.establishers.partnership.{PartnershipAddressYearsId =>
EstablisherPartnershipAddressYearsId, PartnershipPreviousAddressId => EstablisherPartnershipPreviousAddressId}
import identifiers.register.trustees.IsTrusteeNewId
import identifiers.register.trustees.company.{CompanyAddressYearsId => TrusteeCompanyAddressYearsId,
  CompanyPreviousAddressId => TrusteeCompanyPreviousAddressId}
import identifiers.register.trustees.individual.{TrusteeAddressYearsId => TrusteeIndividualAddressYearsId,
  TrusteePreviousAddressId => TrusteeIndividualPreviousAddressId}
import identifiers.register.trustees.partnership.{PartnershipAddressYearsId => TrusteePartnershipAddressYearsId,
  PartnershipPreviousAddressId => TrusteePartnershipPreviousAddressId}
import javax.inject.{Inject, Singleton}
import models.address.Address
import models.requests.DataRequest
import models.{Mode, _}
import play.api.libs.json._
import play.api.mvc.AnyContent
import uk.gov.hmrc.http.HeaderCarrier
import utils.UserAnswers

import scala.concurrent.{ExecutionContext, Future}

trait UserAnswersService {

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
      case NormalMode | CheckMode =>
        subscriptionCacheConnector.save(request.externalId, id, value)

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

  def upsert(mode: Mode, srn: Option[String], value: JsValue)(implicit ec: ExecutionContext, hc: HeaderCarrier,
                                                              request: DataRequest[AnyContent]): Future[JsValue] =
    mode match {
      case NormalMode | CheckMode => subscriptionCacheConnector.upsert(request.externalId, value)
      case UpdateMode | CheckUpdateMode => lockAndCall(srn, updateSchemeCacheConnector.upsert(_, value))
    }

  private def lockAndCall(srn: Option[String], f: String => Future[JsValue])(implicit
                                                                     ec: ExecutionContext,
                                                                     hc: HeaderCarrier,
                                                                     request: DataRequest[AnyContent]
  ): Future[JsValue] = (srn, request.psaId) match {
    case (Some(srnId), Some(psaId)) => lockConnector.lock(psaId.id, srnId).flatMap {
      case VarianceLock => viewConnector.removeAll(request.externalId).flatMap(_ => f(srnId))
      case _ => Future(Json.obj())
    }

    case (None, _) => Future.failed(MissingSrnNumber)
    case _ => Future.failed(MissingPsaId)
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

  def setExistingAddress(mode: Mode, id: TypedIdentifier[Address], userAnswers: UserAnswers): UserAnswers = {
    val existingAddressPathNode = List(KeyPathNode("existingCurrentAddress"))
    val existingAddressPath = JsPath(id.path.path.init ++ existingAddressPathNode)
    userAnswers.get(id) match {
      case Some(address) if mode == CheckUpdateMode =>
        userAnswers.set(existingAddressPath)(Json.toJson(address)).asOpt.getOrElse(userAnswers)
      case _ =>
        userAnswers
    }
  }

  //scalastyle:off cyclomatic.complexity
  def getIsNewId[T](id: TypedIdentifier[T]): Option[TypedIdentifier[Boolean]] = id match {
    case TrusteeCompanyAddressYearsId(index) => Some(IsTrusteeNewId(index))
    case TrusteePartnershipAddressYearsId(index) => Some(IsTrusteeNewId(index))
    case TrusteeIndividualAddressYearsId(index) => Some(IsTrusteeNewId(index))
    case EstablisherCompanyAddressYearsId(index) => Some(IsEstablisherNewId(index))
    case EstablisherPartnershipAddressYearsId(index) => Some(IsEstablisherNewId(index))
    case EstablisherIndividualAddressYearsId(index) => Some(IsEstablisherNewId(index))
    case PartnerAddressYearsId(establisherIndex, partnerIndex) => Some(IsNewPartnerId(establisherIndex, partnerIndex))
    case DirectorAddressYearsId(establisherIndex, directorIndex) => Some(IsNewDirectorId(establisherIndex,
      directorIndex))
    case TrusteeCompanyPreviousAddressId(index) => Some(IsTrusteeNewId(index))
    case TrusteePartnershipPreviousAddressId(index) => Some(IsTrusteeNewId(index))
    case TrusteeIndividualPreviousAddressId(index) => Some(IsTrusteeNewId(index))
    case EstablisherCompanyPreviousAddressId(index) => Some(IsEstablisherNewId(index))
    case EstablisherPartnershipPreviousAddressId(index) => Some(IsEstablisherNewId(index))
    case EstablisherIndividualPreviousAddressId(index) => Some(IsEstablisherNewId(index))
    case PartnerPreviousAddressId(establisherIndex, partnerIndex) => Some(IsNewPartnerId(establisherIndex,
      partnerIndex))
    case DirectorPreviousAddressId(establisherIndex, directorIndex) => Some(IsNewDirectorId(establisherIndex,
      directorIndex))
    case _ => None
  }

  protected def subscriptionCacheConnector: UserAnswersCacheConnector

  protected def updateSchemeCacheConnector: UpdateSchemeCacheConnector

  protected def lockConnector: PensionSchemeVarianceLockConnector

  protected def viewConnector: SchemeDetailsReadOnlyCacheConnector

  protected def appConfig: FrontendAppConfig

  case object MissingSrnNumber extends Exception
  case object MissingPsaId extends Exception("Psa ID missing in request")
}

@Singleton
class UserAnswersServiceEstablishersAndTrusteesImpl @Inject()(override val
                                                              subscriptionCacheConnector: UserAnswersCacheConnector,
                                                              override val
                                                              updateSchemeCacheConnector: UpdateSchemeCacheConnector,
                                                              override val
                                                              lockConnector: PensionSchemeVarianceLockConnector,
                                                              override val
                                                              viewConnector: SchemeDetailsReadOnlyCacheConnector,
                                                              override val appConfig: FrontendAppConfig
                                                             ) extends UserAnswersService {

  override def save[A, I <: TypedIdentifier[A]](mode: Mode, srn: Option[String], id: I, value: A)
                                               (implicit fmt: Format[A], ec: ExecutionContext, hc: HeaderCarrier,
                                                request: DataRequest[AnyContent]): Future[JsValue] =
    save(mode, srn, id, value, EstablishersOrTrusteesChangedId)

  override def upsert(mode: Mode, srn: Option[String], value: JsValue)(implicit ec: ExecutionContext, hc: HeaderCarrier,
                                                                       request: DataRequest[AnyContent])
  : Future[JsValue] =
    upsert(mode, srn, value, EstablishersOrTrusteesChangedId)
}

@Singleton
class UserAnswersServiceInsuranceImpl @Inject()(override val subscriptionCacheConnector: UserAnswersCacheConnector,
                                                override val updateSchemeCacheConnector: UpdateSchemeCacheConnector,
                                                override val lockConnector: PensionSchemeVarianceLockConnector,
                                                override val viewConnector: SchemeDetailsReadOnlyCacheConnector,
                                                override val appConfig: FrontendAppConfig
                                               ) extends UserAnswersService {
  override def save[A, I <: TypedIdentifier[A]](mode: Mode, srn: Option[String], id: I, value: A)
                                               (implicit fmt: Format[A], ec: ExecutionContext, hc: HeaderCarrier,
                                                request: DataRequest[AnyContent]): Future[JsValue] =
    save(mode, srn, id, value, InsuranceDetailsChangedId)

  override def upsert(mode: Mode, srn: Option[String], value: JsValue)(implicit ec: ExecutionContext, hc: HeaderCarrier,
                                                                       request: DataRequest[AnyContent])
  : Future[JsValue] =
    upsert(mode, srn, value, InsuranceDetailsChangedId)
}

@Singleton
class UserAnswersServiceImpl @Inject()(override val subscriptionCacheConnector: UserAnswersCacheConnector,
                                       override val updateSchemeCacheConnector: UpdateSchemeCacheConnector,
                                       override val lockConnector: PensionSchemeVarianceLockConnector,
                                       override val viewConnector: SchemeDetailsReadOnlyCacheConnector,
                                       override val appConfig: FrontendAppConfig
                                      ) extends UserAnswersService
