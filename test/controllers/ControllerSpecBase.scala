/*
 * Copyright 2020 HM Revenue & Customs
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
import controllers.actions.{FakeDataRetrievalAction, FakePspDataRetrievalAction}
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.establishers.company.director.DirectorNameId
import identifiers.register.establishers.individual.EstablisherNameId
import identifiers.register.establishers.partnership.PartnershipDetailsId
import identifiers.register.establishers.partnership.partner.PartnerNameId
import identifiers.register.trustees.TrusteesId
import identifiers.register.trustees.individual.TrusteeNameId
import identifiers.{AdviserNameId, SchemeNameId}
import models.person.PersonName
import models.{CompanyDetails, PartnershipDetails, person}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.libs.json.{JsObject, Json}
import utils.{Enumerable, MapFormats}

trait ControllerSpecBase extends SpecBase with Enumerable.Implicits with MapFormats {

  implicit val global = scala.concurrent.ExecutionContext.Implicits.global

  val cacheMapId = "id"

  def getEmptyData: FakeDataRetrievalAction = new FakeDataRetrievalAction(Some(Json.obj()))
  def getEmptyDataPsp: FakePspDataRetrievalAction = new FakePspDataRetrievalAction(Some(Json.obj()))

  def getMandatorySchemeNameHs: FakeDataRetrievalAction = new FakeDataRetrievalAction(Some(Json.obj(
    SchemeNameId.toString -> "Test Scheme Name")))

  def dontGetAnyData: FakeDataRetrievalAction = new FakeDataRetrievalAction(None)

  def dontGetAnyDataViewOnly: FakeDataRetrievalAction = new FakeDataRetrievalAction(None, viewOnly = true)

  def getMandatoryEstablisher: FakeDataRetrievalAction = new FakeDataRetrievalAction(Some(
    Json.obj(
      "establishers" -> Json.arr(
        Json.obj(
          EstablisherNameId.toString -> PersonName("Test", "Name", false)
        )
      )
    )))


  def getMandatoryTrustee: FakeDataRetrievalAction = new FakeDataRetrievalAction(Some(
    Json.obj(
      TrusteesId.toString -> Json.arr(
        Json.obj(
          TrusteeNameId.toString ->
            PersonName("Test", "Name")
        )
      )
    )))

  def getMandatoryTrusteeCompany: FakeDataRetrievalAction = new FakeDataRetrievalAction(
    Some(Json.obj(
      TrusteesId.toString -> Json.arr(
        Json.obj(
          CompanyDetailsId.toString ->
            CompanyDetails("test company name")
        )
      )
    ))
  )

  def getMandatoryEstablisherCompany: FakeDataRetrievalAction = new FakeDataRetrievalAction(
    Some(Json.obj(
      EstablishersId.toString -> Json.arr(
        Json.obj(
          CompanyDetailsId.toString ->
            CompanyDetails("test company name")
        )
      )
    ))
  )

  def getMandatoryEstablisherIndividual: FakeDataRetrievalAction = new FakeDataRetrievalAction(
    Some(Json.obj(
      EstablishersId.toString -> Json.arr(
        Json.obj(
          EstablisherNameId.toString ->
            person.PersonName("Test", "Name")
        )
      )
    ))
  )

  def getMandatoryEstablisherPartnership: FakeDataRetrievalAction = new FakeDataRetrievalAction(
    Some(Json.obj(
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
      TrusteesId.toString -> Json.arr(
        Json.obj(
          PartnershipDetailsId.toString ->
            PartnershipDetails("test partnership name")
        )
      )
    ))
  )

  def getMandatoryEstablisherCompanyDirectorWithDirectorName: FakeDataRetrievalAction = new FakeDataRetrievalAction(
    Some(Json.obj(
      EstablishersId.toString -> Json.arr(
        Json.obj(
          CompanyDetailsId.toString ->
            CompanyDetails("test company name"),
          "director" -> Json.arr(
            Json.obj(
              DirectorNameId.toString -> PersonName("first", "last")
            )
          )
        )
      )
    ))
  )

  def getMandatoryPartner: FakeDataRetrievalAction = new FakeDataRetrievalAction(
    Some(Json.obj(
      EstablishersId.toString -> Json.arr(
        Json.obj(
          PartnershipDetailsId.toString ->
            PartnershipDetails("test partnership name"),
          "partner" -> Json.arr(
            Json.obj(
              PartnerNameId.toString -> PersonName("first", "last")
            )
          )
        )
      )
    ))
  )

  def getMandatoryWorkingKnowledgePerson: FakeDataRetrievalAction = new FakeDataRetrievalAction(
    Some(Json.obj(AdviserNameId.toString ->
      "name"
    ))
  )

  def asDocument(htmlAsString: String): Document = Jsoup.parse(htmlAsString)

  protected def validCompanyDirectorData(jsValue: (String, Json.JsValueWrapper)): JsObject = {
    Json.obj(
      EstablishersId.toString -> Json.arr(
        Json.obj(
          "director" -> Json.arr(
            Json.obj(
              "directorDetails" -> Json.obj(
                "firstName" -> "first",
                "lastName" -> "last"
              ),
              jsValue
            )
          )
        )
      )
    )
  }

  protected def validTrusteeData(jsValue: (String, Json.JsValueWrapper)): JsObject = {
    Json.obj(
      TrusteesId.toString -> Json.arr(
        Json.obj(
          TrusteeNameId.toString -> Json.obj(
            "firstName" -> "Test",
            "lastName" -> "Name",
            "date" -> "2001-01-01",
            "isDeleted" -> "false"
          ),
          jsValue
        )
      )
    )
  }

  protected def validEstablisherIndividualData(jsValue: (String, Json.JsValueWrapper)): JsObject = {
    Json.obj(
      EstablishersId.toString -> Json.arr(
        Json.obj(
          EstablisherNameId.toString -> Json.obj(
            "firstName" -> "Test",
            "lastName" -> "Name",
            "isDeleted" -> "false"
          ),
          jsValue
        )
      )
    )
  }

  protected def validTrusteePartnershipData(jsValue: (String, Json.JsValueWrapper)): JsObject = {
    Json.obj(
      TrusteesId.toString -> Json.arr(
        Json.obj(
          PartnershipDetailsId.toString -> PartnershipDetails("test partnership name"),
          jsValue
        )
      )
    )
  }

  protected def validEstablisherPartnershipData(jsValue: (String, Json.JsValueWrapper)): JsObject = {
    Json.obj(
      EstablishersId.toString -> Json.arr(
        Json.obj(
          PartnershipDetailsId.toString -> PartnershipDetails("test partnership name"),
          jsValue
        )
      )
    )
  }

  protected def validPartnerData(jsValue: (String, Json.JsValueWrapper)): JsObject = {
    Json.obj(
      EstablishersId.toString -> Json.arr(
        Json.obj(
          "partner" -> Json.arr(
            Json.obj(
              PartnerNameId.toString -> Json.obj(
                "firstName" -> "first",
                "lastName" -> "last"
              ),
              jsValue
            )
          )
        )
      )
    )
  }
}
