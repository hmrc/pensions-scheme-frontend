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
import identifiers.register.establishers.company.CompanyRegistrationNumberId
import models.CompanyRegistrationNumber
import models.register.establishers.individual.EstablishersIndividualMap
import play.api.libs.json.{JsPath, JsValue, Json, Reads}

import scala.util.{Success, Try}

class UserAnswers(json: JsValue) extends Enumerable.Implicits with MapFormats {

  def get[A](id: TypedIdentifier[A])(implicit rds: Reads[A]): Option[A] = {
    get[A](id.path)
  }

  def get[A](path: JsPath)(implicit rds: Reads[A]): Option[A] = {
    JsLens.fromPath(path).get(json)
      .flatMap(Json.fromJson[A]).asOpt
  }

  def allEstablishers: Option[Map[String, String]] = {
//    establisherDetails.map(_.getValues.map{ estDetails =>
//      (estDetails.establisherName, routes.AddEstablisherController.onPageLoad(NormalMode).url)
//    }.toMap)
    ???
  }
  def companyRegistrationNumber: Option[EstablishersIndividualMap[CompanyRegistrationNumber]]= cacheMap.getEntry[EstablishersIndividualMap[CompanyRegistrationNumber]](
    CompanyRegistrationNumberId.toString)

  def companyRegistrationNumber(index: Int): Try[Option[CompanyRegistrationNumber]] = companyRegistrationNumber.map(_.get(index)).getOrElse(Success(None))

}
