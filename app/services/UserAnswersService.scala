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

import config.FrontendAppConfig
import connectors.{PensionSchemeVarianceLockConnector, SchemeDetailsReadOnlyCacheConnector, UpdateSchemeCacheConnector, UserAnswersCacheConnector}
import identifiers.*
import identifiers.register.establishers.company.director.*
import identifiers.register.establishers.company.{CompanyAddressYearsId as EstablisherCompanyAddressYearsId, CompanyPreviousAddressId as EstablisherCompanyPreviousAddressId}
import identifiers.register.establishers.individual.{AddressYearsId as EstablisherIndividualAddressYearsId, PreviousAddressId as EstablisherIndividualPreviousAddressId}
import identifiers.register.establishers.partnership.partner.*
import identifiers.register.establishers.partnership.{PartnershipAddressYearsId as EstablisherPartnershipAddressYearsId, PartnershipPreviousAddressId as EstablisherPartnershipPreviousAddressId}
import identifiers.register.establishers.IsEstablisherNewId
import identifiers.register.trustees.company.{CompanyAddressYearsId as TrusteeCompanyAddressYearsId, CompanyPreviousAddressId as TrusteeCompanyPreviousAddressId}
import identifiers.register.trustees.individual.{TrusteeAddressYearsId as TrusteeIndividualAddressYearsId, TrusteePreviousAddressId as TrusteeIndividualPreviousAddressId}
import identifiers.register.trustees.partnership.{PartnershipAddressYearsId as TrusteePartnershipAddressYearsId, PartnershipPreviousAddressId as TrusteePartnershipPreviousAddressId}
import identifiers.register.trustees.IsTrusteeNewId
import models.*
import models.OptionalSchemeReferenceNumber.toSrn
import models.address.Address
import models.requests.DataRequest
import play.api.Logging
import play.api.libs.json.*
import play.api.mvc.AnyContent
import uk.gov.hmrc.http.HeaderCarrier
import utils.{DataCleanUp, UserAnswers}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait UserAnswersService extends Logging {

  def save[A, I <: TypedIdentifier[A]](mode: Mode, srn: OptionalSchemeReferenceNumber, id: I, value: A)
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

  def save[A, I <: TypedIdentifier[A]](mode: Mode, srn: OptionalSchemeReferenceNumber, id: I, value: A,
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

  def remove[I <: TypedIdentifier[?]](mode: Mode, srn: OptionalSchemeReferenceNumber, id: I)
                                     (implicit
                                      ec: ExecutionContext,
                                      hc: HeaderCarrier,
                                      request: DataRequest[AnyContent]
                                     ): Future[JsValue] =
    mode match {
      case NormalMode | CheckMode =>
        subscriptionCacheConnector.remove(request.externalId, id)
      case UpdateMode | CheckUpdateMode =>
        lockAndCall(srn, updateSchemeCacheConnector.remove(_, id))
    }

  def upsert(mode: Mode, srn: OptionalSchemeReferenceNumber, value: JsValue)
            (implicit ec: ExecutionContext, hc: HeaderCarrier, request: DataRequest[AnyContent]): Future[JsValue] =
    mode match {
      case NormalMode | CheckMode =>
        subscriptionCacheConnector.upsert(request.externalId, value)
      case UpdateMode | CheckUpdateMode =>
        lockAndCall(srn, updateSchemeCacheConnector.upsert(_, value))
    }

  private def lockAndCall(srn: OptionalSchemeReferenceNumber, f: String => Future[JsValue])
                         (implicit ec: ExecutionContext, hc: HeaderCarrier, request: DataRequest[AnyContent]): Future[JsValue] =
    (toSrn(srn), request.psaId) match {
      case (Some(srnId), Some(psaId)) =>
        lockConnector.lock(psaId.id, srnId).flatMap {
          case VarianceLock =>
            viewConnector.removeAll(request.externalId).flatMap(_ => f(srnId))
          case _ =>
            Future(Json.obj())
        }
      case (None, _) =>
        Future.failed(MissingSrnNumber)
      case _ =>
        Future.failed(MissingPsaId)
    }

  def upsert(mode: Mode, srn: OptionalSchemeReferenceNumber, value: JsValue, changeId: TypedIdentifier[Boolean])
            (implicit ec: ExecutionContext, hc: HeaderCarrier, request: DataRequest[AnyContent]): Future[JsValue] =
    mode match {
      case NormalMode | CheckMode =>
        subscriptionCacheConnector.upsert(request.externalId, value)
      case UpdateMode | CheckUpdateMode =>
        val answers = UserAnswers(value).set(changeId)(true).asOpt.getOrElse(UserAnswers(value))
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
  
  def removeEmptyObjectsAndIncompleteEntities(json: JsValue, collectionKey: String, keySet: Set[String], externalId: String)
                                             (implicit ec: ExecutionContext, hc: HeaderCarrier): JsValue =
    (json \ collectionKey).validate[JsArray].asOpt match {
      case Some(jsArray) =>

        val filteredCollection: collection.IndexedSeq[JsValue] =
          DataCleanUp.filterNotEmptyObjectsAndSubsetKeys(
            jsArray = jsArray,
            keySet  = keySet,
            defName = "removeEmptyObjectsAndIncompleteEntities"
          )

        val reads: Reads[JsObject] =
          (__ \ collectionKey)
            .json
            .update(__.read[JsArray].map(_ => JsArray(filteredCollection)))

        json.transform(reads) match {
          case JsSuccess(value, _) =>
            val removed = jsArray.value.size - filteredCollection.size

            if (removed > 0) logger.warn(s"$collectionKey filtering succeeded. $removed elements removed")
            value
          case JsError(errors) =>
            logger.warn(s"$collectionKey filtering failed: $errors")
            json
        }
      case _ =>
        json
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
class UserAnswersServiceEstablishersAndTrusteesImpl @Inject()(override val subscriptionCacheConnector: UserAnswersCacheConnector,
                                                              override val updateSchemeCacheConnector: UpdateSchemeCacheConnector,
                                                              override val lockConnector: PensionSchemeVarianceLockConnector,
                                                              override val viewConnector: SchemeDetailsReadOnlyCacheConnector,
                                                              override val appConfig: FrontendAppConfig
                                                             ) extends UserAnswersService {

  override def save[A, I <: TypedIdentifier[A]](mode: Mode, srn: OptionalSchemeReferenceNumber, id: I, value: A)
                                               (implicit fmt: Format[A], ec: ExecutionContext, hc: HeaderCarrier, request: DataRequest[AnyContent]): Future[JsValue] =
    save(mode, srn, id, value, EstablishersOrTrusteesChangedId)

  override def upsert(mode: Mode, srn: OptionalSchemeReferenceNumber, value: JsValue)
                     (implicit ec: ExecutionContext, hc: HeaderCarrier, request: DataRequest[AnyContent]): Future[JsValue] =
    upsert(mode, srn, value, EstablishersOrTrusteesChangedId)
}

@Singleton
class UserAnswersServiceInsuranceImpl @Inject()(override val subscriptionCacheConnector: UserAnswersCacheConnector,
                                                override val updateSchemeCacheConnector: UpdateSchemeCacheConnector,
                                                override val lockConnector: PensionSchemeVarianceLockConnector,
                                                override val viewConnector: SchemeDetailsReadOnlyCacheConnector,
                                                override val appConfig: FrontendAppConfig
                                               ) extends UserAnswersService {
  override def save[A, I <: TypedIdentifier[A]](mode: Mode, srn: OptionalSchemeReferenceNumber, id: I, value: A)
                                               (implicit fmt: Format[A], ec: ExecutionContext, hc: HeaderCarrier, request: DataRequest[AnyContent]): Future[JsValue] =
    save(mode, srn, id, value, InsuranceDetailsChangedId)

  override def upsert(mode: Mode, srn: OptionalSchemeReferenceNumber, value: JsValue)
                     (implicit ec: ExecutionContext, hc: HeaderCarrier, request: DataRequest[AnyContent]): Future[JsValue] =
    upsert(mode, srn, value, InsuranceDetailsChangedId)
}

@Singleton
class UserAnswersServiceImpl @Inject()(override val subscriptionCacheConnector: UserAnswersCacheConnector,
                                       override val updateSchemeCacheConnector: UpdateSchemeCacheConnector,
                                       override val lockConnector: PensionSchemeVarianceLockConnector,
                                       override val viewConnector: SchemeDetailsReadOnlyCacheConnector,
                                       override val appConfig: FrontendAppConfig
                                      ) extends UserAnswersService
