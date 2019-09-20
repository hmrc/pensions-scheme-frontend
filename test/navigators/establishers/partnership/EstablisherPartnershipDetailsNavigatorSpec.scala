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

package navigators.establishers.partnership

import base.SpecBase
import generators.Generators
import identifiers.Identifier
import identifiers.register.establishers.partnership.PartnershipDetailsId
import models._
import navigators.{Navigator, NavigatorBehaviour}
import org.scalatest.MustMatchers
import org.scalatest.prop._
import play.api.mvc.Call
import utils.UserAnswers

class EstablisherPartnershipDetailsNavigatorSpec extends SpecBase with MustMatchers with NavigatorBehaviour with Generators {

  import EstablisherPartnershipDetailsNavigatorSpec._

  val navigator: Navigator =
    applicationBuilder(dataRetrievalAction = UserAnswers().dataRetrievalAction, featureSwitchEnabled = true).build().injector.instanceOf[Navigator]

  "EstablisherPartnershipDetailsNavigator" when {
    "in NormalMode" must {
      def normalModeRoutes: TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Next Page"),
          row(PartnershipDetailsId(index))(partnershipDetails, addEstablisherPage(NormalMode, None))
        )

      behave like navigatorWithRoutesForMode(NormalMode)(navigator, normalModeRoutes, None)
    }

    "in UpdateMode" must {
      def updateModeRoutes(): TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Next Page"),
          row(PartnershipDetailsId(index))(partnershipDetails, addEstablisherPage(UpdateMode, srn))
        )

      behave like navigatorWithRoutesForMode(UpdateMode)(navigator, updateModeRoutes(), srn)
    }
  }
}

object EstablisherPartnershipDetailsNavigatorSpec {
  private val index = 0
  private val srn = Some("test-srn")
  private val partnershipDetails = PartnershipDetails("test partnership")

  private def addEstablisherPage(mode: Mode, srn: Option[String]): Call =
    controllers.register.establishers.routes.AddEstablisherController.onPageLoad(mode, srn)
}



