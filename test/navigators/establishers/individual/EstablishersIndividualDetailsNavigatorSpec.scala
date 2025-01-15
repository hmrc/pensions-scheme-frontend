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
import controllers.register.establishers.routes._
import generators.Generators
import identifiers.Identifier
import identifiers.register.establishers.IsEstablisherNewId
import identifiers.register.establishers.individual._
import models.Mode._
import models._
import navigators.establishers.individual.EstablishersIndividualContactDetailsNavigatorSpec.srn
import navigators.{Navigator, NavigatorBehaviour}
import org.scalatest.matchers.must.Matchers
import org.scalatest.prop._
import play.api.mvc.Call
import utils.UserAnswers

import java.time.LocalDate

class EstablishersIndividualDetailsNavigatorSpec extends SpecBase with Matchers with NavigatorBehaviour with Generators {
  import EstablishersIndividualDetailsNavigatorSpec._

  val navigator: Navigator = injector.instanceOf[EstablishersIndividualDetailsNavigator]

  "NormalMode" must {
    val normalModeRoutes: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Next Page"),
        row(EstablisherNameId(index))(somePersonNameValue, PsaSchemeTaskListRegistrationEstablisherController.onPageLoad(index)),
        row(EstablisherDOBId(index))(someDate, EstablisherHasNINOController.onPageLoad( NormalMode, Index(0), EmptyOptionalSchemeReferenceNumber)),
        row(EstablisherHasNINOId(index))(true, EstablisherEnterNINOController.onPageLoad( NormalMode, Index(0), EmptyOptionalSchemeReferenceNumber)),
        row(EstablisherHasNINOId(index))(false, EstablisherNoNINOReasonController.onPageLoad( NormalMode, Index(0), EmptyOptionalSchemeReferenceNumber)),
        row(EstablisherEnterNINOId(index))(someRefValue, EstablisherHasUTRController.onPageLoad( NormalMode, Index(0), EmptyOptionalSchemeReferenceNumber)),
        row(EstablisherNoNINOReasonId(index))(someStringValue, EstablisherHasUTRController.onPageLoad( NormalMode, Index(0), EmptyOptionalSchemeReferenceNumber)),
        row(EstablisherHasUTRId(index))(true, EstablisherEnterUTRController.onPageLoad( NormalMode, Index(0), EmptyOptionalSchemeReferenceNumber)),
        row(EstablisherHasUTRId(index))(false, EstablisherNoUTRReasonController.onPageLoad( NormalMode, Index(0), EmptyOptionalSchemeReferenceNumber)),
        row(EstablisherNoUTRReasonId(index))(someStringValue, CheckYourAnswersDetailsController.onPageLoad(journeyMode(NormalMode), Index(0), EmptyOptionalSchemeReferenceNumber)),
        row(EstablisherUTRId(index))(someRefValue, CheckYourAnswersDetailsController.onPageLoad(journeyMode(NormalMode), Index(0), EmptyOptionalSchemeReferenceNumber))
      )

    behave like navigatorWithRoutesForMode(NormalMode)(navigator, normalModeRoutes, OptionalSchemeReferenceNumber(srn))
  }

  "CheckMode" must {
    val checkModeRoutes: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Expected next page"),
        row(EstablisherDOBId(index))(someDate, CheckYourAnswersDetailsController.onPageLoad(journeyMode(CheckMode), Index(0), EmptyOptionalSchemeReferenceNumber)),
        row(EstablisherHasNINOId(index))(true, EstablisherEnterNINOController.onPageLoad(CheckMode, Index(0), EmptyOptionalSchemeReferenceNumber)),
        row(EstablisherEnterNINOId(index))(someRefValue, CheckYourAnswersDetailsController.onPageLoad(journeyMode(CheckMode), Index(0), EmptyOptionalSchemeReferenceNumber)),
        row(EstablisherHasNINOId(index))(false, EstablisherNoNINOReasonController.onPageLoad(CheckMode, Index(0), EmptyOptionalSchemeReferenceNumber)),
        row(EstablisherNoNINOReasonId(index))(someStringValue, CheckYourAnswersDetailsController.onPageLoad(journeyMode(CheckMode), Index(0), EmptyOptionalSchemeReferenceNumber)),
        row(EstablisherHasUTRId(index))(true, EstablisherEnterUTRController.onPageLoad(CheckMode, Index(0), EmptyOptionalSchemeReferenceNumber)),
        row(EstablisherUTRId(index))(someRefValue, CheckYourAnswersDetailsController.onPageLoad(journeyMode(CheckMode), Index(0), EmptyOptionalSchemeReferenceNumber)),
        row(EstablisherHasUTRId(index))(false, EstablisherNoUTRReasonController.onPageLoad(CheckMode, Index(0), EmptyOptionalSchemeReferenceNumber)),
        row(EstablisherNoUTRReasonId(index))(someStringValue, CheckYourAnswersDetailsController.onPageLoad(journeyMode(CheckMode), Index(0), EmptyOptionalSchemeReferenceNumber))
      )

    behave like navigatorWithRoutesForMode(CheckMode)(navigator, checkModeRoutes, OptionalSchemeReferenceNumber(srn))
  }

  "UpdateMode" must {
    val updateModeRoutes: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Expected next page"),
        row(EstablisherNameId(index))(somePersonNameValue, AddEstablisherController.onPageLoad(UpdateMode, OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers)),
        row(EstablisherDOBId(index))(someDate, EstablisherHasNINOController.onPageLoad(UpdateMode, Index(0), OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers)),
        row(EstablisherHasNINOId(index))(true, EstablisherEnterNINOController.onPageLoad(UpdateMode, Index(0), OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers)),
        row(EstablisherHasNINOId(index))(false, EstablisherNoNINOReasonController.onPageLoad(UpdateMode, Index(0), OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers)),
        row(EstablisherEnterNINOId(index))(someRefValue, EstablisherHasUTRController.onPageLoad(UpdateMode, Index(0), OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers)),
        row(EstablisherNoNINOReasonId(index))(someStringValue, EstablisherHasUTRController.onPageLoad(UpdateMode, Index(0), OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers)),
        row(EstablisherHasUTRId(index))(true, EstablisherEnterUTRController.onPageLoad(UpdateMode, Index(0), OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers)),
        row(EstablisherHasUTRId(index))(false, EstablisherNoUTRReasonController.onPageLoad(UpdateMode, Index(0), OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers)),
        row(EstablisherNoUTRReasonId(index))(someStringValue, CheckYourAnswersDetailsController.onPageLoad(journeyMode(UpdateMode), Index(0), OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers)),
        row(EstablisherUTRId(index))(someRefValue, CheckYourAnswersDetailsController.onPageLoad(journeyMode(UpdateMode), Index(0), OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers))
      )

    behave like navigatorWithRoutesForMode(UpdateMode)(navigator, updateModeRoutes, OptionalSchemeReferenceNumber(srn))
  }

  "CheckUpdateMode" must {
    val checkUpdateModeRoutes: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Expected next page"),
        row(EstablisherDOBId(index))(someDate, CheckYourAnswersDetailsController.onPageLoad(journeyMode(CheckUpdateMode), Index(0), OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers)),
        row(EstablisherHasNINOId(index))(true, EstablisherEnterNINOController.onPageLoad(CheckUpdateMode, Index(0), OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers)),
        row(EstablisherEnterNINOId(index))(someRefValue, CheckYourAnswersDetailsController.onPageLoad(journeyMode(CheckUpdateMode), Index(0), OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers)),
        row(EstablisherEnterNINOId(index))(someRefValue, anyMoreChangesPage(OptionalSchemeReferenceNumber(srn)), Some(existingEstablisherUserAnswers)),
        row(EstablisherHasNINOId(index))(false, EstablisherNoNINOReasonController.onPageLoad(CheckUpdateMode, Index(0), OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers)),
        row(EstablisherNoNINOReasonId(index))(someStringValue, CheckYourAnswersDetailsController.onPageLoad(journeyMode(CheckUpdateMode), Index(0), OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers)),
        row(EstablisherHasUTRId(index))(true, EstablisherEnterUTRController.onPageLoad(CheckUpdateMode, Index(0), OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers)),
        row(EstablisherUTRId(index))(someRefValue, CheckYourAnswersDetailsController.onPageLoad(journeyMode(CheckUpdateMode), Index(0), OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers)),
        row(EstablisherUTRId(index))(someRefValue, anyMoreChangesPage(OptionalSchemeReferenceNumber(srn)), Some(existingEstablisherUserAnswers)),
        row(EstablisherHasUTRId(index))(false, EstablisherNoUTRReasonController.onPageLoad(CheckUpdateMode, Index(0), OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers)),
        row(EstablisherNoUTRReasonId(index))(someStringValue, CheckYourAnswersDetailsController.onPageLoad(journeyMode(CheckUpdateMode), Index(0), OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers))
      )

    behave like navigatorWithRoutesForMode(CheckUpdateMode)(navigator, checkUpdateModeRoutes, OptionalSchemeReferenceNumber(srn))
  }
}

object EstablishersIndividualDetailsNavigatorSpec extends SpecBase with Matchers with NavigatorBehaviour with Generators {
  private lazy val index = 0
  private val newEstablisherUserAnswers = UserAnswers().set(IsEstablisherNewId(index))(true).asOpt.value
  private val existingEstablisherUserAnswers = UserAnswers().set(IsEstablisherNewId(index))(false).asOpt.value
  private val srn = Some(SchemeReferenceNumber("srn"))
  private val someDate =  LocalDate.now()
}
