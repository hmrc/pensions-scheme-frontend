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

package navigators.establishers.individual

import base.SpecBase
import generators.Generators
import identifiers.Identifier
import controllers.register.establishers.routes._
import controllers.register.establishers.individual.routes._
import identifiers.register.establishers.IsEstablisherNewId
import identifiers.register.establishers.individual._
import models._
import models.Mode._
import navigators.{Navigator, NavigatorBehaviour}
import org.joda.time.LocalDate
import org.scalatest.MustMatchers
import org.scalatest.prop._
import play.api.mvc.Call
import utils.UserAnswers

class EstablishersIndividualDetailsNavigatorSpec extends SpecBase with MustMatchers with NavigatorBehaviour with Generators {
  import EstablishersIndividualDetailsNavigatorSpec._

  val navigator: Navigator = injector.instanceOf[EstablishersIndividualDetailsNavigator]

  "NormalMode" must {
    val normalModeRoutes: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Next Page"),
        row(EstablisherNameId(index))(somePersonNameValue, AddEstablisherController.onPageLoad(NormalMode, None)),
        row(EstablisherDOBId(index))(someDate, EstablisherHasNINOController.onPageLoad(NormalMode, index, None)),
        row(EstablisherHasNINOId(index))(true, EstablisherEnterNINOController.onPageLoad(NormalMode, index, None)),
        row(EstablisherHasNINOId(index))(false, EstablisherNoNINOReasonController.onPageLoad(NormalMode, index, None)),
        row(EstablisherEnterNINOId(index))(someRefValue, EstablisherHasUTRController.onPageLoad(NormalMode, index, None)),
        row(EstablisherNoNINOReasonId(index))(someStringValue, EstablisherHasUTRController.onPageLoad(NormalMode, index, None)),
        row(EstablisherHasUTRId(index))(true, EstablisherEnterUTRController.onPageLoad(NormalMode, index, None)),
        row(EstablisherHasUTRId(index))(false, EstablisherNoUTRReasonController.onPageLoad(NormalMode, index, None)),
        row(EstablisherNoUTRReasonId(index))(someStringValue, CheckYourAnswersDetailsController.onPageLoad(journeyMode(NormalMode), index, None)),
        row(EstablisherUTRId(index))(someRefValue, CheckYourAnswersDetailsController.onPageLoad(journeyMode(NormalMode), index, None))
      )

    behave like navigatorWithRoutesForMode(NormalMode)(navigator, normalModeRoutes, None)
  }

  "CheckMode" must {
    val checkModeRoutes: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Expected next page"),
        row(EstablisherDOBId(index))(someDate, CheckYourAnswersDetailsController.onPageLoad(journeyMode(CheckMode), index, None)),
        row(EstablisherHasNINOId(index))(true, EstablisherEnterNINOController.onPageLoad(CheckMode, index, None)),
        row(EstablisherEnterNINOId(index))(someRefValue, CheckYourAnswersDetailsController.onPageLoad(journeyMode(CheckMode), index, None)),
        row(EstablisherHasNINOId(index))(false, EstablisherNoNINOReasonController.onPageLoad(CheckMode, index, None)),
        row(EstablisherNoNINOReasonId(index))(someStringValue, CheckYourAnswersDetailsController.onPageLoad(journeyMode(CheckMode), index, None)),
        row(EstablisherHasUTRId(index))(true, EstablisherEnterUTRController.onPageLoad(CheckMode, index, None)),
        row(EstablisherUTRId(index))(someRefValue, CheckYourAnswersDetailsController.onPageLoad(journeyMode(CheckMode), index, None)),
        row(EstablisherHasUTRId(index))(false, EstablisherNoUTRReasonController.onPageLoad(CheckMode, index, None)),
        row(EstablisherNoUTRReasonId(index))(someStringValue, CheckYourAnswersDetailsController.onPageLoad(journeyMode(CheckMode), index, None))
      )

    behave like navigatorWithRoutesForMode(CheckMode)(navigator, checkModeRoutes, None)
  }

  "UpdateMode" must {
    val updateModeRoutes: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Expected next page"),
        row(EstablisherNameId(index))(somePersonNameValue, AddEstablisherController.onPageLoad(UpdateMode, srn), Some(newEstablisherUserAnswers)),
        row(EstablisherDOBId(index))(someDate, EstablisherHasNINOController.onPageLoad(UpdateMode, index, srn), Some(newEstablisherUserAnswers)),
        row(EstablisherHasNINOId(index))(true, EstablisherEnterNINOController.onPageLoad(UpdateMode, index, srn), Some(newEstablisherUserAnswers)),
        row(EstablisherHasNINOId(index))(false, EstablisherNoNINOReasonController.onPageLoad(UpdateMode, index, srn), Some(newEstablisherUserAnswers)),
        row(EstablisherEnterNINOId(index))(someRefValue, EstablisherHasUTRController.onPageLoad(UpdateMode, index, srn), Some(newEstablisherUserAnswers)),
        row(EstablisherNoNINOReasonId(index))(someStringValue, EstablisherHasUTRController.onPageLoad(UpdateMode, index, srn), Some(newEstablisherUserAnswers)),
        row(EstablisherHasUTRId(index))(true, EstablisherEnterUTRController.onPageLoad(UpdateMode, index, srn), Some(newEstablisherUserAnswers)),
        row(EstablisherHasUTRId(index))(false, EstablisherNoUTRReasonController.onPageLoad(UpdateMode, index, srn), Some(newEstablisherUserAnswers)),
        row(EstablisherNoUTRReasonId(index))(someStringValue, CheckYourAnswersDetailsController.onPageLoad(journeyMode(UpdateMode), index, srn), Some(newEstablisherUserAnswers)),
        row(EstablisherUTRId(index))(someRefValue, CheckYourAnswersDetailsController.onPageLoad(journeyMode(UpdateMode), index, srn), Some(newEstablisherUserAnswers))
      )

    behave like navigatorWithRoutesForMode(UpdateMode)(navigator, updateModeRoutes, srn)
  }

  "CheckUpdateMode" must {
    val checkUpdateModeRoutes: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Expected next page"),
        row(EstablisherDOBId(index))(someDate, CheckYourAnswersDetailsController.onPageLoad(journeyMode(CheckUpdateMode), index, srn), Some(newEstablisherUserAnswers)),
        row(EstablisherHasNINOId(index))(true, EstablisherEnterNINOController.onPageLoad(CheckUpdateMode, index, srn), Some(newEstablisherUserAnswers)),
        row(EstablisherEnterNINOId(index))(someRefValue, CheckYourAnswersDetailsController.onPageLoad(journeyMode(CheckUpdateMode), index, srn), Some(newEstablisherUserAnswers)),
        row(EstablisherEnterNINOId(index))(someRefValue, anyMoreChangesPage(srn), Some(existingEstablisherUserAnswers)),
        row(EstablisherHasNINOId(index))(false, EstablisherNoNINOReasonController.onPageLoad(CheckUpdateMode, index, srn), Some(newEstablisherUserAnswers)),
        row(EstablisherNoNINOReasonId(index))(someStringValue, CheckYourAnswersDetailsController.onPageLoad(journeyMode(CheckUpdateMode), index, srn), Some(newEstablisherUserAnswers)),
        row(EstablisherHasUTRId(index))(true, EstablisherEnterUTRController.onPageLoad(CheckUpdateMode, index, srn), Some(newEstablisherUserAnswers)),
        row(EstablisherUTRId(index))(someRefValue, CheckYourAnswersDetailsController.onPageLoad(journeyMode(CheckUpdateMode), index, srn), Some(newEstablisherUserAnswers)),
        row(EstablisherUTRId(index))(someRefValue, anyMoreChangesPage(srn), Some(existingEstablisherUserAnswers)),
        row(EstablisherHasUTRId(index))(false, EstablisherNoUTRReasonController.onPageLoad(CheckUpdateMode, index, srn), Some(newEstablisherUserAnswers)),
        row(EstablisherNoUTRReasonId(index))(someStringValue, CheckYourAnswersDetailsController.onPageLoad(journeyMode(CheckUpdateMode), index, srn), Some(newEstablisherUserAnswers))
      )

    behave like navigatorWithRoutesForMode(CheckUpdateMode)(navigator, checkUpdateModeRoutes, srn)
  }
}

object EstablishersIndividualDetailsNavigatorSpec extends SpecBase with MustMatchers with NavigatorBehaviour with Generators {
  private lazy val index = 0
  private val newEstablisherUserAnswers = UserAnswers().set(IsEstablisherNewId(index))(true).asOpt.value
  private val existingEstablisherUserAnswers = UserAnswers().set(IsEstablisherNewId(index))(false).asOpt.value
  private val srn = Some("srn")
  private val someDate =  LocalDate.now()
}
