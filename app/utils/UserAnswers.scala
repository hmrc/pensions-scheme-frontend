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
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.establishers.individual.EstablisherDetailsId
import identifiers.register.trustees.TrusteesId
import identifiers.register.trustees.individual.TrusteeDetailsId
import models.CompanyDetails
import models.person.PersonDetails
import models.register._
import models.register.establishers.individual.EstablisherDetails
import play.api.libs.json._

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
    (JsLens.fromPath(path) andThen JsLens.atAllIndices).getAll(json)
      .flatMap(a => traverse(a.map(Json.fromJson[A]))).asOpt
  }

  def getAllRecursive[A](path: JsPath)(implicit rds: Reads[A]): Option[Seq[A]] = {
    JsLens.fromPath(path).getAll(json)
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
        (__ \ EstablisherDetailsId.toString).read[EstablisherDetails]
          .map(details => EstablisherIndividualName(s"${details.firstName} ${details.lastName}"))

      val companyName: Reads[EntityDetails] =
        (__ \ CompanyDetailsId.toString).read[CompanyDetails]
          .map(details => EstablisherCompanyName(details.companyName))

      individualName orElse companyName
    }

    getAll[EntityDetails](EstablishersId.path)(nameReads).map(
      _.zipWithIndex.map {
        case (name, id) =>
          name.route(id, None)
      }
    ).getOrElse(Seq.empty)
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
          case e @ JsError(_) =>
            e
        }.reduceLeft(JsError.merge)
    }
  }

  def allTrustees: Seq[(String, String)] = {

    val nameReads: Reads[EntityDetails] =  {

      val individualName: Reads[EntityDetails] =
        (__ \ TrusteeDetailsId.toString).read[PersonDetails]
          .map(details => TrusteeIndividualName(details.fullName))

      val companyName: Reads[EntityDetails] =
        (__ \ identifiers.register.trustees.company.CompanyDetailsId.toString).read[CompanyDetails]
          .map(details => TrusteeCompanyName(details.companyName))

      individualName orElse companyName
    }

    getAll[EntityDetails](JsPath \ TrusteesId.toString)(nameReads).map {
      _.zipWithIndex.map {
        case (name, id) =>
          name.route(id, None)
      }
    }.getOrElse(Seq.empty)
  }

  def hasCompanies: Boolean = {
    val establishers = json \ "establishers" \\ "companyDetails"

    establishers.nonEmpty || {
      val trustees = json \ "trustees" \\ "companyDetails"
      trustees.nonEmpty
    }
  }

}
