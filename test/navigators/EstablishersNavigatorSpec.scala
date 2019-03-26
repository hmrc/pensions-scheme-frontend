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
import identifiers.register.establishers.{AddEstablisherId, ConfirmDeleteEstablisherId, EstablisherKindId}
import models.NormalMode
import models.register.establishers.EstablisherKind
import org.scalatest.{MustMatchers, OptionValues}
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.{Enumerable, UserAnswers}

class EstablishersNavigatorSpec extends SpecBase with MustMatchers with NavigatorBehaviour {

  import EstablishersNavigatorSpec._

  private def routes = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
    (AddEstablisherId(None), emptyAnswers, establisherKind, true, None: Option[Call], false),
    (AddEstablisherId(Some(true)), addEstablishersTrue, establisherKind, true, None: Option[Call], false),
    (AddEstablisherId(Some(false)), addEstablishersFalse, taskList, false, None: Option[Call], false),
    (EstablisherKindId(0), company, companyDetails, true, None: Option[Call], false),
    (EstablisherKindId(0), individual, individualDetails, true, None, false),
    (EstablisherKindId(0), partnership, partnershipDetails, true, None: Option[Call], false),
    (EstablisherKindId(0), emptyAnswers, expired, false, None, false),
    (ConfirmDeleteEstablisherId, emptyAnswers, addEstablisher, true, None, false)
  )
  private val navigator = new EstablishersNavigator(FakeUserAnswersCacheConnector, frontendAppConfig)

  s"${navigator.getClass.getSimpleName}" must {
    appRunning()
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, routes, dataDescriber)
    behave like nonMatchingNavigator(navigator)
  }
}

//noinspection MutatorLikeMethodIsParameterless
object EstablishersNavigatorSpec extends OptionValues with Enumerable.Implicits {

  private val emptyAnswers = UserAnswers(Json.obj())
  private val company = UserAnswers().set(EstablisherKindId(0))(EstablisherKind.Company).asOpt.value
  private val individual = UserAnswers().set(EstablisherKindId(0))(EstablisherKind.Indivdual).asOpt.value
  private val partnership = UserAnswers().set(EstablisherKindId(0))(EstablisherKind.Partnership).asOpt.value
  private val addEstablishersTrue = UserAnswers(Json.obj(AddEstablisherId.toString -> "true"))
  private val addEstablishersFalse = UserAnswers(Json.obj(AddEstablisherId.toString -> "false"))
  private val addEstablishersFalseWithNoScheme = UserAnswers(Json.obj(AddEstablisherId.toString -> "false"))

  private def companyDetails = controllers.register.establishers.company.routes.CompanyDetailsController.onPageLoad(NormalMode, None, 0)

  private def individualDetails = controllers.register.establishers.individual.routes.EstablisherDetailsController.onPageLoad(NormalMode, 0)

  private def partnershipDetails = controllers.register.establishers.partnership.routes.PartnershipDetailsController.onPageLoad(NormalMode, 0)

  private def establisherKind = controllers.register.establishers.routes.EstablisherKindController.onPageLoad(NormalMode, 0)

  private def addEstablisher = controllers.register.establishers.routes.AddEstablisherController.onPageLoad(NormalMode)

  private def expired = controllers.routes.SessionExpiredController.onPageLoad()

  private def taskList = controllers.routes.SchemeTaskListController.onPageLoad()

  private def dataDescriber(answers: UserAnswers): String = answers.toString

}
