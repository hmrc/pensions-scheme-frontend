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

import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.establishers.individual.EstablisherDetailsId
import models.register.establishers.individual.EstablisherDetails
import models.{CompanyDetails, NormalMode}
import org.joda.time.LocalDate
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.{JsPath, Json}

class UserAnswersSpec extends WordSpec with MustMatchers with OptionValues {

  private val establishers = Json.obj(
    "establishers" -> Json.arr(
      Json.obj(
        "name" -> "foo"
      ),
      Json.obj(
        "name" -> "bar"
      )
    )
  )

  ".allEstablishers" must {
    "return a map of establishers names and edit links" in {

      val json = Json.obj(
        EstablishersId.toString -> Json.arr(
          Json.obj(
            CompanyDetailsId.toString ->
              CompanyDetails("my company", None, None)
          ),
          Json.obj(
            EstablisherDetailsId.toString ->
              EstablisherDetails("my", None, "name", LocalDate.now)
          )
        )
      )

      val userAnswers = new UserAnswers(json)

      userAnswers.allEstablishers.value mustEqual Seq(
        "my company" ->
          controllers.register.establishers.routes.AddEstablisherController.onPageLoad(NormalMode).url,
        "my name" ->
          controllers.register.establishers.routes.AddEstablisherController.onPageLoad(NormalMode).url
      )
    }
  }

  ".getAllRecursive" must {
    "get all matching recursive results" in {
      val userAnswers = UserAnswers(establishers)
      val values = userAnswers.getAllRecursive[String](JsPath \ "establishers" \\ "name").value
      values must contain("foo")
      values must contain("bar")
    }

    "return an empty list when no matches" in {
      val userAnswers = UserAnswers(establishers)
      userAnswers.getAllRecursive[String](JsPath \ "establishers" \\ "address").value.size mustBe 0
    }
  }

  ".getAll" must {
    "get all matching results" in {
      val userAnswers = UserAnswers(establishers)
      val values = userAnswers.getAll[String](JsPath \ "establishers" \\ "name").value
      values must contain("foo")
    }

    "return an empty list when no matches" in {
      val userAnswers = UserAnswers(establishers)
      userAnswers.getAll[String](JsPath \ "establishers" \\ "address").value.size mustBe 0
    }
  }

  ".get" must {
    "get a matching result" in {
      val userAnswers = UserAnswers(establishers)
      val values = userAnswers.get[String](JsPath \ "establishers" \ 0).value
      values mustBe "foo"

    }

    "return empty when no matches" in {
      val userAnswers = UserAnswers(establishers)
      userAnswers.get[String](JsPath \ "establishers" \ 8).value.size mustBe 0
    }
  }
}
