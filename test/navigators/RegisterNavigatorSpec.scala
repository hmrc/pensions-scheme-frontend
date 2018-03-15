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

import models.{CheckMode, NormalMode}
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.Json
import utils.UserAnswers
import identifiers.register._

class RegisterNavigatorSpec extends WordSpec with MustMatchers with PropertyChecks {

  val navigator = new RegisterNavigator()
  val emptyAnswers = new UserAnswers(Json.obj())

  "NormalMode" when {
    ".nextPage(SchemeDetails)" must {
      "return a 'call' to 'SchemeEstablishedCountryController' page" in {
        val result = navigator.nextPage(SchemeEstablishedCountryId, NormalMode)(emptyAnswers)
        result mustEqual controllers.register.routes.SchemeEstablishedCountryController.onPageLoad(NormalMode)
      }
    }
  }
}