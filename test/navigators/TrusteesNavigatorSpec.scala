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

package navigators

import base.SpecBase
import identifiers.register.trustees.individual.TrusteeDetailsId
import identifiers.register.trustees._
import models.NormalMode
import models.person.PersonDetails
import org.joda.time.LocalDate
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.Json
import utils.UserAnswers

class TrusteesNavigatorSpec extends SpecBase with MockitoSugar {

  val navigator = new TrusteesNavigator(frontendAppConfig)
  val emptyAnswers = UserAnswers(Json.obj())

  "Pages should post to the correct next page" when {

    ".nextPage(AddTrusteeId)" must {

      "return a `call` to `TrusteeKindController` page with index 1 if no trustees are added yet and 'AddTrustee' is true" in {
        val answers = UserAnswers(Json.obj(
          AddTrusteeId.toString -> true
        ))
        val result = navigator.nextPage(AddTrusteeId, NormalMode)(answers)
        result mustEqual controllers.register.trustees.routes.TrusteeKindController.onPageLoad(NormalMode, 0)
      }

      "return a `call` to `TrusteeKindController` page with index 1 if no trustees are added yet and" +
        " 'AddTrustee' is undefined" in {
        val result = navigator.nextPage(AddTrusteeId, NormalMode)(emptyAnswers)
        result mustEqual controllers.register.trustees.routes.TrusteeKindController.onPageLoad(NormalMode, 0)
      }

      "return a `call` to `TrusteeKindController` page with index 2 if one trustee has already been added and 'AddTrustee' is true" in {
        val answers = UserAnswers(Json.obj(
          AddTrusteeId.toString -> true,
          TrusteesId.toString -> Json.arr(
            Json.obj(
              TrusteeDetailsId.toString -> PersonDetails("first", Some("middle"), "last", LocalDate.now)
            )
          )
        ))
        val result = navigator.nextPage(AddTrusteeId, NormalMode)(answers)
        result mustEqual controllers.register.trustees.routes.TrusteeKindController.onPageLoad(NormalMode, 1)
      }

      "return a `call` to `MoreThanTenTrusteesController` page if 10 trustees has already been added" in {
        val tenTrustees = (0 to 9).map(index => Json.obj(
          TrusteeDetailsId.toString -> PersonDetails(s"testFirstName$index", None, s"testLastName$index", LocalDate.now))
        ).toArray

        val answers = UserAnswers(Json.obj(
          TrusteesId.toString -> tenTrustees
        ))
        val result = navigator.nextPage(AddTrusteeId, NormalMode)(answers)
        result mustEqual controllers.register.trustees.routes.MoreThanTenTrusteesController.onPageLoad(NormalMode)
      }

      "return a `call` to `SchemeReviewController` page if 'AddTrustee' is false" in {
        val answers = UserAnswers(
          Json.obj(
            AddTrusteeId.toString -> false
          )
        )
        val result = navigator.nextPage(AddTrusteeId, NormalMode)(answers)
        result mustEqual controllers.register.routes.SchemeReviewController.onPageLoad()
      }
    }

    ".nextPage(MoreThanTenTrusteesId)" must {
      s"return a `call` to `SchemeReviewController` page" in {
        val result = navigator.nextPage(MoreThanTenTrusteesId, NormalMode)(emptyAnswers)
        result mustEqual controllers.register.routes.SchemeReviewController.onPageLoad()
      }
    }

    ".nextPage(TrusteeKindId)" must {

      "return a `call` to `TrusteeDetailsController` page if 'TrusteeKind' is 'Individual'" in {
        val answers = UserAnswers(
          Json.obj(
            TrusteesId.toString -> Json.arr(
              Json.obj(
                TrusteeKindId.toString -> "individual"
              )
            )
          )
        )
        val result = navigator.nextPage(TrusteeKindId(0), NormalMode)(answers)
        result mustEqual controllers.register.trustees.individual.routes.TrusteeDetailsController.onPageLoad(NormalMode, 0)
      }

      "return a `call` to `TrusteeDetailsController` page if 'TrusteeKind' is 'Company'" in {
        val answers = UserAnswers(
          Json.obj(
            TrusteesId.toString -> Json.arr(
              Json.obj(
                TrusteeKindId.toString -> "company"
              )
            )
          )
        )
        val result = navigator.nextPage(TrusteeKindId(0), NormalMode)(answers)
        result mustEqual controllers.register.trustees.company.routes.CompanyDetailsController.onPageLoad(NormalMode, 0)
      }

      "return a `call` to `SessionExpired` page if TrusteeKind is undefined" in {
        val result = navigator.nextPage(TrusteeKindId(0), NormalMode)(emptyAnswers)
        result mustEqual controllers.routes.SessionExpiredController.onPageLoad()
      }
    }

    ".nextPage(ConfirmDeleteTrusteeId)" must {
      "return a `call` to `AddTrusteeController` page" in {
        val result = navigator.nextPage(ConfirmDeleteTrusteeId, NormalMode)(emptyAnswers)
        result mustEqual controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode)
      }
    }

  }

}

