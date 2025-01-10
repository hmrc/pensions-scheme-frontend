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

class EstablisherPartnershipAddressNavigatorSpec extends SpecBase with Matchers with NavigatorBehaviour with Generators {

  import EstablisherPartnershipAddressNavigatorSpec._

  val navigator: Navigator = injector.instanceOf[EstablisherPartnershipAddressNavigator]

  "NormalMode" must {
    val normalModeRoutes: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Next Page"),
        row(PartnershipPostcodeLookupId(index))(Seq(someTolerantAddress), addressListPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnershipAddressListId(index))(someTolerantAddress, addressYearsPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnershipAddressId(index))(someAddress, addressYearsPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnershipAddressYearsId(index))(AddressYears.UnderAYear, hasBeenTradingPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnershipAddressYearsId(index))(AddressYears.OverAYear, cyaAddressPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnershipHasBeenTradingId(index))(value = true, previousAddressPostcodeLookupPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnershipHasBeenTradingId(index))(value = false, cyaAddressPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnershipPreviousAddressPostcodeLookupId(index))(Seq(someTolerantAddress), previousAddressListPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnershipPreviousAddressListId(index))(someTolerantAddress, cyaAddressPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnershipPreviousAddressId(index))(someAddress, cyaAddressPage(NormalMode, EmptyOptionalSchemeReferenceNumber))
      )

    behave like navigatorWithRoutesForMode(NormalMode)(navigator, normalModeRoutes, EmptyOptionalSchemeReferenceNumber)
  }

  "CheckMode" must {
    val checkModeRoutes: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Next Page"),
        row(PartnershipPostcodeLookupId(index))(Seq(someTolerantAddress), addressListPage(CheckMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnershipAddressListId(index))(someTolerantAddress, cyaAddressPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnershipAddressId(index))(someAddress, cyaAddressPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnershipAddressYearsId(index))(AddressYears.UnderAYear, hasBeenTradingPage(CheckMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnershipAddressYearsId(index))(AddressYears.OverAYear, cyaAddressPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnershipPreviousAddressPostcodeLookupId(index))(Seq(someTolerantAddress), previousAddressListPage(CheckMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnershipPreviousAddressListId(index))(someTolerantAddress, cyaAddressPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnershipPreviousAddressId(index))(someAddress, cyaAddressPage(NormalMode, EmptyOptionalSchemeReferenceNumber))
      )

    behave like navigatorWithRoutesForMode(CheckMode)(navigator, checkModeRoutes, EmptyOptionalSchemeReferenceNumber)
  }

  "UpdateMode" must {
    val updateModeRoutes: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Next page"),
        row(PartnershipPostcodeLookupId(index))(Seq(someTolerantAddress), addressListPage(UpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnershipAddressListId(index))(someTolerantAddress, addressYearsPage(UpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnershipAddressId(index))(someAddress, addressYearsPage(UpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnershipAddressYearsId(index))(AddressYears.UnderAYear, hasBeenTradingPage(UpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnershipAddressYearsId(index))(AddressYears.OverAYear, cyaAddressPage(UpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnershipHasBeenTradingId(index))(value = true, previousAddressPostcodeLookupPage(UpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnershipHasBeenTradingId(index))(value = false, cyaAddressPage(UpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnershipPreviousAddressPostcodeLookupId(index))(Seq(someTolerantAddress), previousAddressListPage(UpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnershipPreviousAddressListId(index))(someTolerantAddress, cyaAddressPage(UpdateMode, OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers)),
        row(PartnershipPreviousAddressId(index))(someAddress, cyaAddressPage(UpdateMode, OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers))
      )

    behave like navigatorWithRoutesForMode(UpdateMode)(navigator, updateModeRoutes, OptionalSchemeReferenceNumber(srn))
  }

  "CheckUpdateMode" must {
    val checkUpdateModeRoutes: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Next page"),
        row(PartnershipPostcodeLookupId(index))(Seq(someTolerantAddress), addressListPage(CheckUpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnershipAddressListId(index))(someTolerantAddress, cyaAddressPage(UpdateMode, OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers)),
        row(PartnershipAddressListId(index))(someTolerantAddress, isThisPreviousAddressPage(OptionalSchemeReferenceNumber(srn))),
        row(PartnershipAddressId(index))(someAddress, cyaAddressPage(UpdateMode, OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers)),
        row(PartnershipAddressId(index))(someAddress, isThisPreviousAddressPage(OptionalSchemeReferenceNumber(srn))),
        row(PartnershipAddressYearsId(index))(AddressYears.UnderAYear, hasBeenTradingPage(CheckUpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnershipAddressYearsId(index))(AddressYears.OverAYear, cyaAddressPage(UpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnershipPreviousAddressPostcodeLookupId(index))(Seq(someTolerantAddress), previousAddressListPage(CheckUpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnershipPreviousAddressListId(index))(someTolerantAddress, cyaAddressPage(UpdateMode, OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers)),
        row(PartnershipPreviousAddressListId(index))(someTolerantAddress, AnyMoreChangesController.onPageLoad(OptionalSchemeReferenceNumber(srn))),
        row(PartnershipPreviousAddressId(index))(someAddress, cyaAddressPage(UpdateMode, OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers)),
        row(PartnershipPreviousAddressId(index))(someAddress, AnyMoreChangesController.onPageLoad(OptionalSchemeReferenceNumber(srn)))
      )

    behave like navigatorWithRoutesForMode(CheckUpdateMode)(navigator, checkUpdateModeRoutes, OptionalSchemeReferenceNumber(srn))
  }


}

object EstablisherPartnershipAddressNavigatorSpec extends SpecBase with Matchers with NavigatorBehaviour with Generators {
  private lazy val index = 0
  private val srn = Some(SchemeReferenceNumber("srn"))
  private val newEstablisherUserAnswers: UserAnswers = UserAnswers().set(IsEstablisherNewId(index))(value = true).asOpt.value

  private def addressYearsPage(mode: Mode, srn: OptionalSchemeReferenceNumber): Call = PartnershipAddressYearsController.onPageLoad(mode, Index(0), OptionalSchemeReferenceNumber(srn))

  private def addressListPage(mode: Mode, srn: OptionalSchemeReferenceNumber): Call = PartnershipAddressListController.onPageLoad(mode, Index(0), OptionalSchemeReferenceNumber(srn))

  private def previousAddressPostcodeLookupPage(mode: Mode, srn: OptionalSchemeReferenceNumber): Call =
    PartnershipPreviousAddressPostcodeLookupController.onPageLoad(mode, Index(0), OptionalSchemeReferenceNumber(srn))

  private def cyaAddressPage(mode: Mode, srn: OptionalSchemeReferenceNumber): Call = CheckYourAnswersPartnershipAddressController.onPageLoad(mode, Index(0), OptionalSchemeReferenceNumber(srn))

  private def isThisPreviousAddressPage(srn: OptionalSchemeReferenceNumber): Call = PartnershipConfirmPreviousAddressController.onPageLoad(Index(0), OptionalSchemeReferenceNumber(srn))

  private def previousAddressListPage(mode: Mode, srn: OptionalSchemeReferenceNumber): Call = PartnershipPreviousAddressListController.onPageLoad(mode, Index(0), OptionalSchemeReferenceNumber(srn))

  private def hasBeenTradingPage(mode: Mode, srn: OptionalSchemeReferenceNumber): Call = PartnershipHasBeenTradingController.onPageLoad(mode, Index(0), OptionalSchemeReferenceNumber(srn))
}




