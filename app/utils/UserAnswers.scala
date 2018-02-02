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
import models.register.establishers.individual.EstablisherDetails
import models.{CompanyDetails, NormalMode}
import play.api.libs.json._

class UserAnswers(json: JsValue) extends Enumerable.Implicits with MapFormats {

  def get[A](id: TypedIdentifier[A])(implicit rds: Reads[A]): Option[A] = {
    get[A](id.path)
  }

  def get[A](path: JsPath)(implicit rds: Reads[A]): Option[A] = {
    JsLens.fromPath(path).get(json)
      .flatMap(Json.fromJson[A]).asOpt
  }

  def getAll[A](path: JsPath)(implicit rds: Reads[A]): Option[Seq[A]] = {
    (JsLens.fromPath(path) andThen JsLens.atAllIndices).get(json)
      .flatMap(Json.fromJson[Seq[A]]).asOpt
  }

  def allEstablishers: Option[Seq[(String, String)]] = {

    val nameReads: Reads[String] =  {

      val individualName: Reads[String] =
        (__ \ EstablisherDetailsId.toString).read[EstablisherDetails]
          .map {
            details =>
              s"${details.firstName} ${details.lastName}"
          }

      val companyName: Reads[String] =
        (__ \ CompanyDetailsId.toString).read[CompanyDetails]
          .map(_.companyName)

      individualName orElse companyName
    }

    getAll[String](EstablishersId.path)(nameReads).map(
      _.map {
        name =>
          name ->
            controllers.register.establishers.routes.AddEstablisherController.onPageLoad(NormalMode).url
      }
    )
  }
}
