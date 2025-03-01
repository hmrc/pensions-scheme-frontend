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

package navigators.trustees.individuals

import base.SpecBase
import controllers.register.trustees.individual.routes._
import controllers.routes.AnyMoreChangesController
import generators.Generators
import identifiers.Identifier
import identifiers.register.trustees.IsTrusteeNewId
import identifiers.register.trustees.individual._
import models.Mode._
import models._
import navigators.{Navigator, NavigatorBehaviour}
import org.scalatest.matchers.must.Matchers
import org.scalatest.prop._
import play.api.mvc.Call
import utils.UserAnswers

class TrusteesIndividualAddressNavigatorSpec extends SpecBase with Matchers with NavigatorBehaviour with Generators {

  import TrusteesIndividualAddressNavigatorSpec._

  val navigator: Navigator = injector.instanceOf[TrusteesIndividualAddressNavigator]

  "NormalMode" must {
    def normalModeRoutes(mode: Mode): TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Next Page"),
        row(IndividualPostCodeLookupId(index))(Seq(someTolerantAddress), IndividualAddressListController.onPageLoad(mode, Index(0), EmptyOptionalSchemeReferenceNumber)),
        row(IndividualAddressListId(index))(someTolerantAddress, TrusteeAddressYearsController.onPageLoad(mode, Index(0), EmptyOptionalSchemeReferenceNumber)),
        row(TrusteeAddressId(index))(someAddress, TrusteeAddressYearsController.onPageLoad(mode, Index(0), EmptyOptionalSchemeReferenceNumber)),
        row(TrusteeAddressYearsId(index))(AddressYears.UnderAYear, IndividualPreviousAddressPostcodeLookupController.onPageLoad(mode, Index(0), EmptyOptionalSchemeReferenceNumber)),
        row(TrusteeAddressYearsId(index))(AddressYears.OverAYear, CheckYourAnswersIndividualAddressController.onPageLoad(mode, Index(0), EmptyOptionalSchemeReferenceNumber)),
        row(IndividualPreviousAddressPostCodeLookupId(index))(Seq(someTolerantAddress), TrusteePreviousAddressListController.onPageLoad(mode, Index(0), EmptyOptionalSchemeReferenceNumber)),
        row(TrusteePreviousAddressListId(index))(someTolerantAddress, CheckYourAnswersIndividualAddressController.onPageLoad(mode, Index(0), EmptyOptionalSchemeReferenceNumber)),
        row(TrusteePreviousAddressId(index))(someAddress, CheckYourAnswersIndividualAddressController.onPageLoad(mode, Index(0), EmptyOptionalSchemeReferenceNumber))
      )

    behave like navigatorWithRoutesForMode(NormalMode)(navigator, normalModeRoutes(NormalMode), EmptyOptionalSchemeReferenceNumber)
  }

  "CheckMode" must {
    def checkModeRoutes(mode: Mode): TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Next Page"),
        row(IndividualPostCodeLookupId(index))(Seq(someTolerantAddress), IndividualAddressListController.onPageLoad(mode, Index(0), EmptyOptionalSchemeReferenceNumber)),
        row(IndividualAddressListId(index))(someTolerantAddress, CheckYourAnswersIndividualAddressController.onPageLoad(journeyMode(mode), Index(0), EmptyOptionalSchemeReferenceNumber)),
        row(TrusteeAddressId(index))(someAddress, CheckYourAnswersIndividualAddressController.onPageLoad(journeyMode(mode), Index(0), EmptyOptionalSchemeReferenceNumber)),
        row(TrusteeAddressYearsId(index))(AddressYears.UnderAYear, IndividualPreviousAddressPostcodeLookupController.onPageLoad(mode, Index(0), EmptyOptionalSchemeReferenceNumber)),
        row(TrusteeAddressYearsId(index))(AddressYears.OverAYear, CheckYourAnswersIndividualAddressController.onPageLoad(journeyMode(mode), Index(0), EmptyOptionalSchemeReferenceNumber)),
        row(IndividualPreviousAddressPostCodeLookupId(index))(Seq(someTolerantAddress), TrusteePreviousAddressListController.onPageLoad(mode, Index(0), EmptyOptionalSchemeReferenceNumber)),
        row(TrusteePreviousAddressListId(index))(someTolerantAddress, CheckYourAnswersIndividualAddressController.onPageLoad(journeyMode(mode), Index(0), EmptyOptionalSchemeReferenceNumber)),
        row(TrusteePreviousAddressId(index))(someAddress, CheckYourAnswersIndividualAddressController.onPageLoad(journeyMode(mode), Index(0), EmptyOptionalSchemeReferenceNumber))
      )

    behave like navigatorWithRoutesForMode(CheckMode)(navigator, checkModeRoutes(CheckMode), EmptyOptionalSchemeReferenceNumber)
  }

  "UpdateMode" must {
    def updateModeRoutes(mode: Mode): TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Next page"),
        row(IndividualPostCodeLookupId(index))(Seq(someTolerantAddress), IndividualAddressListController.onPageLoad(mode, Index(0), OptionalSchemeReferenceNumber(srn))),
        row(IndividualAddressListId(index))(someTolerantAddress, TrusteeAddressYearsController.onPageLoad(mode, Index(0), OptionalSchemeReferenceNumber(srn))),
        row(TrusteeAddressId(index))(someAddress, TrusteeAddressYearsController.onPageLoad(mode, Index(0), OptionalSchemeReferenceNumber(srn))),
        row(TrusteeAddressYearsId(index))(AddressYears.UnderAYear, IndividualPreviousAddressPostcodeLookupController.onPageLoad(mode, Index(0), OptionalSchemeReferenceNumber(srn))),
        row(TrusteeAddressYearsId(index))(AddressYears.OverAYear, CheckYourAnswersIndividualAddressController.onPageLoad(mode, Index(0), OptionalSchemeReferenceNumber(srn))),
        row(IndividualPreviousAddressPostCodeLookupId(index))(Seq(someTolerantAddress), TrusteePreviousAddressListController.onPageLoad(mode, Index(0), OptionalSchemeReferenceNumber(srn))),
        row(TrusteePreviousAddressListId(index))(someTolerantAddress, CheckYourAnswersIndividualAddressController.onPageLoad(mode, Index(0), OptionalSchemeReferenceNumber(srn)), Some(newTrusteeUserAnswers)),
        row(TrusteePreviousAddressId(index))(someAddress, CheckYourAnswersIndividualAddressController.onPageLoad(mode, Index(0), OptionalSchemeReferenceNumber(srn)), Some(newTrusteeUserAnswers))
      )

    behave like navigatorWithRoutesForMode(UpdateMode)(navigator, updateModeRoutes(UpdateMode), OptionalSchemeReferenceNumber(srn))
  }

  "CheckUpdateMode" must {
    def checkUpdateModeRoutes(mode: Mode): TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Next page"),
        row(IndividualPostCodeLookupId(index))(Seq(someTolerantAddress), IndividualAddressListController.onPageLoad(mode, Index(0), OptionalSchemeReferenceNumber(srn))),
        row(IndividualAddressListId(index))(someTolerantAddress, CheckYourAnswersIndividualAddressController.onPageLoad(journeyMode(mode), Index(0), OptionalSchemeReferenceNumber(srn)), Some(newTrusteeUserAnswers)),
        row(TrusteeAddressId(index))(someAddress, CheckYourAnswersIndividualAddressController.onPageLoad(journeyMode(mode), Index(0), OptionalSchemeReferenceNumber(srn)), Some(newTrusteeUserAnswers)),
        row(TrusteeAddressId(index))(someAddress, IndividualConfirmPreviousAddressController.onPageLoad(Index(0), OptionalSchemeReferenceNumber(srn))),
        row(TrusteeAddressYearsId(index))(AddressYears.UnderAYear, IndividualPreviousAddressPostcodeLookupController.onPageLoad(mode, Index(0), OptionalSchemeReferenceNumber(srn))),
        row(TrusteeAddressYearsId(index))(AddressYears.OverAYear, CheckYourAnswersIndividualAddressController.onPageLoad(journeyMode(mode), Index(0), OptionalSchemeReferenceNumber(srn))),
        row(IndividualPreviousAddressPostCodeLookupId(index))(Seq(someTolerantAddress), TrusteePreviousAddressListController.onPageLoad(mode, Index(0), OptionalSchemeReferenceNumber(srn))),
        row(TrusteePreviousAddressListId(index))(someTolerantAddress, CheckYourAnswersIndividualAddressController.onPageLoad(journeyMode(mode), Index(0), OptionalSchemeReferenceNumber(srn)), Some(newTrusteeUserAnswers)),
        row(TrusteePreviousAddressListId(index))(someTolerantAddress, AnyMoreChangesController.onPageLoad(OptionalSchemeReferenceNumber(Some(SchemeReferenceNumber(srn.get)))), Some(existingTrusteeUserAnswers)),
        row(TrusteePreviousAddressId(index))(someAddress, CheckYourAnswersIndividualAddressController.onPageLoad(journeyMode(mode), Index(0), OptionalSchemeReferenceNumber(srn)), Some(newTrusteeUserAnswers)),
        row(TrusteePreviousAddressId(index))(someAddress, AnyMoreChangesController.onPageLoad(OptionalSchemeReferenceNumber(Some(SchemeReferenceNumber(srn.get)))), Some(existingTrusteeUserAnswers))
      )

    behave like navigatorWithRoutesForMode(CheckUpdateMode)(navigator, checkUpdateModeRoutes(CheckUpdateMode), OptionalSchemeReferenceNumber(srn))
  }


}

object TrusteesIndividualAddressNavigatorSpec extends SpecBase with Matchers with NavigatorBehaviour with Generators {
  private lazy val index = 0
  private val newTrusteeUserAnswers: UserAnswers = UserAnswers().set(IsTrusteeNewId(index))(true).asOpt.value
  private val existingTrusteeUserAnswers = UserAnswers().set(IsTrusteeNewId(index))(false).asOpt.value
  private val srn = Some(SchemeReferenceNumber("srn"))
}


