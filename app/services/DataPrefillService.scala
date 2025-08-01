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

import identifiers.register.establishers.company.director.{DirectorDOBId, DirectorEnterNINOId, DirectorNameId}
import identifiers.register.establishers.{EstablisherKindId, EstablishersId}
import identifiers.register.trustees.individual.{TrusteeDOBId, TrusteeEnterNINOId, TrusteeNameId}
import identifiers.register.trustees.{IsTrusteeNewId, TrusteeKindId, TrusteesId}
import models.ReferenceValue
import models.person.PersonName
import models.prefill.IndividualDetails
import models.register.establishers.EstablisherKind
import models.register.trustees.TrusteeKind
import play.api.Logging
import play.api.libs.functional.syntax.*
import play.api.libs.json.*
import play.api.libs.json.JsValue.jsValueToJsLookup
import play.api.libs.json.Reads.JsObjectReducer
import services.DataPrefillService.DirectorIdentifier
import utils.{DataCleanUp, Enumerable, UserAnswers}

import java.time.LocalDate
import javax.inject.Inject
import scala.collection
import scala.collection.Set
import scala.language.postfixOps

class DataPrefillService @Inject() extends Enumerable.Implicits with Logging {

  def copySelectedDirectorsToTrustees(ua: UserAnswers, seqIndexes: Seq[DirectorIdentifier]): UserAnswers = {
    val jsArrayWithAppendedTrustees: JsArray =
      seqIndexes.foldLeft(JsArray()) { case (acc, di) =>
        (ua.json \ EstablishersId.toString \ di.establisherIndex \ "director" \ di.directorIndex).as[JsObject].transform(copyDirectorToTrustee) match {
          case JsSuccess(value, _) =>
            acc.append(value)
          case JsError(errors) =>
            logger.error(s"copySelectedDirectorsToTrustees failed, establisherIndex ${di.establisherIndex} directorIndex ${di.directorIndex}:\n $errors")
            acc
        }
    }

    val trusteeTransformer: Reads[JsObject] =
      (__ \ TrusteesId.toString).json.update(__.read[JsArray].map {
        case existingTrustees@JsArray(_) =>
          JsArray(filterTrusteeIndividuals(existingTrustees)) ++ jsArrayWithAppendedTrustees
      }
    )

    transformUa(ua, trusteeTransformer)
  }

  def copyAllTrusteesToDirectors(ua: UserAnswers, seqIndexes: Seq[Int], establisherIndex: Int): UserAnswers = {
    val completeNotDeletedTrusteeIndexes: Seq[Int] =
      allIndividualTrustees(ua).zipWithIndex.flatMap { case (trustee, index) =>
        if (!trustee.isDeleted && trustee.isComplete) Some(index) else None
      }

    val seqTrustees: Seq[JsObject] =
      (ua.json \ TrusteesId.toString).validate[JsArray].asOpt match {
        case Some(jsArray) =>

          val trusteeIndividualsArr: collection.IndexedSeq[JsValue] =
            filterTrusteeIndividuals(jsArray)
              
          val completeNotDeletedTrustees: Seq[JsValue] =
            completeNotDeletedTrusteeIndexes.map(trusteeIndividualsArr(_))

          seqIndexes.map { index =>
            completeNotDeletedTrustees(index).transform(copyTrusteeToDirector) match {
              case JsSuccess(value, _) =>
                value
              case JsError(errors) =>
                logger.error(s"copyTrusteeToDirector failed, index $index:\n $errors")
                throw JsResultException(errors)
            }
          }
        case _ =>
          Nil
      }

    val seqDirectors: collection.Seq[JsValue] =
      (ua.json \ EstablishersId.toString \ establisherIndex \ "director").validate[JsArray].asOpt match {
        case Some(directors) => directors.value
        case _ => Nil
      }

    val establisherTransformer: Reads[JsObject] =
      (__ \ EstablishersId.toString).json.update(__.read[JsArray].map { establishers =>
        JsArray(
          establishers.value.updated(
            establisherIndex,
            establishers(establisherIndex).as[JsObject] ++ Json.obj("director" -> (seqDirectors ++ seqTrustees))
          )
        )
      })

    transformUa(ua, establisherTransformer)
  }

  private def transformUa(ua: UserAnswers, transformer: Reads[JsObject]): UserAnswers =
    ua.json.transform(transformer) match {
      case JsSuccess(value, _) =>
        UserAnswers(value)
      case _ =>
        ua
    }

  private def copyDirectorToTrustee: Reads[JsObject] = {
    (__ \ "trusteeDetails" \ "firstName").json.copyFrom((__ \ "directorDetails" \ "firstName").json.pick) and
      (__ \ "trusteeDetails" \ "lastName").json.copyFrom((__ \ "directorDetails" \ "lastName").json.pick) and
      (__ \ "trusteeKind").json.put(JsString("individual")) and
      (__ \ "trusteeContactDetails" \ "phoneNumber").json.copyFrom((__ \ "directorContactDetails" \ "phoneNumber").json.pick) and
      (__ \ "trusteeContactDetails" \ "emailAddress").json.copyFrom((__ \ "directorContactDetails" \ "emailAddress").json.pick) and
      (__ \ "trusteeAddressId").json.copyFrom((__ \ "directorAddressId").json.pick) and
      (__ \ "trusteeAddressYears").json.copyFrom((__ \ "companyDirectorAddressYears").json.pick) and
      ((__ \ "trusteePreviousAddress").json.copyFrom((__ \ "previousAddress").json.pick) orElse __.json.put(Json.obj())) and
      (__ \ "hasNino").read[Boolean].flatMap {
        case true =>
          (__ \ "hasNino").json.copyFrom((__ \ "hasNino").json.pick) and
            (__ \ "trusteeNino").json.copyFrom((__ \ "directorNino").json.pick) reduce
        case false =>
          (__ \ "hasNino").json.copyFrom((__ \ "hasNino").json.pick) and
            (__ \ "noNinoReason").json.copyFrom((__ \ "noNinoReason").json.pick) reduce
      } and
      commonReads reduce
  }

  private def copyTrusteeToDirector: Reads[JsObject] = {
    (__ \ "directorDetails" \ "firstName").json.copyFrom((__ \ "trusteeDetails" \ "firstName").json.pick) and
      (__ \ "directorDetails" \ "lastName").json.copyFrom((__ \ "trusteeDetails" \ "lastName").json.pick) and
      (__ \ "directorContactDetails" \ "phoneNumber").json.copyFrom((__ \ "trusteeContactDetails" \ "phoneNumber").json.pick) and
      (__ \ "directorContactDetails" \ "emailAddress").json.copyFrom((__ \ "trusteeContactDetails" \ "emailAddress").json.pick) and
      (__ \ "directorAddressId").json.copyFrom((__ \ "trusteeAddressId").json.pick) and
      (__ \ "companyDirectorAddressYears").json.copyFrom((__ \ "trusteeAddressYears").json.pick) and
      ((__ \ "previousAddress").json.copyFrom((__ \ "trusteePreviousAddress").json.pick) orElse __.json.put(Json.obj())) and
      (__ \ "hasNino").read[Boolean].flatMap {
        case true =>
          (__ \ "hasNino").json.copyFrom((__ \ "hasNino").json.pick) and
            (__ \ "directorNino").json.copyFrom((__ \ "trusteeNino").json.pick) reduce
        case false =>
          (__ \ "hasNino").json.copyFrom((__ \ "hasNino").json.pick) and
            (__ \ "noNinoReason").json.copyFrom((__ \ "noNinoReason").json.pick) reduce
      } and
      commonReads reduce
  }

  private def commonReads: Reads[JsObject] = {
    (__ \ "dateOfBirth").json.copyFrom((__ \ "dateOfBirth").json.pick) and
      (__ \ "hasUtr").read[Boolean].flatMap {
        case true =>
          (__ \ "hasUtr").json.copyFrom((__ \ "hasUtr").json.pick) and
            (__ \ "utr").json.copyFrom((__ \ "utr").json.pick) reduce
        case false =>
          (__ \ "hasUtr").json.copyFrom((__ \ "hasUtr").json.pick) and
            (__ \ "noUtrReason").json.copyFrom((__ \ "noUtrReason").json.pick) reduce
      } reduce
  }

  def getListOfDirectorsToBeCopied(implicit ua: UserAnswers): Seq[IndividualDetails] = {
    val filteredDirectorsSeq = allDirectors.filter(dir => !dir.isDeleted && dir.isComplete)
    filteredDirectorsSeq.filterNot { director =>
      val allNonDeletedTrustees = allIndividualTrustees.filter(!_.isDeleted)

      director.nino.map(ninoVal =>
        allNonDeletedTrustees.exists(_.nino.contains(ninoVal)))
        .getOrElse(allNonDeletedTrustees.exists { trustee =>

          (trustee.dob, director.dob) match {
            case (Some(trusteeDob), Some(dirDob)) => trusteeDob.isEqual(dirDob) && trustee.fullName == director.fullName
            case _ => false
          }
        })
    }
  }

  def getListOfTrusteesToBeCopied(establisherIndex: Int)(implicit ua: UserAnswers): Seq[IndividualDetails] = {
    val filteredTrusteesSeq: Seq[IndividualDetails] =
      allIndividualTrustees.filter(indiv => !indiv.isDeleted && indiv.isComplete)
    
    val allDirectorsNotDeleted: collection.Seq[IndividualDetails] =
      (ua.json \ EstablishersId.toString \ establisherIndex \ "director").validate[JsArray].asOpt match {
        case Some(jsArray) =>
          jsArray
            .value
            .zipWithIndex
            .flatMap { case (jsValue, directorIndex) =>
              jsValue
                .validate[IndividualDetails](readsDirector(establisherIndex, directorIndex))
                .asOpt
            }.filter(indiv => !indiv.isDeleted && indiv.isComplete)
        case _ =>
          Nil
      }

    filteredTrusteesSeq.filterNot { trustee =>
      trustee
        .nino
        .map(ninoVal => allDirectorsNotDeleted.exists(_.nino.contains(ninoVal)))
        .getOrElse(
          allDirectorsNotDeleted.exists { director =>
            (trustee.dob, director.dob) match {
              case (Some(trusteeDob), Some(dirDob)) =>
                dirDob.isEqual(trusteeDob) && trustee.fullName == director.fullName
              case _ =>
                false
            }
          }
        )
    }
  }

  private def allDirectors(implicit ua: UserAnswers): Seq[IndividualDetails] = {
    ua.json.validate[Seq[Option[Seq[IndividualDetails]]]](readsDirectors) match {
      case JsSuccess(directorsWithEstablishers, _) if directorsWithEstablishers.nonEmpty =>
        directorsWithEstablishers.flatMap(_.toSeq).flatten
      case _ =>
        Nil
    }
  }

  private def readsDirectors(implicit ua: UserAnswers): Reads[Seq[Option[Seq[IndividualDetails]]]] =
    new Reads[Seq[Option[Seq[IndividualDetails]]]] {
      private def readsAllDirectors(estIndex: Int)(implicit ua: UserAnswers): Reads[Seq[IndividualDetails]] = {
        case JsArray(directors) =>
          val jsResults: collection.IndexedSeq[JsResult[IndividualDetails]] =
            directors.zipWithIndex.map {
              case (jsValue, dirIndex) => readsDirector(estIndex, dirIndex).reads(jsValue)
            }
          asJsResultSeq(jsResults.toSeq)
        case _ =>
          JsSuccess(Nil)
      }

      override def reads(json: JsValue): JsResult[Seq[Option[Seq[IndividualDetails]]]] =
        ua.json \ EstablishersId.toString match {
          case JsDefined(JsArray(establishers)) =>
            val jsResults = establishers.zipWithIndex.map {
              case (jsValue, index) =>
                val establisherKind = (jsValue \ EstablisherKindId.toString).validate[String].asOpt

                val readsForEstablisherKind = establisherKind match {

                  case Some(EstablisherKind.Company.toString) =>
                    (JsPath \ "director").readNullable(readsAllDirectors(index))
                  case _ =>
                    Reads.pure[Option[Seq[IndividualDetails]]](None)
                }
                readsForEstablisherKind.reads(jsValue)
            }
            asJsResultSeq(jsResults.toSeq)
          case _ => JsSuccess(Nil)
        }
    }

  private def readsDirector(estIndex: Int, directorIndex: Int)(implicit ua: UserAnswers): Reads[IndividualDetails] =
    (
      (JsPath \ DirectorNameId.toString).read[PersonName] and
      (JsPath \ DirectorDOBId.toString).readNullable[LocalDate] and
      (JsPath \ DirectorEnterNINOId.toString).readNullable[ReferenceValue]
    )(
      (directorName, dob, ninoReferenceValue) =>
        IndividualDetails(
          directorName.firstName,
          directorName.lastName,
          directorName.isDeleted,
          ninoReferenceValue.map(_.value),
          dob,
          directorIndex,
          ua.isDirectorComplete(estIndex, directorIndex),
          Some(estIndex)
        )
    )

  private def allIndividualTrustees(implicit ua: UserAnswers): Seq[IndividualDetails] =
    ua.json.validate[Seq[Option[IndividualDetails]]](readsTrustees) match {
      case JsSuccess(trustees, _) =>
        trustees.flatten
      case JsError(_) =>
        Nil
    }

  private def readsIndividualTrustee(index: Int)(implicit ua: UserAnswers): Reads[Option[IndividualDetails]] =
    (
      (JsPath \ TrusteeNameId.toString).read[PersonName] and
      (JsPath \ TrusteeDOBId.toString).readNullable[LocalDate] and
      (JsPath \ TrusteeEnterNINOId.toString).readNullable[ReferenceValue]
    )(
      (trusteeName, dob, ninoReferenceValue) =>
        Some(IndividualDetails(
          firstName  = trusteeName.firstName,
          lastName   = trusteeName.lastName,
          isDeleted  = trusteeName.isDeleted,
          nino       = ninoReferenceValue.map(_.value),
          dob        = dob,
          index      = index,
          isComplete = UserAnswers(
            (ua.json \ TrusteesId.toString).validate[JsArray].asOpt match {
              case Some(jsArray) =>
                Json.obj(TrusteesId.toString -> JsArray(filterTrusteeIndividuals(jsArray)))
              case _ =>
                ua.json
            }
          ).isTrusteeIndividualComplete(index)
        ))
    )

  private def readsTrustees(implicit ua: UserAnswers): Reads[Seq[Option[IndividualDetails]]] =
    (json: JsValue) =>
      (ua.json \ TrusteesId.toString).validate[JsArray].asOpt match {
        case Some(jsArray) =>
          asJsResultSeq(
            filterTrusteeIndividuals(jsArray).zipWithIndex.map {
              case (jsValue, index) =>
                readsIndividualTrustee(index).reads(jsValue)
            }.toSeq
          )
        case _ =>
          JsSuccess(Nil)
      }

  private def asJsResultSeq[A](jsResults: Seq[JsResult[A]]): JsResult[Seq[A]] = {
    JsSuccess(jsResults.collect {
      case JsSuccess(i, _) => i
    })
  }
  
  def filterTrusteeIndividuals(jsArray: JsArray): collection.IndexedSeq[JsValue] =
    DataCleanUp.filterNotEmptyObjectsAndSubsetKeys(
      jsArray = jsArray,
      keySet  = Set(IsTrusteeNewId.toString, TrusteeKindId.toString),
      defName = "filterTrusteeIndividuals"
    )
    .filter(_
      .\(TrusteeKindId.toString)
      .validate[JsString]
      .asOpt
      .contains(JsString(TrusteeKind.Individual.toString))
    )
}

object DataPrefillService {
  case class DirectorIdentifier(establisherIndex: Int, directorIndex: Int)
}
