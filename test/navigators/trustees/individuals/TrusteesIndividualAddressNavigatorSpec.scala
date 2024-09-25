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
        row(IndividualPostCodeLookupId(index))(Seq(someTolerantAddress), IndividualAddressListController.onPageLoad(mode, index, None)),
        row(IndividualAddressListId(index))(someTolerantAddress, TrusteeAddressYearsController.onPageLoad(mode, index, None)),
        row(TrusteeAddressId(index))(someAddress, TrusteeAddressYearsController.onPageLoad(mode, index, None)),
        row(TrusteeAddressYearsId(index))(AddressYears.UnderAYear, IndividualPreviousAddressPostcodeLookupController.onPageLoad(mode, index, None)),
        row(TrusteeAddressYearsId(index))(AddressYears.OverAYear, CheckYourAnswersIndividualAddressController.onPageLoad(mode, index, None)),
        row(IndividualPreviousAddressPostCodeLookupId(index))(Seq(someTolerantAddress), TrusteePreviousAddressListController.onPageLoad(mode, index, None)),
        row(TrusteePreviousAddressListId(index))(someTolerantAddress, CheckYourAnswersIndividualAddressController.onPageLoad(mode, index, None)),
        row(TrusteePreviousAddressId(index))(someAddress, CheckYourAnswersIndividualAddressController.onPageLoad(mode, index, None))
      )

    behave like navigatorWithRoutesForMode(NormalMode)(navigator, normalModeRoutes(NormalMode), None)
  }

  "CheckMode" must {
    def checkModeRoutes(mode: Mode): TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Next Page"),
        row(IndividualPostCodeLookupId(index))(Seq(someTolerantAddress), IndividualAddressListController.onPageLoad(mode, index, None)),
        row(IndividualAddressListId(index))(someTolerantAddress, CheckYourAnswersIndividualAddressController.onPageLoad(journeyMode(mode), index, None)),
        row(TrusteeAddressId(index))(someAddress, CheckYourAnswersIndividualAddressController.onPageLoad(journeyMode(mode), index, None)),
        row(TrusteeAddressYearsId(index))(AddressYears.UnderAYear, IndividualPreviousAddressPostcodeLookupController.onPageLoad(mode, index, None)),
        row(TrusteeAddressYearsId(index))(AddressYears.OverAYear, CheckYourAnswersIndividualAddressController.onPageLoad(journeyMode(mode), index, None)),
        row(IndividualPreviousAddressPostCodeLookupId(index))(Seq(someTolerantAddress), TrusteePreviousAddressListController.onPageLoad(mode, index, None)),
        row(TrusteePreviousAddressListId(index))(someTolerantAddress, CheckYourAnswersIndividualAddressController.onPageLoad(journeyMode(mode), index, None)),
        row(TrusteePreviousAddressId(index))(someAddress, CheckYourAnswersIndividualAddressController.onPageLoad(journeyMode(mode), index, None))
      )

    behave like navigatorWithRoutesForMode(CheckMode)(navigator, checkModeRoutes(CheckMode), None)
  }

  "UpdateMode" must {
    def updateModeRoutes(mode: Mode): TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Next page"),
        row(IndividualPostCodeLookupId(index))(Seq(someTolerantAddress), IndividualAddressListController.onPageLoad(mode, index, srn)),
        row(IndividualAddressListId(index))(someTolerantAddress, TrusteeAddressYearsController.onPageLoad(mode, index, srn)),
        row(TrusteeAddressId(index))(someAddress, TrusteeAddressYearsController.onPageLoad(mode, index, srn)),
        row(TrusteeAddressYearsId(index))(AddressYears.UnderAYear, IndividualPreviousAddressPostcodeLookupController.onPageLoad(mode, index, srn)),
        row(TrusteeAddressYearsId(index))(AddressYears.OverAYear, CheckYourAnswersIndividualAddressController.onPageLoad(mode, index, srn)),
        row(IndividualPreviousAddressPostCodeLookupId(index))(Seq(someTolerantAddress), TrusteePreviousAddressListController.onPageLoad(mode, index, srn)),
        row(TrusteePreviousAddressListId(index))(someTolerantAddress, CheckYourAnswersIndividualAddressController.onPageLoad(mode, index, srn), Some(newTrusteeUserAnswers)),
        row(TrusteePreviousAddressId(index))(someAddress, CheckYourAnswersIndividualAddressController.onPageLoad(mode, index, srn), Some(newTrusteeUserAnswers))
      )

    behave like navigatorWithRoutesForMode(UpdateMode)(navigator, updateModeRoutes(UpdateMode), srn)
  }

  "CheckUpdateMode" must {
    def checkUpdateModeRoutes(mode: Mode): TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Next page"),
        row(IndividualPostCodeLookupId(index))(Seq(someTolerantAddress), IndividualAddressListController.onPageLoad(mode, index, srn)),
        row(IndividualAddressListId(index))(someTolerantAddress, CheckYourAnswersIndividualAddressController.onPageLoad(journeyMode(mode), index, srn), Some(newTrusteeUserAnswers)),
        row(TrusteeAddressId(index))(someAddress, CheckYourAnswersIndividualAddressController.onPageLoad(journeyMode(mode), index, srn), Some(newTrusteeUserAnswers)),
        row(TrusteeAddressId(index))(someAddress, IndividualConfirmPreviousAddressController.onPageLoad(index, srn)),
        row(TrusteeAddressYearsId(index))(AddressYears.UnderAYear, IndividualPreviousAddressPostcodeLookupController.onPageLoad(mode, index, srn)),
        row(TrusteeAddressYearsId(index))(AddressYears.OverAYear, CheckYourAnswersIndividualAddressController.onPageLoad(journeyMode(mode), index, srn)),
        row(IndividualPreviousAddressPostCodeLookupId(index))(Seq(someTolerantAddress), TrusteePreviousAddressListController.onPageLoad(mode, index, srn)),
        row(TrusteePreviousAddressListId(index))(someTolerantAddress, CheckYourAnswersIndividualAddressController.onPageLoad(journeyMode(mode), index, srn), Some(newTrusteeUserAnswers)),
        row(TrusteePreviousAddressListId(index))(someTolerantAddress, AnyMoreChangesController.onPageLoad(srn), Some(existingTrusteeUserAnswers)),
        row(TrusteePreviousAddressId(index))(someAddress, CheckYourAnswersIndividualAddressController.onPageLoad(journeyMode(mode), index, srn), Some(newTrusteeUserAnswers)),
        row(TrusteePreviousAddressId(index))(someAddress, AnyMoreChangesController.onPageLoad(srn), Some(existingTrusteeUserAnswers))
      )

    behave like navigatorWithRoutesForMode(CheckUpdateMode)(navigator, checkUpdateModeRoutes(CheckUpdateMode), srn)
  }


}

object TrusteesIndividualAddressNavigatorSpec extends SpecBase with Matchers with NavigatorBehaviour with Generators {
  private lazy val index = 0
  private val newTrusteeUserAnswers: UserAnswers = UserAnswers().set(IsTrusteeNewId(index))(true).asOpt.value
  private val existingTrusteeUserAnswers = UserAnswers().set(IsTrusteeNewId(index))(false).asOpt.value
  private val srn = Some(SchemeReferenceNumber("srn"))
}


