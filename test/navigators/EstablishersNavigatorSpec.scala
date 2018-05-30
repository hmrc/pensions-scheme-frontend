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
import identifiers.register.establishers.EstablisherKindId
import models.NormalMode
import models.register.establishers.EstablisherKind
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.{Enumerable, UserAnswers}

class EstablishersNavigatorSpec extends SpecBase with MustMatchers with NavigatorBehaviour {

  import EstablishersNavigatorSpec._
  private val navigator = new EstablishersNavigator(frontendAppConfig)

  private val routes = Table(
    ("Id",                          "User Answers",       "Next Page (Normal Mode)",              "Next Page (Check Mode)"),
    (EstablisherKindId(0),          company,              companyDetails,                         None: Option[Call]),
    (EstablisherKindId(0),          individual,           individualDetails,                      None),
    (EstablisherKindId(0),          emptyAnswers,         expired,                                None)
  )

  navigator.getClass.getSimpleName must {
    behave like navigatorWithRoutes(navigator, routes, dataDescriber)
  }

}

object EstablishersNavigatorSpec extends OptionValues with Enumerable.Implicits {

  private val emptyAnswers = UserAnswers(Json.obj())
  private val company = UserAnswers().set(EstablisherKindId(0))(EstablisherKind.Company).asOpt.value
  private val individual = UserAnswers().set(EstablisherKindId(0))(EstablisherKind.Indivdual).asOpt.value

  private def companyDetails = controllers.register.establishers.company.routes.CompanyDetailsController.onPageLoad(NormalMode, 0)
  private def individualDetails = controllers.register.establishers.individual.routes.EstablisherDetailsController.onPageLoad(NormalMode, 0)

  private def expired = controllers.routes.SessionExpiredController.onPageLoad()

  private def dataDescriber(answers: UserAnswers): String = answers.toString

}
