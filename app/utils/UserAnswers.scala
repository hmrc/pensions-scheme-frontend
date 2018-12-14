/*
 * Copyright 2018 HM Revenue & Customs
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

import identifiers.TypedIdentifier
import identifiers.register.establishers.company.director.{DirectorDetailsId, IsDirectorCompleteId}
import identifiers.register.establishers.company.{CompanyDetailsId => EstablisherCompanyDetailsId}
import identifiers.register.establishers.individual.EstablisherDetailsId
import identifiers.register.establishers.partnership.PartnershipDetailsId
import identifiers.register.establishers.partnership.partner.{IsPartnerCompleteId, PartnerDetailsId}
import identifiers.register.establishers.{EstablisherKindId, EstablishersId, IsEstablisherCompleteId}
import identifiers.register.trustees.company.CompanyDetailsId
import identifiers.register.trustees.individual.TrusteeDetailsId
import identifiers.register.trustees.partnership.{IsPartnershipCompleteId, PartnershipDetailsId => TrusteePartnershipDetailsId}
import identifiers.register.trustees.{IsTrusteeCompleteId, TrusteeKindId, TrusteesId}
import models.person.PersonDetails
import models.register._
import models.{CompanyDetails, PartnershipDetails}
import play.api.Logger
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc.Result
import play.api.mvc.Results._

import scala.annotation.tailrec
import scala.concurrent.Future
import scala.language.implicitConversions

case class UserAnswers(json: JsValue = Json.obj()) {

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

  def set[I <: TypedIdentifier.PathDependent](id: I)(value: id.Data)(implicit writes: Writes[id.Data]): JsResult[UserAnswers] = {

    val jsValue = Json.toJson(value)

    JsLens.fromPath(id.path)
      .set(jsValue, json)
      .flatMap(json => id.cleanup(Some(value), UserAnswers(json)))
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
        case JsSuccess(value, path) =>
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

  val readEstablishers: Reads[Seq[Establisher[_]]] = new Reads[Seq[Establisher[_]]] {
    private def readsIndividual(index: Int): Reads[Establisher[_]] = (
      (JsPath \ EstablisherDetailsId.toString).read[PersonDetails] and
        (JsPath \ IsEstablisherCompleteId.toString).readNullable[Boolean]
      ) ((details, isComplete) =>
      EstablisherIndividualEntity(EstablisherDetailsId(index), details.fullName, details.isDeleted, isComplete.getOrElse(false))
    )

    private def readsCompany(index: Int): Reads[Establisher[_]] = (
      (JsPath \ EstablisherCompanyDetailsId.toString).read[CompanyDetails] and
        (JsPath \ IsEstablisherCompleteId.toString).readNullable[Boolean]
      ) ((details, isComplete) =>
      EstablisherCompanyEntity(EstablisherCompanyDetailsId(index), details.companyName, details.isDeleted, isComplete.getOrElse(false))
    )

    private def readsPartnership(index: Int): Reads[Establisher[_]] = (
      (JsPath \ PartnershipDetailsId.toString).read[PartnershipDetails] and
        (JsPath \ IsEstablisherCompleteId.toString).readNullable[Boolean]
      ) ((details, isComplete) =>
      EstablisherPartnershipEntity(PartnershipDetailsId(index), details.name, details.isDeleted, isComplete.getOrElse(false))
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

  def allEstablishers: Seq[Establisher[_]] = {
    json.validate[Seq[Establisher[_]]](readEstablishers) match {
      case JsSuccess(establishers, _) =>
        establishers
      case JsError(errors) =>
        Logger.warn(s"Invalid json while reading all the establishers for addEstablisher: $errors")
        Nil
    }
  }

  def allEstablishersAfterDelete: Seq[Establisher[_]] = {
    allEstablishers.filterNot(_.isDeleted)
  }

  def allDirectors(establisherIndex: Int): Seq[DirectorEntity] = {
    getAllRecursive[PersonDetails](DirectorDetailsId.collectionPath(establisherIndex)).map {
      details =>
        details.map { director =>
          val directorIndex = details.indexOf(director)
          val isComplete = get(IsDirectorCompleteId(establisherIndex, directorIndex)).getOrElse(false)
          DirectorEntity(
            DirectorDetailsId(establisherIndex, directorIndex),
            director.fullName,
            director.isDeleted,
            isComplete
          )
        }
    }.getOrElse(Seq.empty)
  }

  def allDirectorsAfterDelete(establisherIndex: Int): Seq[DirectorEntity] = {
    allDirectors(establisherIndex).filterNot(_.isDeleted)
  }

  def allPartners(establisherIndex: Int): Seq[PartnerEntity] = {
    getAllRecursive[PersonDetails](PartnerDetailsId.collectionPath(establisherIndex)).map {
      details =>
        details.map { partner =>
          val partnerIndex = details.indexOf(partner)
          val isComplete = get(IsPartnerCompleteId(establisherIndex, partnerIndex)).getOrElse(false)
          PartnerEntity(
            PartnerDetailsId(establisherIndex, partnerIndex),
            partner.fullName,
            partner.isDeleted,
            isComplete
          )
        }
    }.getOrElse(Seq.empty)
  }

  def allPartnersAfterDelete(establisherIndex: Int): Seq[PartnerEntity] = {
    allPartners(establisherIndex).filterNot(_.isDeleted)
  }

  val readTrustees: Reads[Seq[Trustee[_]]] = new Reads[Seq[Trustee[_]]] {
    private def readsIndividual(index: Int): Reads[Trustee[_]] = (
      (JsPath \ TrusteeDetailsId.toString).read[PersonDetails] and
        (JsPath \ IsTrusteeCompleteId.toString).readNullable[Boolean]
      ) ((details, isComplete) => TrusteeIndividualEntity(TrusteeDetailsId(index), details.fullName, details.isDeleted, isComplete.getOrElse(false))
    )

    private def readsCompany(index: Int): Reads[Trustee[_]] = (
      (JsPath \ CompanyDetailsId.toString).read[CompanyDetails] and
        (JsPath \ IsTrusteeCompleteId.toString).readNullable[Boolean]
      ) ((details, isComplete) => TrusteeCompanyEntity(CompanyDetailsId(index), details.companyName, details.isDeleted, isComplete.getOrElse(false))
    )

    private def readsPartnership(index: Int): Reads[Trustee[_]] = (
      (JsPath \ TrusteePartnershipDetailsId.toString).read[PartnershipDetails] and
        (JsPath \ IsPartnershipCompleteId.toString).readNullable[Boolean]
      ) ((details, isComplete) => TrusteePartnershipEntity(TrusteePartnershipDetailsId(index), details.name, details.isDeleted, isComplete.getOrElse(false))
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
              readsIndividual(index)
                orElse readsCompany(index)
                orElse readsPartnership(index)
                orElse readsSkeleton(index)
          )
        case _ => JsSuccess(Nil)
      }
    }
  }

  def allTrustees: Seq[Trustee[_]] = {
    json.validate[Seq[Trustee[_]]](readTrustees) match {
      case JsSuccess(trustees, _) =>
        trustees
      case JsError(errors) =>
        Logger.warn(s"Invalid json while reading all the trustees for addTrustees: $errors")
        Nil
    }
  }

  def allTrusteesAfterDelete: Seq[Trustee[_]] = {
    allTrustees.filterNot(_.isDeleted)
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

  def hasCompanies: Boolean = {
    val establisherCompany = json \ "establishers" \\ "companyDetails"
    val establisherPartnership = json \ "establishers" \\ "partnershipDetails"

    establisherCompany.nonEmpty || establisherPartnership.nonEmpty
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

}
