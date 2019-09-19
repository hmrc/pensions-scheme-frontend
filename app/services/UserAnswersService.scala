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

import config.{FeatureSwitchManagementService, FrontendAppConfig}
import connectors.{PensionSchemeVarianceLockConnector, SchemeDetailsReadOnlyCacheConnector, UpdateSchemeCacheConnector, UserAnswersCacheConnector}
import identifiers._
import identifiers.register.establishers.company.director._
import identifiers.register.establishers.company.{CompanyAddressYearsId => EstablisherCompanyAddressYearsId, CompanyPreviousAddressId => EstablisherCompanyPreviousAddressId}
import identifiers.register.establishers.individual.{AddressYearsId => EstablisherIndividualAddressYearsId, PreviousAddressId => EstablisherIndividualPreviousAddressId}
import identifiers.register.establishers.partnership.partner._
import identifiers.register.establishers.partnership.{IsPartnershipCompleteId => IsEstablisherPartnershipCompleteId, PartnershipAddressYearsId => EstablisherPartnershipAddressYearsId, PartnershipPreviousAddressId => EstablisherPartnershipPreviousAddressId}
import identifiers.register.establishers.{IsEstablisherCompleteId, IsEstablisherNewId}
import identifiers.register.trustees.IsTrusteeNewId
import identifiers.register.trustees.company.{CompanyAddressYearsId => TruesteeCompanyAddressYearsId, CompanyPreviousAddressId => TruesteeCompanyPreviousAddressId}
import identifiers.register.trustees.individual.{TrusteeAddressYearsId => TruesteeIndividualAddressYearsId, TrusteePreviousAddressId => TruesteeIndividualPreviousAddressId}
import identifiers.register.trustees.partnership.{PartnershipAddressYearsId => TruesteePartnershipAddressYearsId, PartnershipPreviousAddressId => TruesteePartnershipPreviousAddressId}
import javax.inject.{Inject, Singleton}
import models.AddressYears.OverAYear
import models.address.Address
import models.requests.DataRequest
import models.{Mode, _}
import play.api.libs.json._
import play.api.mvc.AnyContent
import uk.gov.hmrc.http.HeaderCarrier
import utils.{Toggles, UserAnswers}

import scala.concurrent.{ExecutionContext, Future}

trait UserAnswersService {

  protected def subscriptionCacheConnector: UserAnswersCacheConnector

  protected def updateSchemeCacheConnector: UpdateSchemeCacheConnector

  protected def lockConnector: PensionSchemeVarianceLockConnector

  protected def viewConnector: SchemeDetailsReadOnlyCacheConnector

  protected def appConfig: FrontendAppConfig

  protected def fs: FeatureSwitchManagementService

  case object MissingSrnNumber extends Exception

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
      case NormalMode | CheckMode => {
        subscriptionCacheConnector.save(request.externalId, id, value)
      }
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
      case VarianceLock => viewConnector.removeAll(request.externalId).flatMap(_ => f(srnId))
      case _ => Future(Json.obj())
    }

    case _ => Future.failed(MissingSrnNumber)
  }

  def setCompleteFlag(mode: Mode, srn: Option[String], id: TypedIdentifier[Boolean], userAnswers: UserAnswers, value: Boolean)
                     (implicit fmt: Format[Boolean], ec: ExecutionContext, hc: HeaderCarrier, request: DataRequest[AnyContent]): Future[UserAnswers] = {
    save(mode, srn, id, value) map UserAnswers
  }

  def setAddressCompleteFlagAfterAddressYear(mode: Mode, srn: Option[String], id: TypedIdentifier[AddressYears],
                                             addressYears: AddressYears, userAnswers: UserAnswers)
                                            (implicit ec: ExecutionContext, hc: HeaderCarrier, request: DataRequest[AnyContent]): Future[UserAnswers] = {

    if (mode == UpdateMode || mode == CheckUpdateMode) {
      getIsNewId(id).map { newId =>
        (userAnswers.get(newId), addressYears) match {
          case (Some(false) | None, OverAYear) =>
            val addressCompletedId = getCompleteId[AddressYears](id)
            upsert(mode, srn, setCompleteForAddress(addressCompletedId, userAnswers, mode, srn).json).map(UserAnswers)
          case _ =>
            Future.successful(userAnswers)
        }
      }.getOrElse(Future.successful(userAnswers))
    } else {
      Future.successful(userAnswers)
    }
  }

  def setAddressCompleteFlagAfterPreviousAddress(mode: Mode, srn: Option[String], id: TypedIdentifier[Address], userAnswers: UserAnswers)
                                                (implicit ec: ExecutionContext, hc: HeaderCarrier, request: DataRequest[AnyContent]): Future[UserAnswers] = {
    if (mode == UpdateMode || mode == CheckUpdateMode) {
      getIsNewId(id).map { newId =>
        userAnswers.get(newId) match {
          case Some(false) | None =>
            val addressCompletedId = getCompleteId[Address](id)
            upsert(mode, srn, setCompleteForAddress(addressCompletedId, userAnswers, mode, srn).json).map(UserAnswers)
          case _ =>
            Future.successful(userAnswers)
        }
      }.getOrElse(Future.successful(userAnswers))
    } else {
      Future.successful(userAnswers)
    }
  }

  private[services] def setCompleteForAddress(addressCompletedId: Option[TypedIdentifier[Boolean]], answers: UserAnswers,
                                              mode: Mode, srn: Option[String])(implicit ec: ExecutionContext, hc: HeaderCarrier, request: DataRequest[AnyContent]): UserAnswers = {
    addressCompletedId.fold(answers) { changeId =>
      val ua = answers.set(changeId)(true).asOpt.getOrElse(answers)
      changeId match {
        case IsPartnerCompleteId(partnershipIndex, _) =>
          setIsEstablisherComplete(
            ua,
            ua.allPartnersAfterDelete(partnershipIndex).forall(_.isCompleted),
            IsEstablisherPartnershipCompleteId(partnershipIndex),
            partnershipIndex
          )
        case IsEstablisherPartnershipCompleteId(partnershipIndex) if ua.allPartnersAfterDelete(partnershipIndex).forall(_.isCompleted) =>
          ua.set(IsEstablisherCompleteId(partnershipIndex))(true).asOpt.getOrElse(ua)
        case _ => ua
      }
    }
  }

  private def setIsEstablisherComplete(userAnswers: UserAnswers, allDirectorOrPartnerComplete: Boolean,
                                       completeId: TypedIdentifier[Boolean], index: Int) = {
    val partnershipCompleted = userAnswers.get(completeId).contains(true)
    if (allDirectorOrPartnerComplete && partnershipCompleted) {
      userAnswers.set(IsEstablisherCompleteId(index))(true).asOpt.getOrElse(userAnswers)
    } else {
      userAnswers
    }
  }

  def setExistingAddress(mode: Mode, id: TypedIdentifier[Address], userAnswers: UserAnswers)
                        (implicit ec: ExecutionContext, hc: HeaderCarrier, request: DataRequest[AnyContent]): UserAnswers = {
    val existingAddressPathNode = List(KeyPathNode("existingCurrentAddress"))
    val existingAddressPath = JsPath(id.path.path.init ++ existingAddressPathNode)
    userAnswers.get(id) match {
      case Some(address) if mode == CheckUpdateMode =>
        userAnswers.set(existingAddressPath)(Json.toJson(address)).asOpt.getOrElse(userAnswers)
      case _ =>
        userAnswers
    }
  }

  def getCompleteId[T](id: TypedIdentifier[T]): Option[TypedIdentifier[Boolean]] = id match {
    case EstablisherPartnershipAddressYearsId(index) => Some(IsEstablisherPartnershipCompleteId(index))
    case EstablisherIndividualAddressYearsId(index) => Some(IsEstablisherCompleteId(index))
    case PartnerAddressYearsId(establisherIndex, partnerIndex) => Some(IsPartnerCompleteId(establisherIndex, partnerIndex))
    case EstablisherPartnershipPreviousAddressId(index) => Some(IsEstablisherPartnershipCompleteId(index))
    case EstablisherIndividualPreviousAddressId(index) => Some(IsEstablisherCompleteId(index))
    case PartnerPreviousAddressId(establisherIndex, partnerIndex) => Some(IsPartnerCompleteId(establisherIndex, partnerIndex))
    case InsurerConfirmAddressId => Some(IsAboutBenefitsAndInsuranceCompleteId)
    case _ => None
  }

  def getIsNewId[T](id: TypedIdentifier[T]): Option[TypedIdentifier[Boolean]] = id match {
    case TruesteeCompanyAddressYearsId(index) => Some(IsTrusteeNewId(index))
    case TruesteePartnershipAddressYearsId(index) => Some(IsTrusteeNewId(index))
    case TruesteeIndividualAddressYearsId(index) => Some(IsTrusteeNewId(index))
    case EstablisherCompanyAddressYearsId(index) => Some(IsEstablisherNewId(index))
    case EstablisherPartnershipAddressYearsId(index) => Some(IsEstablisherNewId(index))
    case EstablisherIndividualAddressYearsId(index) => Some(IsEstablisherNewId(index))
    case PartnerAddressYearsId(establisherIndex, partnerIndex) => Some(IsNewPartnerId(establisherIndex, partnerIndex))
    case DirectorAddressYearsId(establisherIndex, directorIndex) => Some(IsNewDirectorId(establisherIndex, directorIndex))
    case TruesteeCompanyPreviousAddressId(index) => Some(IsTrusteeNewId(index))
    case TruesteePartnershipPreviousAddressId(index) => Some(IsTrusteeNewId(index))
    case TruesteeIndividualPreviousAddressId(index) => Some(IsTrusteeNewId(index))
    case EstablisherCompanyPreviousAddressId(index) => Some(IsEstablisherNewId(index))
    case EstablisherPartnershipPreviousAddressId(index) => Some(IsEstablisherNewId(index))
    case EstablisherIndividualPreviousAddressId(index) => Some(IsEstablisherNewId(index))
    case PartnerPreviousAddressId(establisherIndex, partnerIndex) => Some(IsNewPartnerId(establisherIndex, partnerIndex))
    case DirectorPreviousAddressId(establisherIndex, directorIndex) => Some(IsNewDirectorId(establisherIndex, directorIndex))
    case _ => None
  }
}

@Singleton
class UserAnswersServiceEstablishersAndTrusteesImpl @Inject()(override val subscriptionCacheConnector: UserAnswersCacheConnector,
                                                              override val updateSchemeCacheConnector: UpdateSchemeCacheConnector,
                                                              override val lockConnector: PensionSchemeVarianceLockConnector,
                                                              override val viewConnector: SchemeDetailsReadOnlyCacheConnector,
                                                              override val appConfig: FrontendAppConfig,
                                                              override val fs: FeatureSwitchManagementService
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
class UserAnswersServiceInsuranceImpl @Inject()(override val subscriptionCacheConnector: UserAnswersCacheConnector,
                                                override val updateSchemeCacheConnector: UpdateSchemeCacheConnector,
                                                override val lockConnector: PensionSchemeVarianceLockConnector,
                                                override val viewConnector: SchemeDetailsReadOnlyCacheConnector,
                                                override val appConfig: FrontendAppConfig,
                                                override val fs: FeatureSwitchManagementService
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
class UserAnswersServiceImpl @Inject()(override val subscriptionCacheConnector: UserAnswersCacheConnector,
                                       override val updateSchemeCacheConnector: UpdateSchemeCacheConnector,
                                       override val lockConnector: PensionSchemeVarianceLockConnector,
                                       override val viewConnector: SchemeDetailsReadOnlyCacheConnector,
                                       override val appConfig: FrontendAppConfig,
                                       override val fs: FeatureSwitchManagementService
                                      ) extends UserAnswersService
