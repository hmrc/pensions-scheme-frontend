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

package controllers

import base.SpecBase
import controllers.actions.FakeDataRetrievalAction
import identifiers.register.adviser.AdviserDetailsId
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.establishers.company.director.DirectorDetailsId
import identifiers.register.establishers.individual.EstablisherDetailsId
import identifiers.register.establishers.partnership.PartnershipDetailsId
import identifiers.register.establishers.partnership.partner.PartnerDetailsId
import identifiers.register.trustees.TrusteesId
import identifiers.register.trustees.individual.TrusteeDetailsId
import identifiers.register.{SchemeDetailsId, SchemeNameId}
import models.person.PersonDetails
import models.register.{AdviserDetails, SchemeDetails, SchemeType}
import models.{CompanyDetails, PartnershipDetails}
import org.joda.time.LocalDate
import play.api.libs.json.Json
import utils.{Enumerable, MapFormats}

trait ControllerSpecBase extends SpecBase with Enumerable.Implicits with MapFormats {

  implicit val global = scala.concurrent.ExecutionContext.Implicits.global

  val cacheMapId = "id"

  def getEmptyData: FakeDataRetrievalAction = new FakeDataRetrievalAction(Some(Json.obj()))

  def getMandatorySchemeName: FakeDataRetrievalAction = new FakeDataRetrievalAction(Some(Json.obj(
    SchemeDetailsId.toString -> SchemeDetails("Test Scheme Name", SchemeType.SingleTrust))))

  def getMandatorySchemeNameHs: FakeDataRetrievalAction = new FakeDataRetrievalAction(Some(Json.obj(
    SchemeNameId.toString -> "Test Scheme Name")))

  def dontGetAnyData: FakeDataRetrievalAction = new FakeDataRetrievalAction(None)

  def getMandatoryEstablisher: FakeDataRetrievalAction = new FakeDataRetrievalAction(Some(
    Json.obj(
      SchemeDetailsId.toString ->
        SchemeDetails("Test Scheme Name", SchemeType.SingleTrust),
      "establishers" -> Json.arr(
        Json.obj(
          EstablisherDetailsId.toString -> PersonDetails("test first name", None, "test last name", LocalDate.now(), false)
        )
      )
    )))

  def getMandatoryTrustee: FakeDataRetrievalAction = new FakeDataRetrievalAction(Some(
    Json.obj(
      SchemeDetailsId.toString ->
        SchemeDetails("Test Scheme Name", SchemeType.SingleTrust),
      "trustees" -> Json.arr(
        Json.obj(
          TrusteeDetailsId.toString ->
            PersonDetails("Test", Some("Trustee"), "Name", LocalDate.now)
        )
      )
    )))

  def getMandatoryTrusteeCompany: FakeDataRetrievalAction = new FakeDataRetrievalAction(
    Some(Json.obj(
      SchemeDetailsId.toString ->
        SchemeDetails("Test Scheme Name", SchemeType.SingleTrust),
      TrusteesId.toString -> Json.arr(
        Json.obj(
          CompanyDetailsId.toString ->
            CompanyDetails("test company name", Some("123456"), Some("abcd"))
        )
      )
    ))
  )

  def getMandatoryEstablisherCompany: FakeDataRetrievalAction = new FakeDataRetrievalAction(
    Some(Json.obj(
      SchemeDetailsId.toString ->
        SchemeDetails("Test Scheme Name", SchemeType.SingleTrust),
      EstablishersId.toString -> Json.arr(
        Json.obj(
          CompanyDetailsId.toString ->
            CompanyDetails("test company name", Some("123456"), Some("abcd"))
        )
      )
    ))
  )

  def getMandatoryEstablisherPartnership: FakeDataRetrievalAction = new FakeDataRetrievalAction(
    Some(Json.obj(
      SchemeDetailsId.toString ->
        SchemeDetails("Test Scheme Name", SchemeType.SingleTrust),
      EstablishersId.toString -> Json.arr(
        Json.obj(
          PartnershipDetailsId.toString ->
            PartnershipDetails("test partnership name")
        )
      )
    ))
  )

  def getMandatoryTrusteePartnership: FakeDataRetrievalAction = new FakeDataRetrievalAction(
    Some(Json.obj(
      SchemeDetailsId.toString ->
        SchemeDetails("Test Scheme Name", SchemeType.SingleTrust),
      TrusteesId.toString -> Json.arr(
        Json.obj(
          PartnershipDetailsId.toString ->
            PartnershipDetails("test partnership name")
        )
      )
    ))
  )

  def getMandatoryEstablisherCompanyDirector: FakeDataRetrievalAction = new FakeDataRetrievalAction(
    Some(Json.obj(
      SchemeDetailsId.toString ->
        SchemeDetails("Test Scheme Name", SchemeType.SingleTrust),
      EstablishersId.toString -> Json.arr(
        Json.obj(
          CompanyDetailsId.toString ->
            CompanyDetails("test company name", Some("123456"), Some("abcd")),
          "director" -> Json.arr(
            Json.obj(
              DirectorDetailsId.toString -> PersonDetails("first", Some("middle"), "last",
                new LocalDate(1990, 2, 2))
            )
          )
        )
      )
    ))
  )

  def getMandatoryEstablisherPartner: FakeDataRetrievalAction = new FakeDataRetrievalAction(
    Some(Json.obj(
      SchemeDetailsId.toString ->
        SchemeDetails("Test Scheme Name", SchemeType.SingleTrust),
      EstablishersId.toString -> Json.arr(
        Json.obj(
          PartnershipDetailsId.toString ->
            PartnershipDetails("test partnership name"),
          "partner" -> Json.arr(
            Json.obj(
              PartnerDetailsId.toString -> PersonDetails("first", Some("middle"), "last",
                new LocalDate(1990, 2, 2))
            )
          )
        )
      )
    ))
  )

  def getMandatoryAdviser: FakeDataRetrievalAction = new FakeDataRetrievalAction(
    Some(Json.obj(
      SchemeDetailsId.toString ->
        SchemeDetails("Test Scheme Name", SchemeType.SingleTrust),
      AdviserDetailsId.toString ->
        AdviserDetails("name", "email", "phone")
    ))
  )
}
