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
import identifiers.{CurrentMembersId, FutureMembersId, MembershipPensionRegulatorId}
import models.{Members, Mode, NormalMode}
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.UserAnswers

class AboutMembersNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import AboutMembersNavigatorSpec._

  private def routes() = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
    (CurrentMembersId, noMembers, futureMembers(NormalMode), false, Some(cya), false),
    (CurrentMembersId, oneMember, futureMembers(NormalMode), false, Some(cya), false),
    (CurrentMembersId, twoToElevenMembers, membershipPensionRegulator, false, Some(membershipPensionRegulator), false),
    (CurrentMembersId, twelveToFiftyMembers, membershipPensionRegulator, false, Some(membershipPensionRegulator), false),
    (CurrentMembersId, fiftyOneToTenThousandMembers, membershipPensionRegulator, false, Some(membershipPensionRegulator), false),
    (CurrentMembersId, moreThanTenThousandMembers, membershipPensionRegulator, false, Some(membershipPensionRegulator), false),
    (MembershipPensionRegulatorId, emptyAnswers, futureMembers(NormalMode), false, None, false),
    (FutureMembersId, emptyAnswers, cya, false, None, false)
  )

  private val navigator = new AboutMembersNavigator(FakeUserAnswersCacheConnector, frontendAppConfig)

  s"${navigator.getClass.getSimpleName}" must {
    appRunning()
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, routes, dataDescriber)
    behave like nonMatchingNavigator(navigator)
  }
}

object AboutMembersNavigatorSpec {

  private val emptyAnswers = UserAnswers(Json.obj())

  private val noMembers = UserAnswers().currentMembers(Members.None)
  private val oneMember = UserAnswers().currentMembers(Members.One)
  private val twoToElevenMembers = UserAnswers().currentMembers(Members.TwoToEleven)
  private val twelveToFiftyMembers = UserAnswers().currentMembers(Members.TwelveToFifty)
  private val fiftyOneToTenThousandMembers = UserAnswers().currentMembers(Members.FiftyOneToTenThousand)
  private val moreThanTenThousandMembers = UserAnswers().currentMembers(Members.MoreThanTenThousand)

  private def futureMembers(mode: Mode): Call = controllers.routes.FutureMembersController.onPageLoad(mode)

  private def membershipPensionRegulator: Call = controllers.routes.MembershipPensionRegulatorController.onPageLoad()

  private def cya: Call = controllers.routes.CheckYourAnswersMembersController.onPageLoad()

  private def dataDescriber(answers: UserAnswers): String = answers.toString
}


