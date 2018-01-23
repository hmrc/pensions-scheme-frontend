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

import identifiers.register._
import identifiers.register.establishers._
import identifiers.register.establishers.individual._
import uk.gov.hmrc.http.cache.client.CacheMap
import models._
import controllers.register.establishers.routes
import play.api.libs.json.{JsPath, JsValue, Json, Reads}
import play.api.libs.json._

import scala.util.{Success, Try}

class UserAnswers(json: JsValue) extends Enumerable.Implicits with MapFormats {

  private def fromPath[A](path: JsPath)(implicit rds: Reads[A]): Option[A] = {
    JsLens.fromPath(path).get(json)
      .flatMap(Json.fromJson[A]).asOpt
  }

  def contactDetails(index: Int): Option[ContactDetails] =
    fromPath[ContactDetails](__ \ "establishers" \ index \ ContactDetailsId)

  def establisherNino(index:Int): Option[EstablisherNino] =
    fromPath[EstablisherNino](__ \ "establishers" \ index \ EstablisherNinoId)

  def uniqueTaxReference(index: Int): Option[UniqueTaxReference] =
    fromPath[UniqueTaxReference](__ \ "establishers" \ index \ UniqueTaxReferenceId)

  def addEstablisher(): Option[Boolean] =
    fromPath[Boolean](__ \ AddEstablisherId)

  def establisherKind: Option[EstablishersIndividualMap[EstablisherKind]] =
    fromPath[EstablishersIndividualMap[EstablisherKind]](__ \ EstablisherKindId)

  def establisherKind(index: Int): Try[Option[EstablisherKind]] =
    establisherKind.map(_.get(index)).getOrElse(Success(None))

  def establisherDetails(index: Int): Option[EstablisherDetails] =
    fromPath[EstablisherDetails](__ \ "establishers" \ index \ EstablisherDetailsId)

  def allEstablishers: Option[Map[String, String]] = {
//    establisherDetails.map(_.getValues.map{ estDetails =>
//      (estDetails.establisherName, routes.AddEstablisherController.onPageLoad(NormalMode).url)
//    }.toMap)
    ???
  }

  def addressYears(index: Int): Option[AddressYears] =
    fromPath[AddressYears](__ \ "establishers" \ index \ AddressYearsId)

  def schemeEstablishedCountry: Option[String] =
    fromPath[String](__ \ SchemeEstablishedCountryId)

  def uKBankAccount: Option[Boolean] =
    fromPath[Boolean](__ \ UKBankAccountId)

  def uKBankDetails: Option[UKBankDetails] =
    fromPath[UKBankDetails](__ \ UKBankDetailsId)

  def benefits: Option[Benefits] =
    fromPath[Benefits](__ \ BenefitsId)

  def benefitsInsurer: Option[BenefitsInsurer] =
    fromPath[BenefitsInsurer](__ \ BenefitsInsurerId)

  def membership: Option[Membership] =
    fromPath[Membership](__ \ MembershipId)

  def membershipFuture: Option[MembershipFuture] =
    fromPath[MembershipFuture](__ \ MembershipFutureId)

  def investmentRegulated: Option[Boolean] =
    fromPath[Boolean](__ \ InvestmentRegulatedId)

  def securedBenefits: Option[Boolean] =
    fromPath[Boolean](__ \ SecuredBenefitsId)

  def occupationalPensionScheme: Option[Boolean] =
    fromPath[Boolean](__ \ OccupationalPensionSchemeId)

  def schemeDetails: Option[SchemeDetails] =
    fromPath[SchemeDetails](__ \ SchemeDetailsId)
}
