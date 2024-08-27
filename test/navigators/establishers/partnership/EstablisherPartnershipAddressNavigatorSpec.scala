/*
 * Copyright 2024 HM Revenue & Customs
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
import controllers.ControllerSpecBase
import controllers.register.establishers.partnership.routes._
import controllers.routes.AnyMoreChangesController
import generators.Generators
import identifiers.Identifier
import identifiers.register.establishers.IsEstablisherNewId
import identifiers.register.establishers.partnership._
import models._
import navigators.{Navigator, NavigatorBehaviour}
import org.scalatest.matchers.must.Matchers
import org.scalatest.prop._
import play.api.mvc.Call
import utils.UserAnswers

class EstablisherPartnershipAddressNavigatorSpec extends SpecBase with Matchers with NavigatorBehaviour with Generators with ControllerSpecBase {

  import EstablisherPartnershipAddressNavigatorSpec._

  val navigator: Navigator = injector.instanceOf[EstablisherPartnershipAddressNavigator]

  "NormalMode" must {
    val normalModeRoutes: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Next Page"),
        row(PartnershipPostcodeLookupId(index))(Seq(someTolerantAddress), addressListPage(NormalMode, srn)),
        row(PartnershipAddressListId(index))(someTolerantAddress, addressYearsPage(NormalMode, srn)),
        row(PartnershipAddressId(index))(someAddress, addressYearsPage(NormalMode, srn)),
        row(PartnershipAddressYearsId(index))(AddressYears.UnderAYear, hasBeenTradingPage(NormalMode, srn)),
        row(PartnershipAddressYearsId(index))(AddressYears.OverAYear, cyaAddressPage(NormalMode, srn)),
        row(PartnershipHasBeenTradingId(index))(value = true, previousAddressPostcodeLookupPage(NormalMode, srn)),
        row(PartnershipHasBeenTradingId(index))(value = false, cyaAddressPage(NormalMode, srn)),
        row(PartnershipPreviousAddressPostcodeLookupId(index))(Seq(someTolerantAddress), previousAddressListPage(NormalMode, srn)),
        row(PartnershipPreviousAddressListId(index))(someTolerantAddress, cyaAddressPage(NormalMode, srn)),
        row(PartnershipPreviousAddressId(index))(someAddress, cyaAddressPage(NormalMode, srn))
      )

    behave like navigatorWithRoutesForMode(NormalMode)(navigator, normalModeRoutes, srn)
  }

  "CheckMode" must {
    val checkModeRoutes: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Next Page"),
        row(PartnershipPostcodeLookupId(index))(Seq(someTolerantAddress), addressListPage(CheckMode, srn)),
        row(PartnershipAddressListId(index))(someTolerantAddress, cyaAddressPage(NormalMode, srn)),
        row(PartnershipAddressId(index))(someAddress, cyaAddressPage(NormalMode, srn)),
        row(PartnershipAddressYearsId(index))(AddressYears.UnderAYear, hasBeenTradingPage(CheckMode, srn)),
        row(PartnershipAddressYearsId(index))(AddressYears.OverAYear, cyaAddressPage(NormalMode, srn)),
        row(PartnershipPreviousAddressPostcodeLookupId(index))(Seq(someTolerantAddress), previousAddressListPage(CheckMode, srn)),
        row(PartnershipPreviousAddressListId(index))(someTolerantAddress, cyaAddressPage(NormalMode, srn)),
        row(PartnershipPreviousAddressId(index))(someAddress, cyaAddressPage(NormalMode, srn))
      )

    behave like navigatorWithRoutesForMode(CheckMode)(navigator, checkModeRoutes, srn)
  }

  "UpdateMode" must {
    val updateModeRoutes: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Next page"),
        row(PartnershipPostcodeLookupId(index))(Seq(someTolerantAddress), addressListPage(UpdateMode, srn)),
        row(PartnershipAddressListId(index))(someTolerantAddress, addressYearsPage(UpdateMode, srn)),
        row(PartnershipAddressId(index))(someAddress, addressYearsPage(UpdateMode, srn)),
        row(PartnershipAddressYearsId(index))(AddressYears.UnderAYear, hasBeenTradingPage(UpdateMode, srn)),
        row(PartnershipAddressYearsId(index))(AddressYears.OverAYear, cyaAddressPage(UpdateMode, srn)),
        row(PartnershipHasBeenTradingId(index))(value = true, previousAddressPostcodeLookupPage(UpdateMode, srn)),
        row(PartnershipHasBeenTradingId(index))(value = false, cyaAddressPage(UpdateMode, srn)),
        row(PartnershipPreviousAddressPostcodeLookupId(index))(Seq(someTolerantAddress), previousAddressListPage(UpdateMode, srn)),
        row(PartnershipPreviousAddressListId(index))(someTolerantAddress, cyaAddressPage(UpdateMode, srn), Some(newEstablisherUserAnswers)),
        row(PartnershipPreviousAddressId(index))(someAddress, cyaAddressPage(UpdateMode, srn), Some(newEstablisherUserAnswers))
      )

    behave like navigatorWithRoutesForMode(UpdateMode)(navigator, updateModeRoutes, srn)
  }

  "CheckUpdateMode" must {
    val checkUpdateModeRoutes: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Next page"),
        row(PartnershipPostcodeLookupId(index))(Seq(someTolerantAddress), addressListPage(CheckUpdateMode, srn)),
        row(PartnershipAddressListId(index))(someTolerantAddress, cyaAddressPage(UpdateMode, srn), Some(newEstablisherUserAnswers)),
        row(PartnershipAddressListId(index))(someTolerantAddress, isThisPreviousAddressPage(srn)),
        row(PartnershipAddressId(index))(someAddress, cyaAddressPage(UpdateMode, srn), Some(newEstablisherUserAnswers)),
        row(PartnershipAddressId(index))(someAddress, isThisPreviousAddressPage(srn)),
        row(PartnershipAddressYearsId(index))(AddressYears.UnderAYear, hasBeenTradingPage(CheckUpdateMode, srn)),
        row(PartnershipAddressYearsId(index))(AddressYears.OverAYear, cyaAddressPage(UpdateMode, srn)),
        row(PartnershipPreviousAddressPostcodeLookupId(index))(Seq(someTolerantAddress), previousAddressListPage(CheckUpdateMode, srn)),
        row(PartnershipPreviousAddressListId(index))(someTolerantAddress, cyaAddressPage(UpdateMode, srn), Some(newEstablisherUserAnswers)),
        row(PartnershipPreviousAddressListId(index))(someTolerantAddress, AnyMoreChangesController.onPageLoad(srn)),
        row(PartnershipPreviousAddressId(index))(someAddress, cyaAddressPage(UpdateMode, srn), Some(newEstablisherUserAnswers)),
        row(PartnershipPreviousAddressId(index))(someAddress, AnyMoreChangesController.onPageLoad(srn))
      )

    behave like navigatorWithRoutesForMode(CheckUpdateMode)(navigator, checkUpdateModeRoutes, srn)
  }


}

object EstablisherPartnershipAddressNavigatorSpec extends SpecBase with Matchers with NavigatorBehaviour with Generators {
  private lazy val index = 0
  private val newEstablisherUserAnswers: UserAnswers = UserAnswers().set(IsEstablisherNewId(index))(value = true).asOpt.value

  private def addressYearsPage(mode: Mode, srn: SchemeReferenceNumber): Call = PartnershipAddressYearsController.onPageLoad(mode, index, srn)

  private def addressListPage(mode: Mode, srn: SchemeReferenceNumber): Call = PartnershipAddressListController.onPageLoad(mode, index, srn)

  private def previousAddressPostcodeLookupPage(mode: Mode, srn: SchemeReferenceNumber): Call =
    PartnershipPreviousAddressPostcodeLookupController.onPageLoad(mode, index, srn)

  private def cyaAddressPage(mode: Mode, srn: SchemeReferenceNumber): Call = CheckYourAnswersPartnershipAddressController.onPageLoad(mode, index, srn)

  private def isThisPreviousAddressPage(srn: SchemeReferenceNumber): Call = PartnershipConfirmPreviousAddressController.onPageLoad(index, srn)

  private def previousAddressListPage(mode: Mode, srn: SchemeReferenceNumber): Call = PartnershipPreviousAddressListController.onPageLoad(mode, index, srn)

  private def hasBeenTradingPage(mode: Mode, srn: SchemeReferenceNumber): Call = PartnershipHasBeenTradingController.onPageLoad(mode, index, srn)
}




