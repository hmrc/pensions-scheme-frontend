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

package navigators.trustees.individuals

import base.SpecBase
import controllers.register.trustees.individual.routes._
import controllers.routes.AnyMoreChangesController
import generators.Generators
import identifiers.Identifier
import identifiers.register.trustees.IsTrusteeNewId
import identifiers.register.trustees.individual._
import models._
import navigators.{Navigator, NavigatorBehaviour}
import org.joda.time.LocalDate
import org.scalatest.MustMatchers
import org.scalatest.prop._
import play.api.mvc.Call
import utils.UserAnswers

class TrusteesIndividualAddressNavigatorSpec extends SpecBase with MustMatchers with NavigatorBehaviour with Generators {
  import TrusteesIndividualAddressNavigatorSpec._

  val navigator: Navigator = injector.instanceOf[TrusteesIndividualAddressNavigator]

  "NormalMode" must {
    def normalModeRoutes(mode: Mode): TableFor3[Identifier, UserAnswers, Call] =
    Table(
      ("Id", "UserAnswers", "Next Page"),
      row(IndividualPostCodeLookupId(index))(Seq(someTolerantAddress), IndividualAddressListController.onPageLoad(mode, index, None)),
      row(IndividualAddressListId(index))(someTolerantAddress, TrusteeAddressController.onPageLoad(mode, index, None)),
      row(TrusteeAddressId(index))(someAddress, TrusteeAddressYearsController.onPageLoad(mode, index, None)),
      row(TrusteeAddressYearsId(index))(AddressYears.UnderAYear, IndividualPreviousAddressPostcodeLookupController.onPageLoad(mode, index, None)),
      row(TrusteeAddressYearsId(index))(AddressYears.OverAYear, CheckYourAnswersIndividualAddressController.onPageLoad(mode, index, None)),
      row(IndividualPreviousAddressPostCodeLookupId(index))(Seq(someTolerantAddress), TrusteePreviousAddressListController.onPageLoad(mode, index, None)),
      row(TrusteePreviousAddressListId(index))(someTolerantAddress, TrusteePreviousAddressController.onPageLoad(mode, index, None)),
      row(TrusteePreviousAddressId(index))(someAddress, CheckYourAnswersIndividualAddressController.onPageLoad(mode, index, None))
    )

    behave like navigatorWithRoutesForMode(NormalMode)(navigator, normalModeRoutes(NormalMode), None)
  }

  "CheckMode" must {
    def checkModeRoutes(mode: Mode): TableFor3[Identifier, UserAnswers, Call] =
    Table(
      ("Id", "UserAnswers", "Next Page"),
      row(IndividualPostCodeLookupId(index))(Seq(someTolerantAddress), IndividualAddressListController.onPageLoad(mode, index, None)),
      row(IndividualAddressListId(index))(someTolerantAddress, TrusteeAddressController.onPageLoad(mode, index, None)),
      row(TrusteeAddressId(index))(someAddress, TrusteeAddressYearsController.onPageLoad(mode, index, None)),
      row(TrusteeAddressYearsId(index))(AddressYears.UnderAYear, IndividualPreviousAddressPostcodeLookupController.onPageLoad(mode, index, None)),
      row(TrusteeAddressYearsId(index))(AddressYears.OverAYear, CheckYourAnswersIndividualAddressController.onPageLoad(mode, index, None)),
      row(IndividualPreviousAddressPostCodeLookupId(index))(Seq(someTolerantAddress), TrusteePreviousAddressListController.onPageLoad(mode, index, None)),
      row(TrusteePreviousAddressListId(index))(someTolerantAddress, TrusteePreviousAddressController.onPageLoad(mode, index, None)),
      row(TrusteePreviousAddressId(index))(someAddress, CheckYourAnswersIndividualAddressController.onPageLoad(mode, index, None))
    )

    behave like navigatorWithRoutesForMode(CheckMode)(navigator, checkModeRoutes(CheckMode), None)
  }

  "UpdateMode" must {
    def updateMadeRoutes(mode: Mode): TableFor3[Identifier, UserAnswers, Call] =
    Table(
      ("Id", "UserAnswers", "Expected next page"),
      row(IndividualPostCodeLookupId(index))(Seq(someTolerantAddress), IndividualAddressListController.onPageLoad(mode, index, srn)),
      row(IndividualAddressListId(index))(someTolerantAddress, TrusteeAddressController.onPageLoad(mode, index, srn)),
      row(TrusteeAddressId(index))(someAddress, TrusteeAddressYearsController.onPageLoad(mode, index, srn)),
      row(TrusteeAddressYearsId(index))(AddressYears.UnderAYear, IndividualPreviousAddressPostcodeLookupController.onPageLoad(mode, index, srn)),
      row(TrusteeAddressYearsId(index))(AddressYears.OverAYear, CheckYourAnswersIndividualAddressController.onPageLoad(mode, index, srn)),
      row(IndividualPreviousAddressPostCodeLookupId(index))(Seq(someTolerantAddress), TrusteePreviousAddressListController.onPageLoad(mode, index, srn)),
      row(TrusteePreviousAddressListId(index))(someTolerantAddress, TrusteePreviousAddressController.onPageLoad(mode, index, srn)),
      row(TrusteePreviousAddressId(index))(someAddress, AnyMoreChangesController.onPageLoad(srn))
    )

    behave like navigatorWithRoutesForMode(UpdateMode)(navigator, updateMadeRoutes(UpdateMode), srn)
  }


//  "CheckUpdateMode" must {
//    def navigationForVarianceModeTrusteeIndividual(mode: Mode): TableFor3[Identifier, UserAnswers, Call] =
//      Table(
//        ("Id", "UserAnswers", "Expected next page"),
//
//      )
//
//    behave like navigatorWithRoutesForMode(CheckUpdateMode)(navigator, navigationForVarianceModeTrusteeIndividual(CheckUpdateMode), srn)
//  }


}

object TrusteesIndividualAddressNavigatorSpec extends SpecBase with MustMatchers with NavigatorBehaviour with Generators {
  private lazy val index = 0
  private val someDate =  LocalDate.now()
  private val newTrusteeUserAnswers = UserAnswers().set(IsTrusteeNewId(index))(true).asOpt.value
  private val exisitingTrusteeUserAnswers = UserAnswers().set(IsTrusteeNewId(index))(false).asOpt.value
  private val srn = Some("srn")

  private def cyaIndividualDetailsPage(mode: Mode, index: Index, srn: Option[String]): Call =
    CheckYourAnswersIndividualDetailsController.onPageLoad(Mode.journeyMode(mode), index, srn)

}


