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
import models.register.establishers.EstablisherKind
import models.{Mode, NormalMode, UpdateMode}
import org.scalatest.{MustMatchers, OptionValues}
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.{Enumerable, UserAnswers}

class EstablishersNavigatorSpec extends SpecBase with MustMatchers with NavigatorBehaviour {

  import EstablishersNavigatorSpec._

  private def routes(mode: Mode) = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
    (AddEstablisherId(None), emptyAnswers, establisherKind(mode), true, None: Option[Call], false),
    (AddEstablisherId(Some(true)), addEstablishersTrue, establisherKind(mode), true, None: Option[Call], false),
    (AddEstablisherId(Some(false)), addEstablishersFalse, taskList(mode), false, None: Option[Call], false),
    (EstablisherKindId(0), company, companyDetails(mode), true, None: Option[Call], false),
    (EstablisherKindId(0), individual, individualName(mode), true, None, false),
    (EstablisherKindId(0), partnership, partnershipDetails(mode), true, None: Option[Call], false),
    (EstablisherKindId(0), emptyAnswers, expired, false, None, false),
    (ConfirmDeleteEstablisherId, emptyAnswers, if(mode==UpdateMode) controllers.routes.AnyMoreChangesController.onPageLoad(None) else addEstablisher(mode), true, None, false)
  )

  "EstablishersNavigator" must {
    val navigator = new EstablishersNavigator(FakeUserAnswersCacheConnector, frontendAppConfig)
    appRunning()
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, routes(NormalMode), dataDescriber)
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, routes(UpdateMode), dataDescriber, UpdateMode)
    behave like nonMatchingNavigator(navigator)
    behave like nonMatchingNavigator(navigator, UpdateMode)
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

  private def companyDetails(mode: Mode) = controllers.register.establishers.company.routes.CompanyDetailsController.onPageLoad(mode, None, 0)

  private def individualDetails(mode: Mode) = controllers.register.establishers.individual.routes.EstablisherDetailsController.onPageLoad(mode, 0, None)

  private def individualName(mode: Mode) = controllers.register.establishers.individual.routes.EstablisherNameController.onPageLoad(mode, 0, None)

  private def partnershipDetails(mode: Mode) = controllers.register.establishers.partnership.routes.PartnershipDetailsController.onPageLoad(mode, 0, None)

  private def establisherKind(mode: Mode) = controllers.register.establishers.routes.EstablisherKindController.onPageLoad(mode, 0, None)

  private def addEstablisher(mode: Mode) = controllers.register.establishers.routes.AddEstablisherController.onPageLoad(mode, None)

  private def expired = controllers.routes.SessionExpiredController.onPageLoad()

  private def taskList(mode: Mode) = controllers.routes.SchemeTaskListController.onPageLoad(mode, None)

  private def dataDescriber(answers: UserAnswers): String = answers.toString

}
