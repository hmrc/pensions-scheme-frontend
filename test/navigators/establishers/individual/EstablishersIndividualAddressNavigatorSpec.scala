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

package navigators.establishers.individual

import base.SpecBase
import controllers.register.establishers.individual.routes._
import controllers.routes.AnyMoreChangesController
import generators.Generators
import identifiers.Identifier
import identifiers.register.establishers.IsEstablisherNewId
import identifiers.register.establishers.individual._
import models.Mode._
import models._
import navigators.{Navigator, NavigatorBehaviour}
import org.scalatest.matchers.must.Matchers
import org.scalatest.prop._
import play.api.mvc.Call
import utils.UserAnswers

class EstablishersIndividualAddressNavigatorSpec extends SpecBase with Matchers with NavigatorBehaviour with Generators {

  import EstablishersIndividualAddressNavigatorSpec._
  val navigator: Navigator = injector.instanceOf[EstablishersIndividualAddressNavigator]

  "NormalMode" must {
    def normalModeRoutes(mode: Mode): TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Next Page"),
        row(PostCodeLookupId(index))(Seq(someTolerantAddress), AddressListController.onPageLoad(mode, Index(0), EmptyOptionalSchemeReferenceNumber)),
        row(AddressListId(index))(someTolerantAddress, AddressYearsController.onPageLoad(mode, Index(0), EmptyOptionalSchemeReferenceNumber)),
        row(AddressId(index))(someAddress, AddressYearsController.onPageLoad(mode, Index(0), EmptyOptionalSchemeReferenceNumber)),
        row(AddressYearsId(index))(AddressYears.UnderAYear, PreviousAddressPostCodeLookupController.onPageLoad(mode, Index(0), EmptyOptionalSchemeReferenceNumber)),
        row(AddressYearsId(index))(AddressYears.OverAYear, CheckYourAnswersAddressController.onPageLoad(mode, Index(0), EmptyOptionalSchemeReferenceNumber)),
        row(PreviousPostCodeLookupId(index))(Seq(someTolerantAddress), PreviousAddressListController.onPageLoad(mode, Index(0), EmptyOptionalSchemeReferenceNumber)),
        row(PreviousAddressListId(index))(someTolerantAddress, CheckYourAnswersAddressController.onPageLoad(mode, Index(0), EmptyOptionalSchemeReferenceNumber)),
        row(PreviousAddressId(index))(someAddress, CheckYourAnswersAddressController.onPageLoad(mode, Index(0), EmptyOptionalSchemeReferenceNumber))
      )

    behave like navigatorWithRoutesForMode(NormalMode)(navigator, normalModeRoutes(NormalMode), EmptyOptionalSchemeReferenceNumber)
  }

  "CheckMode" must {
    def checkModeRoutes(mode: Mode): TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Next Page"),
        row(PostCodeLookupId(index))(Seq(someTolerantAddress), AddressListController.onPageLoad(mode, Index(0), EmptyOptionalSchemeReferenceNumber)),
        row(AddressListId(index))(someTolerantAddress, CheckYourAnswersAddressController.onPageLoad(journeyMode(mode), Index(0), EmptyOptionalSchemeReferenceNumber)),
        row(AddressId(index))(someAddress, CheckYourAnswersAddressController.onPageLoad(journeyMode(mode), Index(0), EmptyOptionalSchemeReferenceNumber)),
        row(AddressYearsId(index))(AddressYears.UnderAYear, PreviousAddressPostCodeLookupController.onPageLoad(mode, Index(0), EmptyOptionalSchemeReferenceNumber)),
        row(AddressYearsId(index))(AddressYears.OverAYear, CheckYourAnswersAddressController.onPageLoad(journeyMode(mode), Index(0), EmptyOptionalSchemeReferenceNumber)),
        row(PreviousPostCodeLookupId(index))(Seq(someTolerantAddress), PreviousAddressListController.onPageLoad(mode, Index(0), EmptyOptionalSchemeReferenceNumber)),
        row(PreviousAddressListId(index))(someTolerantAddress, CheckYourAnswersAddressController.onPageLoad(journeyMode(mode), Index(0), EmptyOptionalSchemeReferenceNumber)),
        row(PreviousAddressId(index))(someAddress, CheckYourAnswersAddressController.onPageLoad(journeyMode(mode), Index(0), EmptyOptionalSchemeReferenceNumber))
      )

    behave like navigatorWithRoutesForMode(CheckMode)(navigator, checkModeRoutes(CheckMode), EmptyOptionalSchemeReferenceNumber)
  }

  "UpdateMode" must {
    def updateModeRoutes(mode: Mode): TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Next page"),
        row(PostCodeLookupId(index))(Seq(someTolerantAddress), AddressListController.onPageLoad(mode, Index(0), OptionalSchemeReferenceNumber(srn))),
        row(AddressListId(index))(someTolerantAddress, AddressYearsController.onPageLoad(mode, Index(0), OptionalSchemeReferenceNumber(srn))),
        row(AddressId(index))(someAddress, AddressYearsController.onPageLoad(mode, Index(0), OptionalSchemeReferenceNumber(srn))),
        row(AddressYearsId(index))(AddressYears.UnderAYear, PreviousAddressPostCodeLookupController.onPageLoad(mode, Index(0), OptionalSchemeReferenceNumber(srn))),
        row(AddressYearsId(index))(AddressYears.OverAYear, CheckYourAnswersAddressController.onPageLoad(mode, Index(0), OptionalSchemeReferenceNumber(srn))),
        row(PreviousPostCodeLookupId(index))(Seq(someTolerantAddress), PreviousAddressListController.onPageLoad(mode, Index(0), OptionalSchemeReferenceNumber(srn))),
        row(PreviousAddressListId(index))(someTolerantAddress, CheckYourAnswersAddressController.onPageLoad(mode, Index(0), OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers)),
        row(PreviousAddressId(index))(someAddress, CheckYourAnswersAddressController.onPageLoad(mode, Index(0), OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers))
      )

    behave like navigatorWithRoutesForMode(UpdateMode)(navigator, updateModeRoutes(UpdateMode), OptionalSchemeReferenceNumber(srn))
  }

  "CheckUpdateMode" must {
    def checkUpdateModeRoutes(mode: Mode): TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Next page"),
        row(PostCodeLookupId(index))(Seq(someTolerantAddress), AddressListController.onPageLoad(mode, Index(0), OptionalSchemeReferenceNumber(srn))),
        row(AddressListId(index))(someTolerantAddress, CheckYourAnswersAddressController.onPageLoad(journeyMode(mode), Index(0), OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers)),
        row(AddressListId(index))(someTolerantAddress, IndividualConfirmPreviousAddressController.onPageLoad(Index(0), OptionalSchemeReferenceNumber(srn))),
        row(AddressId(index))(someAddress, CheckYourAnswersAddressController.onPageLoad(journeyMode(mode), Index(0), OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers)),
        row(AddressId(index))(someAddress, IndividualConfirmPreviousAddressController.onPageLoad(Index(0), OptionalSchemeReferenceNumber(srn))),
        row(AddressYearsId(index))(AddressYears.UnderAYear, PreviousAddressPostCodeLookupController.onPageLoad(mode, Index(0), OptionalSchemeReferenceNumber(srn))),
        row(AddressYearsId(index))(AddressYears.OverAYear, CheckYourAnswersAddressController.onPageLoad(journeyMode(mode), Index(0), OptionalSchemeReferenceNumber(srn))),
        row(PreviousPostCodeLookupId(index))(Seq(someTolerantAddress), PreviousAddressListController.onPageLoad(mode, Index(0), OptionalSchemeReferenceNumber(srn))),
        row(PreviousAddressListId(index))(someTolerantAddress, CheckYourAnswersAddressController.onPageLoad(journeyMode(mode), Index(0), OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers)),
        row(PreviousAddressListId(index))(someTolerantAddress, AnyMoreChangesController.onPageLoad(OptionalSchemeReferenceNumber(srn)), Some(existingEstablisherUserAnswers)),
        row(PreviousAddressId(index))(someAddress, CheckYourAnswersAddressController.onPageLoad(journeyMode(mode), Index(0), OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers)),
        row(PreviousAddressId(index))(someAddress, AnyMoreChangesController.onPageLoad(OptionalSchemeReferenceNumber(srn)), Some(existingEstablisherUserAnswers))
      )

    behave like navigatorWithRoutesForMode(CheckUpdateMode)(navigator, checkUpdateModeRoutes(CheckUpdateMode), OptionalSchemeReferenceNumber(srn))
  }


}

object EstablishersIndividualAddressNavigatorSpec extends SpecBase with Matchers with NavigatorBehaviour with Generators {
  private lazy val index = 0
  private val newEstablisherUserAnswers: UserAnswers = UserAnswers().set(IsEstablisherNewId(index))(true).asOpt.value
  private val existingEstablisherUserAnswers = UserAnswers().set(IsEstablisherNewId(index))(false).asOpt.value
  private val srn = Some(SchemeReferenceNumber("srn"))
}





