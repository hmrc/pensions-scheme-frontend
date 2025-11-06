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

package utils

import identifiers.*
import identifiers.register.establishers.company.CompanyDetailsId as EstablisherCompanyDetailsId
import identifiers.register.establishers.company.director.{DirectorNameId, IsNewDirectorId}
import identifiers.register.establishers.individual.EstablisherNameId
import identifiers.register.establishers.partnership.PartnershipDetailsId
import identifiers.register.establishers.partnership.partner.{IsNewPartnerId, PartnerNameId}
import identifiers.register.establishers.{EstablisherKindId, EstablishersId, IsEstablisherNewId}
import identifiers.register.trustees.company.CompanyDetailsId
import identifiers.register.trustees.individual.TrusteeNameId
import identifiers.register.trustees.partnership.PartnershipDetailsId as TrusteePartnershipDetailsId
import identifiers.register.trustees.{IsTrusteeNewId, TrusteeKindId, TrusteesId}
import models.address.Address
import models.person.PersonName
import models.register.*
import models.register.establishers.EstablisherKind
import models.register.trustees.TrusteeKind
import models.{CompanyDetails, Mode, PartnershipDetails, UpdateMode}
import play.api.Logging
import play.api.libs.functional.syntax.*
import play.api.libs.json.*
import play.api.libs.json.Reads.*
import play.api.mvc.Result
import play.api.mvc.Results.*
import utils.datacompletion.{DataCompletionEstablishers, DataCompletionTrustees}

import scala.annotation.tailrec
import scala.collection.Set
import scala.concurrent.Future

//scalastyle:off number.of.methods
final case class UserAnswers(json: JsValue = Json.obj())
  extends Enumerable.Implicits
    with DataCompletionEstablishers
    with DataCompletionTrustees
    with Logging {

  def getAll[A](path: JsPath)(implicit rds: Reads[A]): Option[Seq[A]] = {
    JsLens.fromPath(path).andThen(JsLens.atAllIndices)
      .getAll(json)
      .flatMap(a => traverse(a.map(s => Json.fromJson[A](s))))
      .asOpt
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

  def getAllRecursive[A](path: JsPath)(implicit rds: Reads[A]): Option[Seq[A]] = {
    JsLens.fromPath(path)
      .getAll(json)
      .flatMap(a => traverse(a.map(Json.fromJson[A]))).asOpt
  }

  def set(path: JsPath)(jsValue: JsValue): JsResult[UserAnswers] = {
    JsLens.fromPath(path)
      .set(jsValue, json).map(UserAnswers.apply)
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

  def remove[I <: TypedIdentifier.PathDependent](id: I): JsResult[UserAnswers] =
    JsLens
      .fromPath(id.path)
      .remove(json)
      .flatMap(json => id.cleanup(None, UserAnswers(json)))

  def allDirectors(establisherIndex: Int): Seq[DirectorEntity] =
    getAllRecursive[PersonName](DirectorNameId.collectionPath(establisherIndex)).map {
      details =>
        for ((director, directorIndex) <- details.zipWithIndex) yield {
          val isComplete = isDirectorComplete(establisherIndex, directorIndex)

          val isNew = get(IsNewDirectorId(establisherIndex, directorIndex)).getOrElse(false)

          DirectorEntity(
            id          = DirectorNameId(establisherIndex, directorIndex),
            name        = director.fullName,
            isDeleted   = director.isDeleted,
            isCompleted = isComplete,
            isNewEntity = isNew,
            noOfRecords = details.count(!_.isDeleted)
          )
        }
    }.getOrElse(Seq.empty)

  def allDirectorsAfterDelete(establisherIndex: Int): Seq[DirectorEntity] =
    allDirectors(establisherIndex)
      .filterNot(directorEntity => directorEntity.isDeleted)

  def allPartners(establisherIndex: Int): Seq[Partner[?]] =
    getAllRecursive[PersonName](PartnerNameId.collectionPath(establisherIndex)).map {
      details =>
        for ((partner, partnerIndex) <- details.zipWithIndex) yield {
          val isComplete = isPartnerComplete(establisherIndex, partnerIndex)
          val isNew = get(IsNewPartnerId(establisherIndex, partnerIndex)).getOrElse(false)

          PartnerEntity(
            id          = PartnerNameId(establisherIndex, partnerIndex),
            name        = partner.fullName,
            isDeleted   = partner.isDeleted,
            isCompleted = isComplete,
            isNewEntity = isNew,
            noOfRecords = details.count(!_.isDeleted)
          )
        }
    }.getOrElse(Seq.empty)

  def allPartnersAfterDelete(establisherIndex: Int): Seq[Partner[?]] =
    allPartners(establisherIndex).filterNot(_.isDeleted)

  //scalastyle:off method.length
  private def readTrustees: Reads[Seq[Trustee[?]]] =
    new Reads[Seq[Trustee[?]]] {

      private def noOfRecords: Int =
        json
          .validate((__ \ "trustees")
            .readNullable(__.read(Reads.seq(
              (__ \ TrusteeKindId.toString).read[String].flatMap {
                case TrusteeKind.Individual.toString =>
                  (__ \ "trusteeDetails" \ "isDeleted").json.pick[JsBoolean] orElse notDeleted
                case TrusteeKind.Company.toString =>
                  (__ \ "companyDetails" \ "isDeleted").json.pick[JsBoolean] orElse notDeleted
                case TrusteeKind.Partnership.toString =>
                  (__ \ "partnershipDetails" \ "isDeleted").json.pick[JsBoolean] orElse notDeleted
              }
            ).map(_.count(deleted => !deleted.value))))) match {
          case JsSuccess(Some(ele), _) =>
            ele
          case _ =>
            0
        }

      private def readsIndividual(index: Int): Reads[Trustee[?]] =
        (
          (JsPath \ TrusteeNameId.toString).read[PersonName] and
          (JsPath \ IsTrusteeNewId.toString).readNullable[Boolean]
        )(
          (personName, isNew) =>
            TrusteeIndividualEntity(
              id          = TrusteeNameId(index),
              name        = personName.fullName,
              isDeleted   = personName.isDeleted,
              isCompleted = isTrusteeIndividualComplete(index),
              isNewEntity = isNew.getOrElse(false),
              noOfRecords = noOfRecords,
              schemeType  = schemeType
            )
        )

      private def readsCompany(index: Int): Reads[Trustee[?]] =
        (
          (JsPath \ CompanyDetailsId.toString).read[CompanyDetails] and
          (JsPath \ IsTrusteeNewId.toString).readNullable[Boolean]
        )(
          (companyDetails, isNew) =>
            TrusteeCompanyEntity(
              id          = CompanyDetailsId(index),
              name        = companyDetails.companyName,
              isDeleted   = companyDetails.isDeleted,
              isCompleted = isTrusteeCompanyComplete(index),
              isNewEntity = isNew.getOrElse(false),
              noOfRecords = noOfRecords,
              schemeType  = schemeType
            )
        )

      private def readsPartnership(index: Int): Reads[Trustee[?]] =
        (
          (JsPath \ TrusteePartnershipDetailsId.toString).read[PartnershipDetails] and
          (JsPath \ IsTrusteeNewId.toString).readNullable[Boolean]
        )(
          (partnershipDetails, isNew) =>
            TrusteePartnershipEntity(
              id          = TrusteePartnershipDetailsId(index),
              name        = partnershipDetails.name,
              isDeleted   = partnershipDetails.isDeleted,
              isCompleted = isTrusteePartnershipComplete(index),
              isNewEntity = isNew.getOrElse(false),
              noOfRecords = noOfRecords,
              schemeType  = schemeType
            )
        )

      private def readsSkeleton(index: Int): Reads[Trustee[?]] = (json: JsValue) => {
        (json \ TrusteeKindId.toString)
          .toOption.map(_ => JsSuccess(TrusteeSkeletonEntity(TrusteeKindId(index))))
          .getOrElse(JsError(s"Trustee does not have element trusteeKind: index=$index"))
      }

      override def reads(json: JsValue): JsResult[Seq[Trustee[?]]] =
        (json \ TrusteesId.toString).validate[JsArray].asOpt match {
          case Some(trustees) =>

            val jsResults =
              DataCleanUp.filterNotEmptyObjectsAndSubsetKeys(
                  jsArray = trustees, 
                  keySet  = Set(TrusteeKindId.toString, IsTrusteeNewId.toString),
                  defName = "UserAnswers.readTrustees"
                )
                .zipWithIndex
                .map { case (jsValue, index) =>

                  val trusteeKind = (jsValue \ TrusteeKindId.toString).validate[String].asOpt

                  val readsForTrusteeKind = trusteeKind match {
                    case Some(TrusteeKind.Individual.toString) =>
                      readsIndividual(index)
                    case Some(TrusteeKind.Company.toString) =>
                      readsCompany(index)
                    case Some(TrusteeKind.Partnership.toString) =>
                      readsPartnership(index)
                    case _ =>
                      readsSkeleton(index)
                  }

                  readsForTrusteeKind.reads(jsValue)
                }

            asJsResultSeq(jsResults.toSeq, "readTrustees")
          case _ =>
            JsSuccess(Nil)
        }
    }

  def allTrustees: Seq[Trustee[?]] =
    json.validate[Seq[Trustee[?]]](readTrustees) match {
      case JsSuccess(trustees, _) =>
        trustees
      case JsError(errors) =>
        logger.warn(s"Invalid json while reading all the trustees for addTrustees: $errors")
        Nil
    }

  def allTrusteesAfterDelete: Seq[Trustee[?]] =
    allTrustees.filterNot(_.isDeleted)

  def establishersCount: Int =
    (json \ EstablishersId.toString).validate[JsArray] match {
      case JsSuccess(establisherArray, _) =>
        establisherArray.value.size
      case _ =>
        0
    }

  def trusteesCount: Int =
    (json \ TrusteesId.toString).validate[JsArray] match {
      case JsSuccess(trusteesArray, _) =>
        trusteesArray.value.size
      case _ =>
        0
    }

  def hasCompanies(mode: Mode): Boolean = {
    allEstablishersAfterDelete(mode).exists {
      _.id match {
        case EstablisherCompanyDetailsId(_) => true
        case _ => false
      }
    }
  }

  def allEstablishersAfterDelete(mode: Mode): Seq[Establisher[?]] =
    allEstablishers(mode).filterNot(_.isDeleted)

  def allEstablishers(mode: Mode): Seq[Establisher[?]] =
    json.validate[Seq[Establisher[?]]](readEstablishers(mode)) match {
      case JsSuccess(establishers, _) =>
        establishers
      case JsError(errors) =>
        logger.warn(s"Invalid json while reading all the establishers for addEstablisher: $errors")
        Nil
    }

  //scalastyle:off method.length
  private def readEstablishers(mode: Mode): Reads[Seq[Establisher[?]]] = new Reads[Seq[Establisher[?]]] {

    private def noOfRecords: Int =
      json
        .validate((__ \ "establishers")
          .readNullable(__.read(Reads.seq(
            (__ \ EstablisherKindId.toString).read[String].flatMap {
              case EstablisherKind.Individual.toString =>
                (__ \ "establisherDetails" \ "isDeleted").json.pick[JsBoolean] orElse notDeleted
              case EstablisherKind.Company.toString =>
                (__ \ "companyDetails" \ "isDeleted").json.pick[JsBoolean] orElse notDeleted
              case EstablisherKind.Partnership.toString =>
                (__ \ "partnershipDetails" \ "isDeleted").json.pick[JsBoolean] orElse notDeleted
            }
          ).map(_.count(deleted => !deleted.value))))) match {
        case JsSuccess(Some(ele), _) =>
          ele
        case _ =>
          0
      }

    private def readsIndividual(index: Int): Reads[Establisher[?]] =
      (
        (JsPath \ EstablisherNameId.toString).read[PersonName] and
        (JsPath \ IsEstablisherNewId.toString).readNullable[Boolean]
      )(
        (personName, isNew) =>
          EstablisherIndividualEntity(
            id          = EstablisherNameId(index),
            name        = personName.fullName,
            isDeleted   = personName.isDeleted,
            isCompleted = isEstablisherIndividualComplete(index),
            isNewEntity = isNew.getOrElse(false),
            noOfRecords = noOfRecords
          )
      )

    private def readsCompany(index: Int): Reads[Establisher[?]] =
      (
        (JsPath \ EstablisherCompanyDetailsId.toString).read[CompanyDetails] and
        (JsPath \ IsEstablisherNewId.toString).readNullable[Boolean]
      )(
        (companyDetails, isNew) =>
          EstablisherCompanyEntity(
            id          = EstablisherCompanyDetailsId(index),
            name        = companyDetails.companyName,
            isDeleted   = companyDetails.isDeleted,
            isCompleted = isEstablisherCompanyAndDirectorsComplete(index, mode),
            isNewEntity = isNew.getOrElse(false),
            noOfRecords = noOfRecords
          )
      )

    private def readsPartnership(index: Int): Reads[Establisher[?]] =
      (
        (JsPath \ PartnershipDetailsId.toString).read[PartnershipDetails] and
        (JsPath \ IsEstablisherNewId.toString).readNullable[Boolean]
      )(
        (partnershipDetails, isNew) =>
          EstablisherPartnershipEntity(
            id          = PartnershipDetailsId(index),
            name        = partnershipDetails.name,
            isDeleted   = partnershipDetails.isDeleted,
            isCompleted = isEstablisherPartnershipAndPartnersComplete(index),
            isNewEntity = isNew.getOrElse(false),
            noOfRecords = noOfRecords
          )
    )

    private def readsSkeleton(index: Int): Reads[Establisher[?]] = (json: JsValue) => {
      (json \ EstablisherKindId.toString)
        .toOption.map(_ => JsSuccess(EstablisherSkeletonEntity(EstablisherKindId(index))))
        .getOrElse(JsError(s"Establisher does not have element establisherKind: index=$index"))
    }

    override def reads(json: JsValue): JsResult[Seq[Establisher[?]]] = {
      (json \ EstablishersId.toString).validate[JsArray].asOpt match {
        case Some(establishers) =>

          val jsResults =
            DataCleanUp.filterNotEmptyObjectsAndSubsetKeys(
                jsArray = establishers,
                keySet  = Set(EstablisherKindId.toString, IsEstablisherNewId.toString),
                defName = "UserAnswers.readEstablishers"
              )
              .zipWithIndex
              .map { case (jsValue, index) =>

                val establisherKind = (jsValue \ EstablisherKindId.toString).validate[String].asOpt

                val readsForEstablisherKind = establisherKind match {
                  case Some(EstablisherKind.Individual.toString) =>
                    readsIndividual(index)
                  case Some(EstablisherKind.Company.toString) =>
                    readsCompany(index)
                  case Some(EstablisherKind.Partnership.toString) =>
                    readsPartnership(index)
                  case _ =>
                    readsSkeleton(index)
                }

                readsForEstablisherKind.reads(jsValue)
              }

          asJsResultSeq(jsResults.toSeq, "readEstablishers")
        case _ =>
          JsSuccess(Nil)
      }
    }
  }

  private def notDeleted: Reads[JsBoolean] = __.read(JsBoolean(false))

  private def asJsResultSeq[A](jsResults: Seq[JsResult[A]], defName: String): JsResult[Seq[A]] = {
    val allErrors = jsResults.collect {
      case JsError(errors) => errors
    }.flatten

    if (allErrors.nonEmpty) { // If any of JSON is invalid then log warning but return the valid ones
      logger.warn(s"Errors in JSON from $defName: $allErrors")
    }

    JsSuccess(jsResults.collect {
      case JsSuccess(i, _) => i
    })
  }

  def upsert[I <: TypedIdentifier.PathDependent](id: I)(value: id.Data)
                                                (fn: UserAnswers => Future[Result])
                                                (implicit writes: Writes[id.Data]): Future[Result] = {
    this
      .set(id)(value)
      .fold(
        errors => {
          logger.error(
            "Unable to set user answer",
            Exception(
              s"path(s) from JSON: ${errors.map(_._1.path.mkString(", "))}" +
                s"\nerror messages from JSON: ${errors.flatMap(_._2.map(_.messages.head))}"
            ))
          Future.successful(InternalServerError)
        },
        userAnswers => fn(userAnswers)
      )
  }

  def set[I <: TypedIdentifier.PathDependent](id: I)
                                             (value: id.Data)
                                             (implicit writes: Writes[id.Data]): JsResult[UserAnswers] = {
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

  def setOrException[I <: TypedIdentifier.PathDependent](id: I)
                                                        (value: id.Data)
                                                        (implicit writes: Writes[id.Data]): UserAnswers =
    set(id)(value) match {
      case JsSuccess(ua, _) =>
        ua
      case JsError(errors) =>
        throw new RuntimeException("Unable to store value in user answers: " + errors)
    }

  def isUserAnswerUpdated: Boolean =
    List(
      get[Boolean](InsuranceDetailsChangedId),
      get[Boolean](EstablishersOrTrusteesChangedId)
    ).flatten.contains(true)

  def get[A](id: TypedIdentifier[A])(implicit rds: Reads[A]): Option[A] = {
    get[A](id.path)
  }

  def get[A](path: JsPath)(implicit rds: Reads[A]): Option[A] = {
    JsLens.fromPath(path).get(json)
      .flatMap(Json.fromJson[A]).asOpt
  }

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

  def isAllTrusteesCompleted: Boolean = {
    val isSingleOrMaster = schemeType.fold(false)(scheme => Seq("single", "master").exists(_.equals(scheme)))

    if (isSingleOrMaster)
      allTrusteesAfterDelete.nonEmpty && allTrusteesAfterDelete.forall(_.isCompleted)
    else
      allTrusteesAfterDelete.forall(_.isCompleted)
  }

  private def isDirectorPartnerCompleted(establisherIndex: Int): Boolean =
    get(EstablisherKindId(establisherIndex)) match {
      case Some(EstablisherKind.Company) =>
        allDirectorsAfterDelete(establisherIndex).forall(_.isCompleted)
      case Some(EstablisherKind.Partnership) =>
        allPartnersAfterDelete(establisherIndex).forall(_.isCompleted)
      case _ =>
        true
    }

  private def allEstablishersCompleted(mode: Mode): Boolean =
    !allEstablishersAfterDelete(mode).zipWithIndex.collect {
      case (item, establisherIndex) =>
        item.isCompleted && isDirectorPartnerCompleted(establisherIndex)
    }.contains(false)

  private def isInsuranceCompleted: Boolean =
    get(BenefitsSecuredByInsuranceId) match {
      case Some(true) =>
        !List(
          get(InvestmentRegulatedSchemeId),
          get(OccupationalPensionSchemeId),
          get(TypeOfBenefitsId),
          get(InsuranceCompanyNameId),
          get(InsurancePolicyNumberId),
          get(InsurerConfirmAddressId)
        ).contains(None)
      case Some(false) =>
        true
      case _ =>
        false
    }

  def areVariationChangesCompleted: Boolean =
    List(
      Some(isInsuranceCompleted),
      Some(isAllTrusteesCompleted),
      isTypeOfBenefitsCompleted,
      Some(allEstablishersCompleted(UpdateMode))
    ).flatten.forall(_ == true)

  protected def schemeType: Option[String] =
    json.transform((__ \ "schemeType" \ "name").json.pick[JsString]) match {
      case JsSuccess(scheme, _) =>
        Some(scheme.value)
      case JsError(_) =>
        None
    }

  def pspEstablishers: Seq[String] =
    json.validate[Seq[String]](readPspEstablishers) match {
      case JsSuccess(establishers, _) =>
        establishers
      case JsError(errors) =>
        logger.warn(s"Invalid json while reading establishers from psp scheme details: $errors")
        Nil
    }

  private def readPspEstablishers: Reads[Seq[String]] = new Reads[Seq[String]] {

    private def readsIndividual: Reads[String] =
      (
        (JsPath \ "establisherDetails" \ "firstName").read[String] and
        (JsPath \ "establisherDetails" \ "lastName").read[String]
      )(
        (firstName, lastName) =>
          s"$firstName $lastName"
      )

    private def readsCompany: Reads[String] = (JsPath \ "companyDetails" \ "companyName").read[String]

    private def readsPartnership: Reads[String] = (JsPath \ "partnershipDetails" \ "name").read[String]

    override def reads(json: JsValue): JsResult[Seq[String]] =
      json \ EstablishersId.toString match {
        case JsDefined(JsArray(establishers)) =>
          val jsResults = establishers.zipWithIndex.map {
            case (jsValue, _) =>

              val establisherKind = (jsValue \ EstablisherKindId.toString).validate[String].asOpt

              val readsForEstablisherKind = establisherKind match {
                case Some(EstablisherKind.Individual.toString) =>
                  readsIndividual
                case Some(EstablisherKind.Company.toString) =>
                  readsCompany
                case Some(EstablisherKind.Partnership.toString) =>
                  readsPartnership
                case _ =>
                  readsBlankString
              }
              readsForEstablisherKind.reads(jsValue)
          }

          asJsResultSeq(jsResults.toSeq, "readPspEstablishers")
        case _ =>
          JsSuccess(Nil)
      }
  }

  def pspTrustees: Seq[String] =
    json.validate[Seq[String]](readPspTrustees) match {
      case JsSuccess(trustees, _) =>
        trustees
      case JsError(errors) =>
        logger.warn(s"Invalid json while reading trustees from psp scheme details: $errors")
        Nil
    }

  private def readPspTrustees: Reads[Seq[String]] = new Reads[Seq[String]] {

    private def readsIndividual: Reads[String] =
      (
        (JsPath \ TrusteeNameId.toString \ "firstName").read[String] and
        (JsPath \ TrusteeNameId.toString \ "lastName").read[String]
      )(
        (firstName, lastName) =>
          s"$firstName $lastName"
      )

    private def readsCompany: Reads[String] =
      (JsPath \ CompanyDetailsId.toString \ "companyName").read[String]

    private def readsPartnership: Reads[String] =
      (JsPath \ TrusteePartnershipDetailsId.toString \ "name").read[String]

    override def reads(json: JsValue): JsResult[Seq[String]] =
      json \ TrusteesId.toString match {
        case JsDefined(JsArray(trustees)) =>
          val jsResults = trustees.zipWithIndex.map {
            case (jsValue, index) =>

              val trusteeKind = (jsValue \ TrusteeKindId.toString).validate[String].asOpt

              val readsForTrusteeKind = trusteeKind match {
                case Some(TrusteeKind.Individual.toString) =>
                  readsIndividual
                case Some(TrusteeKind.Company.toString) =>
                  readsCompany
                case Some(TrusteeKind.Partnership.toString) =>
                  readsPartnership
                case _ =>
                  readsBlankString
              }

              readsForTrusteeKind.reads(jsValue)
          }

          asJsResultSeq(jsResults.toSeq, "readPspTrustees")
        case _ =>
          JsSuccess(Nil)
      }
  }

  private def readsBlankString: Reads[String] = (json: JsValue) => {
    (json \ EstablisherKindId.toString)
      .toOption.map(_ => JsSuccess(""))
      .getOrElse(JsError(s"Entity is neither individual, company or partnership"))
  }

}
