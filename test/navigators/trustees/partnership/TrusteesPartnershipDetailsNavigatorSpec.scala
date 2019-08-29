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
import controllers.actions.FakeDataRetrievalAction
import generators.Generators
import identifiers.Identifier
import identifiers.register.trustees.IsTrusteeNewId
import identifiers.register.trustees.partnership._
import models._
import navigators.{Navigator, NavigatorBehaviour}
import org.scalatest.{MustMatchers, OptionValues}
import org.scalatest.prop._
import play.api.mvc.Call
import utils.UserAnswers
import controllers.register.trustees.partnership.routes._
import controllers.register.trustees.routes._
import play.api.libs.json.Json

class TrusteesPartnershipDetailsNavigatorSpec extends SpecBase with MustMatchers with NavigatorBehaviour with Generators {

  import TrusteesPartnershipDetailsNavigatorSpec._

  val navigator: Navigator =
    applicationBuilder(dataRetrievalAction = new FakeDataRetrievalAction(Some(Json.obj())), featureSwitchEnabled = true).build().injector.instanceOf[Navigator]

  "TrusteesPartnershipDetailsNavigator" when {
    "in NormalMode" must {
      def navigationForTrusteePartnership(mode: Mode): TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Next Page"),
          row(PartnershipDetailsId(index))(partnershipDetails, addTrusteesPage(mode, None)),
          row(PartnershipHasUTRId(index))(true, PartnershipUTRController.onPageLoad(NormalMode, index, None)),
          row(PartnershipHasUTRId(index))(false, PartnershipNoUTRReasonController.onPageLoad(NormalMode, index, None)),
          row(PartnershipNoUTRReasonId(index))(someStringValue, hasVatPage(NormalMode, index, None)),
          row(PartnershipUTRId(index))(someRefValue, hasVatPage(NormalMode, index, None)),
          row(PartnershipHasVATId(index))(true, PartnershipEnterVATController.onPageLoad(NormalMode, index, None)),
          row(PartnershipHasVATId(index))(false, HasPartnershipPAYEController.onPageLoad(NormalMode, index, None)),
          row(PartnershipEnterVATId(index))(someRefValue, HasPartnershipPAYEController.onPageLoad(NormalMode, index, None))
        )

      behave like navigatorWithRoutesForMode(NormalMode)(navigator, navigationForTrusteePartnership(NormalMode), None)
    }

    "CheckMode" must {
      val checkModeRoutes: TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Expected next page"),
          row(PartnershipHasUTRId(index))(true, PartnershipUTRController.onPageLoad(CheckMode, index, None)),
          row(PartnershipHasUTRId(index))(false, PartnershipNoUTRReasonController.onPageLoad(CheckMode, index, None)),
          row(PartnershipNoUTRReasonId(index))(someStringValue, cyaPartnershipDetailsPage(CheckMode, index, None)),
          row(PartnershipUTRId(index))(someRefValue, cyaPartnershipDetailsPage(CheckMode, index, None)),
          row(PartnershipHasVATId(index))(true, PartnershipEnterVATController.onPageLoad(CheckMode, index, None)),
          row(PartnershipHasVATId(index))(false, cyaPartnershipDetailsPage(CheckMode, index, None)),
          row(PartnershipEnterVATId(index))(someRefValue, cyaPartnershipDetailsPage(CheckMode, index, None))
        )

      behave like navigatorWithRoutesForMode(CheckMode)(navigator, checkModeRoutes, None)
    }

    "in UpdateMode" must {
      def navigationForUpdateModeTrusteePartnership(mode: Mode): TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Expected next page"),
          row(PartnershipDetailsId(index))(partnershipDetails, addTrusteesPage(mode, srn), Some(newTrusteeUserAnswers)),
          row(PartnershipHasUTRId(index))(true, PartnershipUTRController.onPageLoad(UpdateMode, index, srn), Some(newTrusteeUserAnswers)),
          row(PartnershipHasUTRId(index))(false, PartnershipNoUTRReasonController.onPageLoad(UpdateMode, index, srn), Some(newTrusteeUserAnswers)),
          row(PartnershipNoUTRReasonId(index))(someStringValue, hasVatPage(UpdateMode, index, srn), Some(newTrusteeUserAnswers)),
          row(PartnershipUTRId(index))(someRefValue, hasVatPage(UpdateMode, index, srn), Some(newTrusteeUserAnswers)),
          row(PartnershipHasVATId(index))(true, PartnershipEnterVATController.onPageLoad(UpdateMode, index, srn)),
          row(PartnershipHasVATId(index))(false, HasPartnershipPAYEController.onPageLoad(UpdateMode, index, srn)),
          row(PartnershipEnterVATId(index))(someRefValue, HasPartnershipPAYEController.onPageLoad(UpdateMode, index, srn))
        )

      behave like navigatorWithRoutesForMode(UpdateMode)(navigator, navigationForUpdateModeTrusteePartnership(UpdateMode), srn)
    }

    "CheckUpdateMode" must {
      val navigationForVarianceModeTrusteeIndividual: TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Expected next page"),
          row(PartnershipHasUTRId(index))(true, PartnershipUTRController.onPageLoad(CheckUpdateMode, index, srn), Some(newTrusteeUserAnswers)),
          row(PartnershipHasUTRId(index))(false, PartnershipNoUTRReasonController.onPageLoad(CheckUpdateMode, index, srn), Some(newTrusteeUserAnswers)),
          row(PartnershipUTRId(index))(someRefValue, cyaPartnershipDetailsPage(CheckUpdateMode, index, srn), Some(newTrusteeUserAnswers)),
          row(PartnershipUTRId(index))(someRefValue, anyMoreChangesPage(srn), Some(existingTrusteeUserAnswers)),
          row(PartnershipNoUTRReasonId(index))(someStringValue, cyaPartnershipDetailsPage(CheckUpdateMode, index, srn), Some(newTrusteeUserAnswers)),
          row(PartnershipHasVATId(index))(true, PartnershipEnterVATController.onPageLoad(CheckUpdateMode, index, srn)),
          row(PartnershipHasVATId(index))(false, cyaPartnershipDetailsPage(CheckUpdateMode, index, srn)),
          row(PartnershipEnterVATId(index))(someRefValue, cyaPartnershipDetailsPage(CheckUpdateMode, index, srn), Some(newTrusteeUserAnswers)),
          row(PartnershipEnterVATId(index))(someRefValue, anyMoreChangesPage(srn), Some(existingTrusteeUserAnswers))
        )

      behave like navigatorWithRoutesForMode(CheckUpdateMode)(navigator, navigationForVarianceModeTrusteeIndividual, srn)
    }
  }

}

object TrusteesPartnershipDetailsNavigatorSpec extends OptionValues {
  private lazy val index = 0
  private val srn = Some("srn")
  private val newTrusteeUserAnswers = UserAnswers().set(IsTrusteeNewId(index))(true).asOpt.value
  private val existingTrusteeUserAnswers = UserAnswers().set(IsTrusteeNewId(index))(false).asOpt.value
  private val partnershipDetails = PartnershipDetails("test partnership")

  private def addTrusteesPage(mode: Mode, srn: Option[String]): Call =
    AddTrusteeController.onPageLoad(Mode.journeyMode(mode), srn)

  private def hasVatPage(mode: Mode, index: Index, srn: Option[String]): Call =
    PartnershipHasVATController.onPageLoad(Mode.journeyMode(mode), index, srn)

  private def cyaPartnershipDetailsPage(mode: Mode, index: Index, srn: Option[String]): Call =
    CheckYourAnswersPartnershipDetailsController.onPageLoad(Mode.journeyMode(mode), index, srn)
}

