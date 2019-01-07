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

package navigators

import base.SpecBase
import connectors.FakeUserAnswersCacheConnector
import identifiers.EstablishedCountryId
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.UserAnswers

class BeforeYouStartNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import BeforeYouStartNavigatorSpec._

  private def routes() = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
    (EstablishedCountryId, emptyAnswers, indexPage, false, None, false)
  )

  "BeforeYouStartNavigator" must {
    val navigator = new BeforeYouStartNavigator(FakeUserAnswersCacheConnector, frontendAppConfig)

    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, routes(), dataDescriber)
    behave like nonMatchingNavigator(navigator)
  }
}

object BeforeYouStartNavigatorSpec {

  private val emptyAnswers = UserAnswers(Json.obj())
  private def dataDescriber(answers: UserAnswers): String = answers.toString
  private def indexPage: Call = controllers.routes.IndexController.onPageLoad()
}



