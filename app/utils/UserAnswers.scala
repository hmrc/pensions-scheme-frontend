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

package utils

import identifiers._
import identifiers.register.establishers.company.director.{DirectorDetailsId, DirectorNameId, IsNewDirectorId}
import identifiers.register.establishers.company.{CompanyDetailsId => EstablisherCompanyDetailsId}
import identifiers.register.establishers.individual.EstablisherDetailsId
import identifiers.register.establishers.partnership.PartnershipDetailsId
import identifiers.register.establishers.partnership.partner.{IsNewPartnerId, IsPartnerCompleteId, PartnerDetailsId}
import identifiers.register.establishers.{EstablisherKindId, EstablishersId, IsEstablisherCompleteId, IsEstablisherNewId}
import identifiers.register.trustees.company.CompanyDetailsId
import identifiers.register.trustees.individual.{TrusteeDetailsId, TrusteeNameId}
import identifiers.register.trustees.partnership.{PartnershipDetailsId => TrusteePartnershipDetailsId}
import identifiers.register.trustees.{IsTrusteeNewId, TrusteeKindId, TrusteesId}
import models.address.Address
import models.person.{PersonDetails, PersonName}
import models.register._
import models.register.establishers.EstablisherKind
import models.{CompanyDetails, Mode, PartnershipDetails, UpdateMode}
import play.api.Logger
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc.Result
import play.api.mvc.Results._
import utils.datacompletion.{DataCompletionEstablishers, DataCompletionTrustees}

import scala.annotation.tailrec
import scala.concurrent.Future
import scala.language.implicitConversions

//scalastyle:off number.of.methods
final case class UserAnswers(json: JsValue = Json.obj()) extends Enumerable.Implicits with DataCompletionEstablishers with DataCompletionTrustees {

  def prettyPrint: String = Json.prettyPrint(json)

  def get[A](id: TypedIdentifier[A])(implicit rds: Reads[A]): Option[A] = {
    get[A](id.path)
  }

  def get[A](path: JsPath)(implicit rds: Reads[A]): Option[A] = {
    JsLens.fromPath(path).get(json)
      .flatMap(Json.fromJson[A]).asOpt
  }

  def getAll[A](path: JsPath)(implicit rds: Reads[A]): Option[Seq[A]] = {
    (JsLens.fromPath(path) andThen JsLens.atAllIndices)
      .getAll(json)
      .flatMap(a => traverse(a.map(s => Json.fromJson[A](s))))
      .asOpt
  }

  def getAllRecursive[A](path: JsPath)(implicit rds: Reads[A]): Option[Seq[A]] = {
    JsLens.fromPath(path)
      .getAll(json)
      .flatMap(a => traverse(a.map(Json.fromJson[A]))).asOpt
  }

  def set(path: JsPath)(jsValue: JsValue): JsResult[UserAnswers] = {
    JsLens.fromPath(path)
      .set(jsValue, json).map(UserAnswers(_))
  }

  def set[I <: TypedIdentifier.PathDependent](id: I)(value: id.Data)(implicit writes: Writes[id.Data]): JsResult[UserAnswers] = {
    val jsValue = Json.toJson(value)
    val oldValue = json
    val jsResultSetValue = JsLens.fromPath(id.path).set(jsValue, json)
    jsResultSetValue.flatMap { newValue =>
      if (oldValue == newValue) {
        JsSuccess(UserAnswers(newValue))
      } else {
        id.cleanup(Some(value), UserAnswers(newValue))
      }
    }
  }

  def remove[I <: TypedIdentifier.PathDependent](id: I): JsResult[UserAnswers] = {

    JsLens.fromPath(id.path)
      .remove(json)
      .flatMap(json => id.cleanup(None, UserAnswers(json)))
  }

  def removeAllOf[I <: TypedIdentifier.PathDependent](ids: List[I]): JsResult[UserAnswers] = {

    @tailrec
    def removeRec[II <: TypedIdentifier.PathDependent](localIds: List[II], result: JsResult[UserAnswers]): JsResult[UserAnswers] = {
      result match {
        case JsSuccess(_, path) =>
          localIds match {
            case Nil => result
            case id :: tail => removeRec(tail, result.flatMap(_.remove(id)))
          }
        case failure => failure
      }
    }

    removeRec(ids, JsSuccess(this))
  }


  private def traverse[A](seq: Seq[JsResult[A]]): JsResult[Seq[A]] = {
    seq match {
      case s if s.forall(_.isSuccess) =>
        JsSuccess(seq.foldLeft(Seq.empty[A]) {
          case (m, JsSuccess(n, _)) =>
            m :+ n
          case (m, _) =>
            m
        })
      case s =>
        s.collect {
          case e@JsError(_) =>
            e
        }.reduceLeft(JsError.merge)
    }
  }

  def readEntities[T](entities: Seq[JsValue], reads: Int => Reads[T]): JsResult[Seq[T]] = {
    @tailrec
    def recur(result: JsResult[Seq[T]], entities: Seq[(JsValue, Int)]): JsResult[Seq[T]] = {
      result match {
        case JsSuccess(results, _) =>
          entities match {
            case Seq(h, t@_*) => reads(h._2).reads(h._1) match {
              case JsSuccess(item, _) => recur(JsSuccess(results :+ item), t)
              case error@JsError(_) => error
            }
            case _ => result
          }
        case error: JsError => error
      }
    }

    recur(JsSuccess(Nil), entities.zipWithIndex)
  }

  private def notDeleted: Reads[JsBoolean] = __.read(JsBoolean(false))

  //scalastyle:off method.length
  def readEstablishers(isHnSEnabled: Boolean, mode: Mode): Reads[Seq[Establisher[_]]] = new Reads[Seq[Establisher[_]]] {

    private def noOfRecords : Int = json.validate((__ \ 'establishers).readNullable(__.read(
      Reads.seq((__ \ 'establisherKind).read[String].flatMap {
        case "individual" => (__ \ 'establisherDetails \ "isDeleted").json.pick[JsBoolean] orElse notDeleted
        case "company" => (__ \ 'companyDetails \ "isDeleted").json.pick[JsBoolean] orElse notDeleted
        case "partnership" => (__ \ 'partnershipDetails \ "isDeleted").json.pick[JsBoolean] orElse notDeleted
      }).map(x=> x.count(deleted => deleted == JsBoolean(false)))))) match {
      case JsSuccess(Some(ele), _) => ele
      case _ => 0
    }


    private def readsIndividual(index: Int): Reads[Establisher[_]] = (
      (JsPath \ EstablisherDetailsId.toString).read[PersonDetails] and
        (JsPath \ IsEstablisherCompleteId.toString).readNullable[Boolean] and
        (JsPath \ IsEstablisherNewId.toString).readNullable[Boolean]
      ) ((details, isComplete, isNew) =>
      EstablisherIndividualEntity(
        EstablisherDetailsId(index), details.fullName, details.isDeleted,
        isComplete.getOrElse(false), isNew.fold(false)(identity), noOfRecords)
    )

    private def readsCompany(index: Int): Reads[Establisher[_]] = (
      (JsPath \ EstablisherCompanyDetailsId.toString).read[CompanyDetails] and
        (JsPath \ IsEstablisherNewId.toString).readNullable[Boolean]
      ) ((details, isNew) =>
      EstablisherCompanyEntity(EstablisherCompanyDetailsId(index),
        details.companyName, details.isDeleted, isEstablisherCompanyAndDirectorsComplete(index, mode, isHnSEnabled), isNew.fold(false)(identity), noOfRecords)
    )

    private def readsPartnership(index: Int): Reads[Establisher[_]] = (
      (JsPath \ PartnershipDetailsId.toString).read[PartnershipDetails] and
        (JsPath \ IsEstablisherCompleteId.toString).readNullable[Boolean] and
        (JsPath \ IsEstablisherNewId.toString).readNullable[Boolean]
      ) ((details, isComplete, isNew) =>
      EstablisherPartnershipEntity(PartnershipDetailsId(index),
        details.name, details.isDeleted, isComplete.getOrElse(false), isNew.fold(false)(identity), noOfRecords)
    )

    private def readsSkeleton(index: Int): Reads[Establisher[_]] = new Reads[Establisher[_]] {
      override def reads(json: JsValue): JsResult[Establisher[_]] = {
        (json \ EstablisherKindId.toString)
          .toOption.map(_ => JsSuccess(EstablisherSkeletonEntity(EstablisherKindId(index))))
          .getOrElse(JsError(s"Establisher does not have element establisherKind: index=$index"))
      }
    }

    override def reads(json: JsValue): JsResult[Seq[Establisher[_]]] = {
      json \ EstablishersId.toString match {
        case JsDefined(JsArray(establishers)) =>
          readEntities(
            establishers,
            index => readsIndividual(index)
              orElse readsCompany(index)
              orElse readsPartnership(index)
              orElse readsSkeleton(index)
          )
        case _ => JsSuccess(Nil)
      }
    }
  }

  def allEstablishers(isHnSEnabled: Boolean, mode: Mode): Seq[Establisher[_]] = {
    json.validate[Seq[Establisher[_]]](readEstablishers(isHnSEnabled, mode)) match {
      case JsSuccess(establishers, _) =>
        establishers
      case JsError(errors) =>
        Logger.warn(s"Invalid json while reading all the establishers for addEstablisher: $errors")
        Nil
    }
  }

  def allEstablishersAfterDelete(isHnSEnabled: Boolean, mode: Mode): Seq[Establisher[_]] = {
    allEstablishers(isHnSEnabled, mode).filterNot(_.isDeleted)
  }

  def allDirectorsOld(establisherIndex: Int): Seq[DirectorEntityNonHnS] = {

    getAllRecursive[PersonDetails](DirectorDetailsId.collectionPath(establisherIndex)).map {
      details =>
        for ((director, directorIndex) <- details.zipWithIndex) yield {
          val isComplete = isDirectorCompleteNonHnS(establisherIndex, directorIndex)
          val isNew = get(IsNewDirectorId(establisherIndex, directorIndex)).getOrElse(false)
          DirectorEntityNonHnS(
            DirectorDetailsId(establisherIndex, directorIndex),
            director.fullName,
            director.isDeleted,
            isComplete,
            isNew,
            details.count(!_.isDeleted)
          )
        }
    }.getOrElse(Seq.empty)
  }

  def allDirectorsHnS(establisherIndex: Int): Seq[DirectorEntity] = {

    getAllRecursive[PersonName](DirectorNameId.collectionPath(establisherIndex)).map {
      details =>
        for ((director, directorIndex) <- details.zipWithIndex) yield {
          val isComplete = isDirectorCompleteHnS(establisherIndex, directorIndex)
          val isNew = get(IsNewDirectorId(establisherIndex, directorIndex)).getOrElse(false)
          DirectorEntity(
            DirectorNameId(establisherIndex, directorIndex),
            director.fullName,
            director.isDeleted,
            isComplete,
            isNew,
            details.count(!_.isDeleted)
          )
        }
    }.getOrElse(Seq.empty)
  }

  def allDirectors(establisherIndex: Int, isHnSEnabled: Boolean): Seq[Director[_]] = {
    if(isHnSEnabled)
      allDirectorsHnS(establisherIndex)
    else
      allDirectorsOld(establisherIndex)
  }

  def allDirectorsAfterDelete(establisherIndex: Int, isHnSEnabled: Boolean): Seq[Director[_]] = {
      allDirectors(establisherIndex, isHnSEnabled).filterNot(_.isDeleted)
  }

  def allPartners(establisherIndex: Int): Seq[PartnerEntity] = {
    getAllRecursive[PersonDetails](PartnerDetailsId.collectionPath(establisherIndex)).map {
      details =>
        for ((partner, partnerIndex) <- details.zipWithIndex) yield {
          val isComplete = get(IsPartnerCompleteId(establisherIndex, partnerIndex)).getOrElse(false)
          val isNew = get(IsNewPartnerId(establisherIndex, partnerIndex)).getOrElse(false)
          PartnerEntity(
            PartnerDetailsId(establisherIndex, partnerIndex),
            partner.fullName,
            partner.isDeleted,
            isComplete,
            isNew,
            details.count(!_.isDeleted)
          )
        }
    }.getOrElse(Seq.empty)
  }

  def allPartnersAfterDelete(establisherIndex: Int): Seq[PartnerEntity] = {
    allPartners(establisherIndex).filterNot(_.isDeleted)
  }

  private def schemeType : Option[String] = json.transform((__ \ 'schemeType \ 'name).json.pick[JsString]) match {
    case JsSuccess(scheme, _) => Some(scheme.value)
    case JsError(errors) => None
  }

  //scalastyle:off method.length
  def readTrustees(isHnSEnabled: Boolean): Reads[Seq[Trustee[_]]] = new Reads[Seq[Trustee[_]]] {

    private def noOfRecords : Int = json.validate((__ \ 'trustees).readNullable(__.read(
      Reads.seq((__ \ 'trusteeKind).read[String].flatMap {
        case "individual" => (__ \ 'trusteeDetails \ "isDeleted").json.pick[JsBoolean] orElse notDeleted
        case "company" => (__ \ 'companyDetails \ "isDeleted").json.pick[JsBoolean] orElse notDeleted
        case "partnership" => (__ \ 'partnershipDetails \ "isDeleted").json.pick[JsBoolean] orElse notDeleted
      }).map(x=> x.count(deleted => deleted == JsBoolean(false)))))) match {
      case JsSuccess(Some(ele), _) => ele
      case _ => 0
    }

    // Change this method
    private def readsIndividualNonHns(index: Int): Reads[Trustee[_]] = (
      (JsPath \ TrusteeDetailsId.toString).read[PersonDetails] and
        (JsPath \ IsTrusteeNewId.toString).readNullable[Boolean]
      ) ((details, isNew) =>
      TrusteeIndividualEntityNonHns(
        TrusteeDetailsId(index), details.fullName, details.isDeleted,
        isTrusteeIndividualComplete(isHnSEnabled, index), isNew.fold(false)(identity), noOfRecords, schemeType)
    )

    private def readsIndividual(index: Int): Reads[Trustee[_]] =
      (
        (JsPath \ TrusteeNameId.toString).read[PersonName] and
          (JsPath \ IsTrusteeNewId.toString).readNullable[Boolean]
        ) ((details, isNew) =>
        TrusteeIndividualEntity(
          TrusteeNameId(index), details.fullName, details.isDeleted,
          isTrusteeIndividualComplete(isHnSEnabled, index), isNew.fold(false)(identity), noOfRecords, schemeType)
      )

    private def readsCompany(index: Int): Reads[Trustee[_]] = (
      (JsPath \ CompanyDetailsId.toString).read[CompanyDetails] and
        (JsPath \ IsTrusteeNewId.toString).readNullable[Boolean]
      ) ((details, isNew) => {
      TrusteeCompanyEntity(CompanyDetailsId(index), details.companyName, details.isDeleted,
        isTrusteeCompanyComplete(index, isHnSEnabled), isNew.fold(false)(identity), noOfRecords, schemeType)
    }
    )
    private def readsPartnership(index: Int): Reads[Trustee[_]] = (
      (JsPath \ TrusteePartnershipDetailsId.toString).read[PartnershipDetails] and
        (JsPath \ IsTrusteeNewId.toString).readNullable[Boolean]
      ) ((details, isNew) => TrusteePartnershipEntity(
      TrusteePartnershipDetailsId(index), details.name, details.isDeleted,
      isTrusteePartnershipComplete(index, isHnSEnabled), isNew.fold(false)(identity), noOfRecords, schemeType)
    )
    private def readsSkeleton(index: Int): Reads[Trustee[_]] = new Reads[Trustee[_]] {
      override def reads(json: JsValue): JsResult[Trustee[_]] = {
        (json \ TrusteeKindId.toString)
          .toOption.map(_ => JsSuccess(TrusteeSkeletonEntity(TrusteeKindId(index))))
          .getOrElse(JsError(s"Trustee does not have element trusteeKind: index=$index"))
      }
    }
    override def reads(json: JsValue): JsResult[Seq[Trustee[_]]] = {
      json \ TrusteesId.toString match {
        case JsDefined(JsArray(trustees)) =>
          readEntities(
            trustees,
            index =>
              (if(isHnSEnabled) readsIndividual(index) else readsIndividualNonHns(index))
                orElse readsCompany(index)
                orElse readsPartnership(index)
                orElse readsSkeleton(index)
          )
        case _ => JsSuccess(Nil)
      }
    }
  }

  def allTrustees(isHnSEnabled: Boolean): Seq[Trustee[_]] = {

    json.validate[Seq[Trustee[_]]](readTrustees(isHnSEnabled)) match {
      case JsSuccess(trustees, _) =>
        trustees
      case JsError(errors) =>
        Logger.warn(s"Invalid json while reading all the trustees for addTrustees: $errors")
        Nil
    }
  }

  def allTrusteesAfterDelete(isHnSEnabled: Boolean): Seq[Trustee[_]] = {
    allTrustees(isHnSEnabled).filterNot(_.isDeleted)
  }

  def establishersCount: Int = {
    (json \ EstablishersId.toString).validate[JsArray] match {
      case JsSuccess(establisherArray, _) => establisherArray.value.size
      case _ => 0
    }
  }

  def trusteesCount: Int = {
    (json \ TrusteesId.toString).validate[JsArray] match {
      case JsSuccess(trusteesArray, _) => trusteesArray.value.size
      case _ => 0
    }
  }

  def hasCompanies(isHnSEnabled: Boolean, mode: Mode): Boolean = {
    allEstablishersAfterDelete(isHnSEnabled, mode).exists {
      _.id match {
        case EstablisherCompanyDetailsId(_) | PartnershipDetailsId(_) => true
        case _ => false
      }
    }
  }

  def upsert[I <: TypedIdentifier.PathDependent](id: I)(value: id.Data)
                                                (fn: UserAnswers => Future[Result])
                                                (implicit writes: Writes[id.Data]): Future[Result] = {
    this
      .set(id)(value)
      .fold(
        errors => {
          Logger.error("Unable to set user answer", JsResultException(errors))
          Future.successful(InternalServerError)
        },
        userAnswers => fn(userAnswers)
      )
  }

  def isUserAnswerUpdated: Boolean =
    List(
      get[Boolean](InsuranceDetailsChangedId),
      get[Boolean](EstablishersOrTrusteesChangedId)
    ).flatten.contains(true)

  def addressAnswer(address: Address)(implicit countryOptions: CountryOptions): Seq[String] = {
    val country = countryOptions.options.find(_.value == address.country).map(_.label).getOrElse(address.country)
    Seq(
      Some(address.addressLine1),
      Some(address.addressLine2),
      address.addressLine3,
      address.addressLine4,
      address.postcode,
      Some(country)
    ).flatten
  }

  def isAllTrusteesCompleted(isHnSEnabled: Boolean): Boolean = {
    val isSingleOrMaster = schemeType.fold(false)(scheme => Seq("single", "master").exists(_.equals(scheme)))

    if(isSingleOrMaster)
      allTrusteesAfterDelete(isHnSEnabled).nonEmpty && allTrusteesAfterDelete(isHnSEnabled).forall(_.isCompleted)
    else
      allTrusteesAfterDelete(isHnSEnabled).forall(_.isCompleted)

  }

  def isDirectorPartnerCompleted(establisherIndex:Int, isHnSEnabled: Boolean) = get(EstablisherKindId(establisherIndex)) match {
    case Some(EstablisherKind.Company) => allDirectorsAfterDelete(establisherIndex, isHnSEnabled).forall(_.isCompleted)
    case Some(EstablisherKind.Partnership) => allPartnersAfterDelete(establisherIndex).forall(_.isCompleted)
    case _ => true
  }

  def allEstablishersCompleted(isHnSEnabled: Boolean, mode: Mode) =
    !allEstablishersAfterDelete(isHnSEnabled, mode).zipWithIndex.collect { case (item, establisherIndex) =>
      item.isCompleted && isDirectorPartnerCompleted(establisherIndex, isHnSEnabled)
  }.contains(false)

  def isInsuranceCompleted: Boolean = get(BenefitsSecuredByInsuranceId) match {
    case Some(true) => !List(get(InvestmentRegulatedSchemeId), get(OccupationalPensionSchemeId), get(TypeOfBenefitsId),
      get(InsuranceCompanyNameId), get(InsurancePolicyNumberId), get(InsurerConfirmAddressId)).contains(None)
    case Some(false) => true
    case _ => false
  }

  def areVariationChangesCompleted(isHnSEnabled: Boolean = false): Boolean =
    isInsuranceCompleted && isAllTrusteesCompleted(isHnSEnabled) && allEstablishersCompleted(isHnSEnabled, UpdateMode)

}
