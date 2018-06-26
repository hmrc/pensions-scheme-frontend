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

import controllers.register.{establishers, trustees}
import identifiers.TypedIdentifier
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.establishers.company.director.DirectorDetailsId
import identifiers.register.establishers.individual.EstablisherDetailsId
import identifiers.register.trustees.TrusteesId
import identifiers.register.trustees.individual.TrusteeDetailsId
import models.{CompanyDetails, Index, NormalMode, TrusteeEntityDetails}
import models.person.PersonDetails
import models.register._
import models.register.establishers.company.director.DirectorDetails
import models.register.trustees.TrusteeKind
import play.api.Logger
import play.api.libs.json._
import play.api.mvc.Result
import play.api.mvc.Results._
import viewmodels.EntityKind

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

  def allEstablishers: Seq[(String, String)] = {

    val nameReads: Reads[EntityDetails] =  {

      val individualName: Reads[EntityDetails] =
        (__ \ EstablisherDetailsId.toString).read[PersonDetails]
          .map(details => EstablisherIndividualName(s"${details.firstName} ${details.lastName}", details.isDeleted))

      val companyName: Reads[EntityDetails] =
        (__ \ CompanyDetailsId.toString).read[CompanyDetails]
          .map(details => EstablisherCompanyName(details.companyName, details.isDeleted))

      individualName orElse companyName
    }

    getAll[EntityDetails](JsPath \ EstablishersId.toString)(nameReads).map { entity =>
      entity.filterNot(_.route(0, None)._2).zipWithIndex.map{
        case (filtered, index) =>
          val (name, _, url) = filtered.route(index, None)
          (name, url)
      }
    }.getOrElse(Seq.empty)
  }


  def allDirectors(index: Int): Seq[DirectorDetails] = {
    getAllRecursive[DirectorDetails](DirectorDetailsId.collectionPath(index))
      .map(_.filterNot(_.isDeleted)).getOrElse(Nil)
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

  def allTrustees: Seq[TrusteeEntityDetails] = {
    val nameReads: Reads[TrusteeEntityDetails] = {

      val individualTrustee: Reads[TrusteeEntityDetails] = (__ \ TrusteeDetailsId.toString)
        .read[PersonDetails]
        .map(details => TrusteeEntityDetails(details.fullName, details.isDeleted, None, None, TrusteeKind.Individual))

      val companyTrustee: Reads[TrusteeEntityDetails] = (__ \ identifiers.register.trustees.company.CompanyDetailsId.toString)
        .read[CompanyDetails]
        .map(details => TrusteeEntityDetails(details.companyName, details.isDeleted, None, None, TrusteeKind.Company))

      individualTrustee orElse companyTrustee
    }

    getAll[TrusteeEntityDetails](JsPath \ TrusteesId.toString)(nameReads).map {
      case (trusteeEntityDetails) =>
        trusteeEntityDetails.map{
          case (trusteeEntity) =>
            trusteeEntity.trusteeKind match {
            case TrusteeKind.Individual =>
              trusteeEntity.copy(
                changeUrl = Some(controllers.register.trustees.individual.routes.TrusteeDetailsController.onPageLoad(
                  NormalMode, trusteeEntityDetails.indexOf(trusteeEntity)).url),
                deleteUrl = Some(trustees.routes.ConfirmDeleteTrusteeController.onPageLoad(
                  Index(trusteeEntityDetails.indexOf(trusteeEntity)), TrusteeKind.Individual).url)
              )
            case TrusteeKind.Company =>
                trusteeEntity.copy(
                  changeUrl = Some(controllers.register.trustees.company.routes.CompanyDetailsController.onPageLoad(
                    NormalMode, trusteeEntityDetails.indexOf(trusteeEntity)).url),
                  deleteUrl = Some(trustees.routes.ConfirmDeleteTrusteeController.onPageLoad(
                    Index(trusteeEntityDetails.indexOf(trusteeEntity)), TrusteeKind.Company).url))
            case _ => trusteeEntity
          }
        }

    }.getOrElse(Seq.empty)
  }

  def allTrusteesAfterDelete: Seq[TrusteeEntityDetails] = {
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
    val establishers = json \ "establishers" \\ "companyDetails"

    establishers.nonEmpty || {
      val trustees = json \ "trustees" \\ "companyDetails"
      trustees.nonEmpty
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

}
