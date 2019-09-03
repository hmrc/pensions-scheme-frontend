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
import controllers.register.trustees.partnership.routes._
import controllers.routes.AnyMoreChangesController
import generators.Generators
import identifiers.Identifier
import identifiers.register.trustees.IsTrusteeNewId
import identifiers.register.trustees.partnership._
import models._
import navigators.{Navigator, NavigatorBehaviour}
import org.scalatest.MustMatchers
import org.scalatest.prop._
import play.api.mvc.Call
import utils.UserAnswers

class TrusteesPartnershipAddressNavigatorSpec extends SpecBase with MustMatchers with NavigatorBehaviour with Generators {

  import TrusteesPartnershipAddressNavigatorSpec._

  val navigator: Navigator = injector.instanceOf[TrusteesPartnershipAddressNavigator]

  "NormalMode" must {
    val normalModeRoutes: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Next Page"),
        row(PartnershipPostcodeLookupId(index))(Seq(someTolerantAddress), addressListPage(NormalMode, None)),
        row(PartnershipAddressListId(index))(someTolerantAddress, addressPage(NormalMode, None)),
        row(PartnershipAddressId(index))(someAddress, addressYearsPage(NormalMode, None)),
        row(PartnershipAddressYearsId(index))(AddressYears.UnderAYear, hasBeenTradingPage(NormalMode, None)),
        row(PartnershipAddressYearsId(index))(AddressYears.OverAYear, cyaAddressPage(NormalMode, None)),
        row(PartnershipHasBeenTradingId(index))(value = true, previousAddressPostcodeLookupPage(NormalMode, None)),
        row(PartnershipHasBeenTradingId(index))(value = false, cyaAddressPage(NormalMode, None)),
        row(PartnershipPreviousAddressPostcodeLookupId(index))(Seq(someTolerantAddress), previousAddressListPage(NormalMode, None)),
        row(PartnershipPreviousAddressListId(index))(someTolerantAddress, previousAddressPage(NormalMode, None)),
        row(PartnershipPreviousAddressId(index))(someAddress, cyaAddressPage(NormalMode, None))
      )

    behave like navigatorWithRoutesForMode(NormalMode)(navigator, normalModeRoutes, None)
  }

  "CheckMode" must {
    val checkModeRoutes: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Next Page"),
        row(PartnershipPostcodeLookupId(index))(Seq(someTolerantAddress), addressListPage(CheckMode, None)),
        row(PartnershipAddressListId(index))(someTolerantAddress, addressPage(CheckMode, None)),
        row(PartnershipAddressId(index))(someAddress, cyaAddressPage(NormalMode, None)),
        row(PartnershipAddressYearsId(index))(AddressYears.UnderAYear, hasBeenTradingPage(CheckMode, None)),
        row(PartnershipAddressYearsId(index))(AddressYears.OverAYear, cyaAddressPage(NormalMode, None)),
        row(PartnershipPreviousAddressPostcodeLookupId(index))(Seq(someTolerantAddress), previousAddressListPage(CheckMode, None)),
        row(PartnershipPreviousAddressListId(index))(someTolerantAddress, previousAddressPage(CheckMode, None)),
        row(PartnershipPreviousAddressId(index))(someAddress, cyaAddressPage(NormalMode, None))
      )

    behave like navigatorWithRoutesForMode(CheckMode)(navigator, checkModeRoutes, None)
  }

  "UpdateMode" must {
    val updateModeRoutes: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Next page"),
        row(PartnershipPostcodeLookupId(index))(Seq(someTolerantAddress), addressListPage(UpdateMode, srn)),
        row(PartnershipAddressListId(index))(someTolerantAddress, addressPage(UpdateMode, srn)),
        row(PartnershipAddressId(index))(someAddress, addressYearsPage(UpdateMode, srn)),
        row(PartnershipAddressYearsId(index))(AddressYears.UnderAYear, hasBeenTradingPage(UpdateMode, srn)),
        row(PartnershipAddressYearsId(index))(AddressYears.OverAYear, cyaAddressPage(UpdateMode, srn)),
        row(PartnershipHasBeenTradingId(index))(value = true, previousAddressPostcodeLookupPage(UpdateMode, srn)),
        row(PartnershipHasBeenTradingId(index))(value = false, cyaAddressPage(UpdateMode, srn)),
        row(PartnershipPreviousAddressPostcodeLookupId(index))(Seq(someTolerantAddress), previousAddressListPage(UpdateMode, srn)),
        row(PartnershipPreviousAddressListId(index))(someTolerantAddress, previousAddressPage(UpdateMode, srn)),
        row(PartnershipPreviousAddressId(index))(someAddress, cyaAddressPage(UpdateMode, srn), Some(newTrusteeUserAnswers))
      )

    behave like navigatorWithRoutesForMode(UpdateMode)(navigator, updateModeRoutes, srn)
  }

  "CheckUpdateMode" must {
    val checkUpdateModeRoutes: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Next page"),
        row(PartnershipPostcodeLookupId(index))(Seq(someTolerantAddress), addressListPage(CheckUpdateMode, srn)),
        row(PartnershipAddressListId(index))(someTolerantAddress, addressPage(CheckUpdateMode, srn)),
        row(PartnershipAddressId(index))(someAddress, cyaAddressPage(UpdateMode, srn), Some(newTrusteeUserAnswers)),
        row(PartnershipAddressId(index))(someAddress, isThisPreviousAddressPage(srn)),
        row(PartnershipAddressYearsId(index))(AddressYears.UnderAYear, hasBeenTradingPage(CheckUpdateMode, srn)),
        row(PartnershipAddressYearsId(index))(AddressYears.OverAYear, cyaAddressPage(UpdateMode, srn)),
        row(PartnershipPreviousAddressPostcodeLookupId(index))(Seq(someTolerantAddress), previousAddressListPage(CheckUpdateMode, srn)),
        row(PartnershipPreviousAddressListId(index))(someTolerantAddress, previousAddressPage(CheckUpdateMode, srn)),
        row(PartnershipPreviousAddressId(index))(someAddress, cyaAddressPage(UpdateMode, srn), Some(newTrusteeUserAnswers)),
        row(PartnershipPreviousAddressId(index))(someAddress, AnyMoreChangesController.onPageLoad(srn))
      )

    behave like navigatorWithRoutesForMode(CheckUpdateMode)(navigator, checkUpdateModeRoutes, srn)
  }


}

object TrusteesPartnershipAddressNavigatorSpec extends SpecBase with MustMatchers with NavigatorBehaviour with Generators {
  private lazy val index = 0
  private val srn = Some("srn")
  private val newTrusteeUserAnswers: UserAnswers = UserAnswers().set(IsTrusteeNewId(index))(value = true).asOpt.value

  private def addressYearsPage(mode: Mode, srn: Option[String]): Call = PartnershipAddressYearsController.onPageLoad(mode, index, srn)

  private def addressListPage(mode: Mode, srn: Option[String]): Call = PartnershipAddressListController.onPageLoad(mode, index, srn)

  private def addressPage(mode: Mode, srn: Option[String]): Call = PartnershipAddressController.onPageLoad(mode, index, srn)

  private def previousAddressPostcodeLookupPage(mode: Mode, srn: Option[String]): Call =
    PartnershipPreviousAddressPostcodeLookupController.onPageLoad(mode, index, srn)

  private def cyaAddressPage(mode: Mode, srn: Option[String]): Call = CheckYourAnswersPartnershipAddressController.onPageLoad(mode, index, srn)

  private def isThisPreviousAddressPage(srn: Option[String]): Call = PartnershipConfirmPreviousAddressController.onPageLoad(index, srn)

  private def previousAddressListPage(mode: Mode, srn: Option[String]): Call = PartnershipPreviousAddressListController.onPageLoad(mode, index, srn)

  private def previousAddressPage(mode: Mode, srn: Option[String]): Call = PartnershipPreviousAddressController.onPageLoad(mode, index, srn)

  private def hasBeenTradingPage(mode: Mode, srn: Option[String]): Call = PartnershipHasBeenTradingController.onPageLoad(mode, index, srn)
}




