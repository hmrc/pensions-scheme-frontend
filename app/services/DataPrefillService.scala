/*
 * Copyright 2022 HM Revenue & Customs
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
import identifiers.register.trustees.{TrusteeKindId, TrusteesId}
import models.ReferenceValue
import models.person.PersonName
import models.prefill.IndividualDetails
import models.register.establishers.EstablisherKind
import models.register.trustees.TrusteeKind
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import services.DataPrefillService.DirectorIdentifier
import utils.{Enumerable, UserAnswers}

import java.time.LocalDate
import javax.inject.Inject
import scala.language.postfixOps

class DataPrefillService @Inject()() extends Enumerable.Implicits {

  def copySelectedDirectorsToTrustees(ua: UserAnswers, seqIndexes: Seq[DirectorIdentifier]): UserAnswers = {
    val jsArrayWithAppendedTrustees = seqIndexes.foldLeft(JsArray()) { case (acc, di) =>
      (ua.json \ "establishers" \ di.establisherIndex \ "director" \ di.directorIndex).as[JsObject]
        .transform(copyDirectorToTrustee) match {
        case JsSuccess(value, _) => acc.append(value)
        case _ => acc
      }
    }

    val trusteeTransformer = (__ \ "trustees").json.update(
      __.read[JsArray].map {
        case existingTrustees@JsArray(_) => existingTrustees ++ jsArrayWithAppendedTrustees
      }
    )
    transformUa(ua, trusteeTransformer)
  }

  def copyAllTrusteesToDirectors(ua: UserAnswers, seqIndexes: Seq[Int], establisherIndex: Int): UserAnswers = {
    val seqTrustees = (ua.json \ "trustees").validate[JsArray].asOpt match {
      case Some(arr) =>
        seqIndexes.map { index =>
          arr.value(index).transform(copyTrusteeToDirector) match {
            case JsSuccess(value, _) => value
            case JsError(_) =>
              Json.obj()
          }
        }
      case _ => Nil
    }
    val seqDirectors = (ua.json \ "establishers" \ establisherIndex \ "director").validate[JsArray].asOpt match {
      case Some(arr) => arr.value
      case _ => Nil
    }

    val establisherTransformer = (__ \ "establishers").json.update(
      __.read[JsArray].map { arr =>
        JsArray(arr.value.updated(establisherIndex, arr(establisherIndex).as[JsObject] ++
          Json.obj("director" -> (seqDirectors ++ seqTrustees))))
      }
    )

    transformUa(ua, establisherTransformer)
  }

  private def transformUa(ua: UserAnswers, transformer: Reads[JsObject]): UserAnswers = {
    ua.json.transform(transformer) match {
      case JsSuccess(value, _) => UserAnswers(value)
      case _ => ua
    }
  }

  private def copyDirectorToTrustee: Reads[JsObject] = {
    (__ \ 'trusteeDetails \ 'firstName).json.copyFrom((__ \ 'directorDetails \ 'firstName).json.pick) and
      (__ \ 'trusteeDetails \ 'lastName).json.copyFrom((__ \ 'directorDetails \ 'lastName).json.pick) and
      (__ \ 'trusteeKind).json.put(JsString("individual")) and
      (__ \ 'trusteeContactDetails \ 'phone).json.copyFrom((__ \ 'directorContactDetails \ 'phoneNumber).json.pick) and
      (__ \ 'trusteeContactDetails \ 'email).json.copyFrom((__ \ 'directorContactDetails \ 'emailAddress).json.pick) and
      (__ \ 'trusteeAddressId).json.copyFrom((__ \ 'directorAddressId).json.pick) and
      (__ \ 'trusteeAddressYears).json.copyFrom((__ \ 'companyDirectorAddressYears).json.pick) and
      ((__ \ 'trusteePreviousAddress).json.copyFrom((__ \ 'previousAddress).json.pick) orElse __.json.put(Json.obj())) and
      (__ \ "hasNino").read[Boolean].flatMap {
        case true =>
          (__ \ 'hasNino).json.copyFrom((__ \ 'hasNino).json.pick) and
            (__ \ 'trusteeNino).json.copyFrom((__ \ 'directorNino).json.pick) reduce
        case false =>
          (__ \ 'hasNino).json.copyFrom((__ \ 'hasNino).json.pick) and
            (__ \ 'noNinoReason).json.copyFrom((__ \ 'noNinoReason).json.pick) reduce
      } and
      commonReads reduce
  }

  private def copyTrusteeToDirector: Reads[JsObject] = {
    (__ \ 'directorDetails \ 'firstName).json.copyFrom((__ \ 'trusteeDetails \ 'firstName).json.pick) and
      (__ \ 'directorDetails \ 'lastName).json.copyFrom((__ \ 'trusteeDetails \ 'lastName).json.pick) and
      (__ \ 'directorContactDetails \ 'phoneNumber).json.copyFrom((__ \ 'trusteeContactDetails \ 'phoneNumber).json.pick) and
      (__ \ 'directorContactDetails \ 'emailAddress).json.copyFrom((__ \ 'trusteeContactDetails \ 'emailAddress).json.pick) and
      (__ \ 'directorAddressId).json.copyFrom((__ \ 'trusteeAddressId).json.pick) and
      (__ \ 'companyDirectorAddressYears).json.copyFrom((__ \ 'trusteeAddressYears).json.pick) and
      ((__ \ 'previousAddress).json.copyFrom((__ \ 'trusteePreviousAddress).json.pick) orElse __.json.put(Json.obj())) and
      (__ \ "hasNino").read[Boolean].flatMap {
        case true =>
          (__ \ 'hasNino).json.copyFrom((__ \ 'hasNino).json.pick) and
            (__ \ 'directorNino).json.copyFrom((__ \ 'trusteeNino).json.pick) reduce
        case false =>
          (__ \ 'hasNino).json.copyFrom((__ \ 'hasNino).json.pick) and
            (__ \ 'noNinoReason).json.copyFrom((__ \ 'noNinoReason).json.pick) reduce
      } and
      commonReads reduce
  }

  private def commonReads: Reads[JsObject] = {
    (__ \ 'dateOfBirth).json.copyFrom((__ \ 'dateOfBirth).json.pick) and
      (__ \ "hasUtr").read[Boolean].flatMap {
        case true =>
          (__ \ 'hasUtr).json.copyFrom((__ \ 'hasUtr).json.pick) and
            (__ \ 'utr).json.copyFrom((__ \ 'utr).json.pick) reduce
        case false =>
          (__ \ 'hasUtr).json.copyFrom((__ \ 'hasUtr).json.pick) and
            (__ \ 'noUtrReason).json.copyFrom((__ \ 'noUtrReason).json.pick) reduce
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
    val filteredTrusteesSeq = allIndividualTrustees.filter(indv => !indv.isDeleted && indv.isComplete)
    val allDirectors = (ua.json \ "establishers" \ establisherIndex \ "director").validate[JsArray].asOpt match {
      case Some(arr) => arr.value.zipWithIndex.flatMap { jsValueWithIndex =>
        jsValueWithIndex._1.validate[IndividualDetails](readsDirector(establisherIndex, jsValueWithIndex._2)).asOpt
      }
      case _ => Nil
    }
    filteredTrusteesSeq.filterNot { trustee =>
      val allDirectorsNotDeleted = allDirectors.filter(!_.isDeleted)
      trustee.nino.map(ninoVal => allDirectorsNotDeleted.exists(_.nino.contains(ninoVal)))
        .getOrElse(allDirectorsNotDeleted.exists { director =>
          (trustee.dob, director.dob) match {
            case (Some(trusteeDob), Some(dirDob)) => dirDob.isEqual(trusteeDob) && trustee.fullName == director.fullName
            case _ => false
          }
        })
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

  private def readsDirectors(implicit ua: UserAnswers): Reads[Seq[Option[Seq[IndividualDetails]]]] = new Reads[Seq[Option[Seq[IndividualDetails]]]] {
    private def readsAllDirectors(estIndex: Int)(implicit ua: UserAnswers): Reads[Seq[IndividualDetails]] = {
      case JsArray(directors) =>
        val jsResults: IndexedSeq[JsResult[IndividualDetails]] = directors.zipWithIndex.map { case (jsValue, dirIndex) =>
          readsDirector(estIndex, dirIndex).reads(jsValue)
        }
        asJsResultSeq(jsResults)
      case _ => JsSuccess(Nil)
    }

    override def reads(json: JsValue): JsResult[Seq[Option[Seq[IndividualDetails]]]] = {
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
          asJsResultSeq(jsResults)
        case _ => JsSuccess(Nil)
      }
    }
  }

  private def readsDirector(estIndex: Int, directorIndex: Int)(implicit ua: UserAnswers): Reads[IndividualDetails] = (
    (JsPath \ DirectorNameId.toString).read[PersonName] and
      (JsPath \ DirectorDOBId.toString).readNullable[LocalDate] and
      (JsPath \ DirectorEnterNINOId.toString).readNullable[ReferenceValue]
    ) ((directorName, dob, ninoReferenceVale) =>
    IndividualDetails(
      directorName.firstName, directorName.lastName, directorName.isDeleted, ninoReferenceVale.map(_.value), dob, directorIndex,
      ua.isDirectorComplete(estIndex, directorIndex), Some(estIndex))
  )

  def allIndividualTrustees(implicit ua: UserAnswers): Seq[IndividualDetails] = {
    ua.json.validate[Seq[Option[IndividualDetails]]](readsTrustees) match {
      case JsSuccess(trustees, _) =>
        trustees.flatten
      case JsError(eee) => Nil
    }
  }

  private def readsTrustees(implicit ua: UserAnswers): Reads[Seq[Option[IndividualDetails]]] = new Reads[Seq[Option[IndividualDetails]]] {
    private def readsIndividualTrustee(index: Int)(implicit ua: UserAnswers): Reads[Option[IndividualDetails]] = (
      (JsPath \ TrusteeNameId.toString).read[PersonName] and
        (JsPath \ TrusteeDOBId.toString).readNullable[LocalDate] and
        (JsPath \ TrusteeEnterNINOId.toString).readNullable[ReferenceValue]
      ) ((trusteeName, dob, ninoReferenceVale) =>
      Some(IndividualDetails(
        trusteeName.firstName, trusteeName.lastName, trusteeName.isDeleted, ninoReferenceVale.map(_.value), dob, index, ua.isTrusteeIndividualComplete(index)))
    )

    override def reads(json: JsValue): JsResult[Seq[Option[IndividualDetails]]] = {
      ua.json \ TrusteesId.toString match {
        case JsDefined(JsArray(trustees)) =>
          val jsResults: IndexedSeq[JsResult[Option[IndividualDetails]]] = trustees.zipWithIndex.map { case (jsValue, index) =>
            val trusteeKind = (jsValue \ TrusteeKindId.toString).validate[String].asOpt
            val readsForTrusteeKind = trusteeKind match {
              case Some(TrusteeKind.Individual.toString) => readsIndividualTrustee(index)
              case _ => Reads.pure[Option[IndividualDetails]](None)
            }
            readsForTrusteeKind.reads(jsValue)
          }
          asJsResultSeq(jsResults)
        case _ => JsSuccess(Nil)
      }
    }
  }

  private def asJsResultSeq[A](jsResults: Seq[JsResult[A]]): JsResult[Seq[A]] = {
    JsSuccess(jsResults.collect {
      case JsSuccess(i, _) => i
    })
  }
}

object DataPrefillService {
  case class DirectorIdentifier(establisherIndex: Int, directorIndex: Int)
}
