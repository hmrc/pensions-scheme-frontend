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

package navigators

import base.SpecBase
import controllers.actions.FakeDataRetrievalAction
import identifiers._
import models._
import org.scalatest.prop.TableFor3
import play.api.libs.json.{JsString, Json, Writes}
import play.api.mvc.Call
import utils.{Enumerable, UserAnswers}

class AboutMembersNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import AboutMembersNavigatorSpec._

  val navigator: Navigator =
    applicationBuilder(dataRetrievalAction = new FakeDataRetrievalAction(Some(Json.obj()))).build().injector.instanceOf[Navigator]

  "in NormalMode" must {
    def navigation: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Next Page"),
        row(CurrentMembersId)(Members.None, futureMembers(NormalMode)),
        row(CurrentMembersId)(Members.One, futureMembers(NormalMode)),
        row(CurrentMembersId)(Members.TwoToEleven, membershipPensionRegulator(NormalMode)),
        row(CurrentMembersId)(Members.TwelveToFifty, membershipPensionRegulator(NormalMode)),
        row(CurrentMembersId)(Members.FiftyOneToTenThousand, membershipPensionRegulator(NormalMode)),
        row(CurrentMembersId)(Members.MoreThanTenThousand, membershipPensionRegulator(NormalMode)),
        rowNoValue(MembershipPensionRegulatorId)(futureMembers(NormalMode)),
        rowNoValue(FutureMembersId)(cya)
      )
    behave like navigatorWithRoutesForMode(NormalMode)(navigator, navigation, None)
  }

  "in CheckMode" must {
    def navigation: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Next Page"),
        row(CurrentMembersId)(Members.None, cya),
        row(CurrentMembersId)(Members.One, cya),
        row(CurrentMembersId)(Members.TwoToEleven, membershipPensionRegulator(CheckMode)),
        row(CurrentMembersId)(Members.TwelveToFifty, membershipPensionRegulator(CheckMode)),
        row(CurrentMembersId)(Members.FiftyOneToTenThousand, membershipPensionRegulator(CheckMode)),
        row(CurrentMembersId)(Members.MoreThanTenThousand, membershipPensionRegulator(CheckMode)),
        rowNoValue(MembershipPensionRegulatorId)(cya),
        rowNoValue(FutureMembersId)(cya)
      )
    behave like navigatorWithRoutesForMode(CheckMode)(navigator, navigation, None)
  }
}

object AboutMembersNavigatorSpec {
  private implicit def writes[A: Enumerable]: Writes[A] = Writes(value => JsString(value.toString))
  private def futureMembers(mode: Mode): Call              = controllers.routes.FutureMembersController.onPageLoad(mode)
  private def membershipPensionRegulator(mode: Mode): Call = controllers.routes.MembershipPensionRegulatorController.onPageLoad(mode)
  private def cya: Call                                    = controllers.routes.CheckYourAnswersMembersController.onPageLoad(NormalMode, None)
}
