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

package views.register.establishers.company

import identifiers.register.SchemeDetailsId
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.establishers.company.director.DirectorDetailsId
import models.register.establishers.company.director.DirectorDetails
import models.register.{SchemeDetails, SchemeType}
import models.{CompanyDetails, Index}
import org.joda.time.LocalDate
import play.api.libs.json.Json
import views.behaviours.ViewBehaviours
import views.html.register.establishers.company.companyReview

class CompanyReviewViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "companyReview"
  val index = Index(0)
  val schemeName = "Test Scheme Name"
  val companyName = "test company name"
  val directors = Seq("director a", "director b", "director c")
  def director(lastName: String) = Json.obj(
    DirectorDetailsId.toString -> DirectorDetails("director", None, lastName, LocalDate.now())
  )

  val validData = Json.obj(
    SchemeDetailsId.toString ->
      SchemeDetails(schemeName, SchemeType.SingleTrust),
    EstablishersId.toString -> Json.arr(
      Json.obj(
        CompanyDetailsId.toString ->
          CompanyDetails(companyName, Some("123456"), Some("abcd")),
        "director" -> Json.arr(director("a"), director("b"), director("c"))
      )
    )
  )

  def createView = () => companyReview(frontendAppConfig, index, schemeName, companyName, directors)(fakeRequest, messages)

  "CompanyReview view" must {
    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__heading"))

  }
}
