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

class TrusteesIndividualDetailsNavigatorSpec extends SpecBase with MustMatchers with NavigatorBehaviour with Generators {
  import TrusteesIndividualDetailsNavigatorSpec._

  val navigator: Navigator = injector.instanceOf[TrusteesIndividualDetailsNavigator]

  "NormalMode" must {
    def navigationForNewTrusteeIndividual(mode: Mode): TableFor3[Identifier, UserAnswers, Call] =
    Table(
      ("Id", "UserAnswers", "Next Page"),
      row(TrusteeNameId(index))(somePersonNameValue, controllers.register.trustees.routes.AddTrusteeController.onPageLoad(mode, None)),
      row(TrusteeDOBId(index))(someDate, TrusteeHasNINOController.onPageLoad(mode, index, None)),
      row(TrusteeHasNINOId(index))(true, TrusteeNinoNewController.onPageLoad(mode, index, None)),
      row(TrusteeHasNINOId(index))(false, TrusteeNoNINOReasonController.onPageLoad(mode, index, None)),
      row(TrusteeNewNinoId(index))(someRefValue, TrusteeHasUTRController.onPageLoad(mode, index, None)),
      row(TrusteeNoNINOReasonId(index))(someStringValue, TrusteeHasUTRController.onPageLoad(mode, index, None)),
      row(TrusteeHasUTRId(index))(true, TrusteeUTRController.onPageLoad(mode, index, None)),
      row(TrusteeHasUTRId(index))(false, TrusteeNoUTRReasonController.onPageLoad(mode, index, None)),
      row(TrusteeNoUTRReasonId(index))(someStringValue, cyaIndividualDetailsPage(mode, index, None)),
      row(TrusteeUTRId(index))(someRefValue, cyaIndividualDetailsPage(mode, index, None))
    )

    behave like navigatorWithRoutesForMode(NormalMode)(navigator, navigationForNewTrusteeIndividual(NormalMode), None)
  }

  "CheckMode" must {
    def checkModeRoutes(mode: Mode): TableFor3[Identifier, UserAnswers, Call] =
    Table(
      ("Id", "UserAnswers", "Expected next page"),
      row(TrusteeDOBId(index))(someDate, cyaIndividualDetailsPage(CheckMode, index, None)),
      row(TrusteeHasNINOId(index))(true, TrusteeNinoNewController.onPageLoad(CheckMode, index, None)),
      row(TrusteeNewNinoId(index))(someRefValue, cyaIndividualDetailsPage(CheckMode, index, None)),
      row(TrusteeHasNINOId(index))(false, TrusteeNoNINOReasonController.onPageLoad(CheckMode, index, None)),
      row(TrusteeNoNINOReasonId(index))(someStringValue, cyaIndividualDetailsPage(CheckMode, index, None)),
      row(TrusteeHasUTRId(index))(true, TrusteeUTRController.onPageLoad(CheckMode, index, None)),
      row(TrusteeUTRId(index))(someRefValue, cyaIndividualDetailsPage(CheckMode, index, None)),
      row(TrusteeHasUTRId(index))(false, TrusteeNoUTRReasonController.onPageLoad(CheckMode, index, None)),
      row(TrusteeNoUTRReasonId(index))(someStringValue, cyaIndividualDetailsPage(CheckMode, index, None))
    )

    behave like navigatorWithRoutesForMode(CheckMode)(navigator, checkModeRoutes(CheckMode), None)
  }

  "UpdateMode" must {
    def navigationForVarianceModeTrusteeIndividual(mode: Mode): TableFor3[Identifier, UserAnswers, Call] =
    Table(
      ("Id", "UserAnswers", "Expected next page"),
      row(TrusteeNameId(index))(somePersonNameValue, controllers.register.trustees.routes.AddTrusteeController.onPageLoad(mode, srn), Some(newTrusteeUserAnswers)),
      row(TrusteeDOBId(index))(someDate, TrusteeHasNINOController.onPageLoad(mode, index, srn), Some(newTrusteeUserAnswers)),
      row(TrusteeHasNINOId(index))(true, TrusteeNinoNewController.onPageLoad(mode, index, srn), Some(newTrusteeUserAnswers)),
      row(TrusteeHasNINOId(index))(false, TrusteeNoNINOReasonController.onPageLoad(mode, index, srn), Some(newTrusteeUserAnswers)),
      row(TrusteeNewNinoId(index))(someRefValue, TrusteeHasUTRController.onPageLoad(mode, index, srn), Some(newTrusteeUserAnswers)),
      row(TrusteeNoNINOReasonId(index))(someStringValue, TrusteeHasUTRController.onPageLoad(mode, index, srn), Some(newTrusteeUserAnswers)),
      row(TrusteeHasUTRId(index))(true, TrusteeUTRController.onPageLoad(mode, index, srn), Some(newTrusteeUserAnswers)),
      row(TrusteeHasUTRId(index))(false, TrusteeNoUTRReasonController.onPageLoad(mode, index, srn), Some(newTrusteeUserAnswers)),
      row(TrusteeNoUTRReasonId(index))(someStringValue, cyaIndividualDetailsPage(mode, index, srn), Some(newTrusteeUserAnswers)),
      row(TrusteeUTRId(index))(someRefValue, cyaIndividualDetailsPage(mode, index, srn), Some(newTrusteeUserAnswers))
    )

    behave like navigatorWithRoutesForMode(UpdateMode)(navigator, navigationForVarianceModeTrusteeIndividual(UpdateMode), srn)
  }


  "CheckUpdateMode" must {
    def navigationForVarianceModeTrusteeIndividual(mode: Mode): TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Expected next page"),
        row(TrusteeDOBId(index))(someDate, cyaIndividualDetailsPage(CheckUpdateMode, index, srn), Some(newTrusteeUserAnswers)),
        row(TrusteeHasNINOId(index))(true, TrusteeNinoNewController.onPageLoad(CheckUpdateMode, index, srn), Some(newTrusteeUserAnswers)),
        row(TrusteeNewNinoId(index))(someRefValue, cyaIndividualDetailsPage(CheckUpdateMode, index, srn), Some(newTrusteeUserAnswers)),
        row(TrusteeNewNinoId(index))(someRefValue, anyMoreChangesPage(srn), Some(exisitingTrusteeUserAnswers)),
        row(TrusteeHasNINOId(index))(false, TrusteeNoNINOReasonController.onPageLoad(CheckUpdateMode, index, srn), Some(newTrusteeUserAnswers)),
        row(TrusteeNoNINOReasonId(index))(someStringValue, cyaIndividualDetailsPage(CheckUpdateMode, index, srn), Some(newTrusteeUserAnswers)),
        row(TrusteeHasUTRId(index))(true, TrusteeUTRController.onPageLoad(CheckUpdateMode, index, srn), Some(newTrusteeUserAnswers)),
        row(TrusteeUTRId(index))(someRefValue, cyaIndividualDetailsPage(CheckUpdateMode, index, srn), Some(newTrusteeUserAnswers)),
        row(TrusteeHasUTRId(index))(false, TrusteeNoUTRReasonController.onPageLoad(CheckUpdateMode, index, srn), Some(newTrusteeUserAnswers)),
        row(TrusteeNoUTRReasonId(index))(someStringValue, cyaIndividualDetailsPage(CheckUpdateMode, index, srn), Some(newTrusteeUserAnswers))
      )

    behave like navigatorWithRoutesForMode(CheckUpdateMode)(navigator, navigationForVarianceModeTrusteeIndividual(CheckUpdateMode), srn)
  }


}

object TrusteesIndividualDetailsNavigatorSpec extends SpecBase with MustMatchers with NavigatorBehaviour with Generators {
  private lazy val index = 0
  private val someDate =  LocalDate.now()
  private val newTrusteeUserAnswers = UserAnswers().set(IsTrusteeNewId(index))(true).asOpt.value
  private val exisitingTrusteeUserAnswers = UserAnswers().set(IsTrusteeNewId(index))(false).asOpt.value
  private val srn = Some("srn")

  private def cyaIndividualDetailsPage(mode: Mode, index: Index, srn: Option[String]): Call =
    CheckYourAnswersIndividualDetailsController.onPageLoad(Mode.journeyMode(mode), index, srn)

}
