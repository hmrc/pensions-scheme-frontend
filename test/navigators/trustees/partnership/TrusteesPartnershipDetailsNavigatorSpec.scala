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

package navigators.trustees.partnership

import base.SpecBase
import generators.Generators
import identifiers.Identifier
import identifiers.register.trustees.partnership.PartnershipDetailsId
import models._
import navigators.{Navigator, NavigatorBehaviour}
import org.scalatest.MustMatchers
import org.scalatest.prop._
import play.api.mvc.Call
import utils.UserAnswers

class TrusteesPartnershipDetailsNavigatorSpec extends SpecBase with MustMatchers with NavigatorBehaviour with Generators {

  import TrusteesPartnershipDetailsNavigatorSpec._

  val navigator: Navigator = injector.instanceOf[TrusteesPartnershipDetailsNavigator]

  "TrusteesPartnershipDetailsNavigator" when {
    "in NormalMode" must {
      def navigationForTrusteePartnership(mode: Mode): TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Next Page"),
          row(PartnershipDetailsId(index))(partnershipDetails, controllers.register.trustees.routes.AddTrusteeController.onPageLoad(mode, None))
        )

      behave like navigatorWithRoutesForMode(NormalMode)(navigator, navigationForTrusteePartnership(NormalMode), None)
    }
  }

}

object TrusteesPartnershipDetailsNavigatorSpec {
  private lazy val index = 0
  private val partnershipDetails = PartnershipDetails("test partnership")
}

