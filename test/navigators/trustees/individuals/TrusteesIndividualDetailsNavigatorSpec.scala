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
import generators.Generators
import identifiers.Identifier
import identifiers.register.trustees.IsTrusteeNewId
import identifiers.register.trustees.individual._
import models.FeatureToggleName.SchemeRegistration
import models._
import navigators.{Navigator, NavigatorBehaviour}
import org.scalatest.matchers.must.Matchers
import org.scalatest.prop._
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.UserAnswers

import java.time.LocalDate

class TrusteesIndividualDetailsNavigatorSpec extends SpecBase with Matchers with NavigatorBehaviour with Generators {
  import TrusteesIndividualDetailsNavigatorSpec._

  val navigator: Navigator = injector.instanceOf[TrusteesIndividualDetailsNavigator]

  "NormalMode" must {
    val navigationForNewTrusteeIndividual: TableFor3[Identifier, UserAnswers, Call] =
    Table(
      ("Id", "UserAnswers", "Next Page"),
      row(TrusteeNameId(index))(somePersonNameValue, controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode, EmptyOptionalSchemeReferenceNumber)),
      row(TrusteeNameId(index))(somePersonNameValue, controllers.register.trustees.routes.PsaSchemeTaskListRegistrationTrusteeController.onPageLoad(index),Some(uaFeatureToggleOn)),
      row(TrusteeDOBId(index))(someDate, TrusteeHasNINOController.onPageLoad( NormalMode, index, EmptyOptionalSchemeReferenceNumber)),
      row(TrusteeHasNINOId(index))(true, TrusteeEnterNINOController.onPageLoad( NormalMode, index, EmptyOptionalSchemeReferenceNumber)),
      row(TrusteeHasNINOId(index))(false, TrusteeNoNINOReasonController.onPageLoad( NormalMode, index, EmptyOptionalSchemeReferenceNumber)),
      row(TrusteeEnterNINOId(index))(someRefValue, TrusteeHasUTRController.onPageLoad( NormalMode, index, EmptyOptionalSchemeReferenceNumber)),
      row(TrusteeNoNINOReasonId(index))(someStringValue, TrusteeHasUTRController.onPageLoad( NormalMode, index, EmptyOptionalSchemeReferenceNumber)),
      row(TrusteeHasUTRId(index))(true, TrusteeEnterUTRController.onPageLoad( NormalMode, index, EmptyOptionalSchemeReferenceNumber)),
      row(TrusteeHasUTRId(index))(false, TrusteeNoUTRReasonController.onPageLoad( NormalMode, index, EmptyOptionalSchemeReferenceNumber)),
      row(TrusteeNoUTRReasonId(index))(someStringValue, cyaIndividualDetailsPage( NormalMode, index, EmptyOptionalSchemeReferenceNumber)),
      row(TrusteeUTRId(index))(someRefValue, cyaIndividualDetailsPage( NormalMode, index, EmptyOptionalSchemeReferenceNumber))
    )

    behave like navigatorWithRoutesForMode(NormalMode)(navigator, navigationForNewTrusteeIndividual, None)
  }

  "CheckMode" must {
    val checkModeRoutes: TableFor3[Identifier, UserAnswers, Call] =
    Table(
      ("Id", "UserAnswers", "Expected next page"),
      row(TrusteeDOBId(index))(someDate, cyaIndividualDetailsPage(CheckMode, Index, EmptyOptionalSchemeReferenceNumber)),
      row(TrusteeHasNINOId(index))(true, TrusteeEnterNINOController.onPageLoad(CheckMode, Index, EmptyOptionalSchemeReferenceNumber)),
      row(TrusteeEnterNINOId(index))(someRefValue, cyaIndividualDetailsPage(CheckMode, Index, EmptyOptionalSchemeReferenceNumber)),
      row(TrusteeHasNINOId(index))(false, TrusteeNoNINOReasonController.onPageLoad(CheckMode, Index, EmptyOptionalSchemeReferenceNumber)),
      row(TrusteeNoNINOReasonId(index))(someStringValue, cyaIndividualDetailsPage(CheckMode, Index, EmptyOptionalSchemeReferenceNumber)),
      row(TrusteeHasUTRId(index))(true, TrusteeEnterUTRController.onPageLoad(CheckMode, Index, EmptyOptionalSchemeReferenceNumber)),
      row(TrusteeUTRId(index))(someRefValue, cyaIndividualDetailsPage(CheckMode, Index, EmptyOptionalSchemeReferenceNumber)),
      row(TrusteeHasUTRId(index))(false, TrusteeNoUTRReasonController.onPageLoad(CheckMode, Index, EmptyOptionalSchemeReferenceNumber)),
      row(TrusteeNoUTRReasonId(index))(someStringValue, cyaIndividualDetailsPage(CheckMode, Index, EmptyOptionalSchemeReferenceNumber))
    )

    behave like navigatorWithRoutesForMode(CheckMode)(navigator, checkModeRoutes, None)
  }

  "UpdateMode" must {
    val navigationForVarianceModeTrusteeIndividual: TableFor3[Identifier, UserAnswers, Call] =
    Table(
      ("Id", "UserAnswers", "Expected next page"),
      row(TrusteeNameId(index))(somePersonNameValue, controllers.register.trustees.routes.AddTrusteeController.onPageLoad(UpdateMode, OptionalSchemeReferenceNumber(srn)), Some(newTrusteeUserAnswers)),
      row(TrusteeDOBId(index))(someDate, TrusteeHasNINOController.onPageLoad(UpdateMode, index, OptionalSchemeReferenceNumber(srn)), Some(newTrusteeUserAnswers)),
      row(TrusteeHasNINOId(index))(true, TrusteeEnterNINOController.onPageLoad(UpdateMode, index, OptionalSchemeReferenceNumber(srn)), Some(newTrusteeUserAnswers)),
      row(TrusteeHasNINOId(index))(false, TrusteeNoNINOReasonController.onPageLoad(UpdateMode, index, OptionalSchemeReferenceNumber(srn)), Some(newTrusteeUserAnswers)),
      row(TrusteeEnterNINOId(index))(someRefValue, TrusteeHasUTRController.onPageLoad(UpdateMode, index, OptionalSchemeReferenceNumber(srn)), Some(newTrusteeUserAnswers)),
      row(TrusteeNoNINOReasonId(index))(someStringValue, TrusteeHasUTRController.onPageLoad(UpdateMode, index, OptionalSchemeReferenceNumber(srn)), Some(newTrusteeUserAnswers)),
      row(TrusteeHasUTRId(index))(true, TrusteeEnterUTRController.onPageLoad(UpdateMode, index, OptionalSchemeReferenceNumber(srn)), Some(newTrusteeUserAnswers)),
      row(TrusteeHasUTRId(index))(false, TrusteeNoUTRReasonController.onPageLoad(UpdateMode, index, OptionalSchemeReferenceNumber(srn)), Some(newTrusteeUserAnswers)),
      row(TrusteeNoUTRReasonId(index))(someStringValue, cyaIndividualDetailsPage(UpdateMode, index, OptionalSchemeReferenceNumber(srn)), Some(newTrusteeUserAnswers)),
      row(TrusteeUTRId(index))(someRefValue, cyaIndividualDetailsPage(UpdateMode, index, OptionalSchemeReferenceNumber(srn)), Some(newTrusteeUserAnswers))
    )

    behave like navigatorWithRoutesForMode(UpdateMode)(navigator, navigationForVarianceModeTrusteeIndividual, OptionalSchemeReferenceNumber(srn))
  }


  "CheckUpdateMode" must {
    val navigationForVarianceModeTrusteeIndividual: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Expected next page"),
        row(TrusteeDOBId(index))(someDate, cyaIndividualDetailsPage(CheckUpdateMode, index, OptionalSchemeReferenceNumber(srn)), Some(newTrusteeUserAnswers)),
        row(TrusteeHasNINOId(index))(true, TrusteeEnterNINOController.onPageLoad(CheckUpdateMode, index, OptionalSchemeReferenceNumber(srn)), Some(newTrusteeUserAnswers)),
        row(TrusteeEnterNINOId(index))(someRefValue, cyaIndividualDetailsPage(CheckUpdateMode, index, OptionalSchemeReferenceNumber(srn)), Some(newTrusteeUserAnswers)),
        row(TrusteeEnterNINOId(index))(someRefValue, anyMoreChangesPage(srn), Some(exisitingTrusteeUserAnswers)),
        row(TrusteeHasNINOId(index))(false, TrusteeNoNINOReasonController.onPageLoad(CheckUpdateMode, index, OptionalSchemeReferenceNumber(srn)), Some(newTrusteeUserAnswers)),
        row(TrusteeNoNINOReasonId(index))(someStringValue, cyaIndividualDetailsPage(CheckUpdateMode, index, OptionalSchemeReferenceNumber(srn)), Some(newTrusteeUserAnswers)),
        row(TrusteeHasUTRId(index))(true, TrusteeEnterUTRController.onPageLoad(CheckUpdateMode, index, OptionalSchemeReferenceNumber(srn)), Some(newTrusteeUserAnswers)),
        row(TrusteeUTRId(index))(someRefValue, cyaIndividualDetailsPage(CheckUpdateMode, index, OptionalSchemeReferenceNumber(srn)), Some(newTrusteeUserAnswers)),
        row(TrusteeUTRId(index))(someRefValue, anyMoreChangesPage(srn), Some(exisitingTrusteeUserAnswers)),
        row(TrusteeHasUTRId(index))(false, TrusteeNoUTRReasonController.onPageLoad(CheckUpdateMode, index, OptionalSchemeReferenceNumber(srn)), Some(newTrusteeUserAnswers)),
        row(TrusteeNoUTRReasonId(index))(someStringValue, cyaIndividualDetailsPage(CheckUpdateMode, index, OptionalSchemeReferenceNumber(srn)), Some(newTrusteeUserAnswers))
      )

    behave like navigatorWithRoutesForMode(CheckUpdateMode)(navigator, navigationForVarianceModeTrusteeIndividual, OptionalSchemeReferenceNumber(srn))
  }


}

object TrusteesIndividualDetailsNavigatorSpec extends SpecBase with Matchers with NavigatorBehaviour with Generators {
  private lazy val index = 0
  private val someDate =  LocalDate.now()
  private val newTrusteeUserAnswers = UserAnswers().set(IsTrusteeNewId(index))(true).asOpt.value
  private val exisitingTrusteeUserAnswers = UserAnswers().set(IsTrusteeNewId(index))(false).asOpt.value
  private val srn = Some(SchemeReferenceNumber("srn"))
  private val uaFeatureToggleOn = {
    val uaWithToggle = Json.obj(
      SchemeRegistration.asString -> true
    )
    UserAnswers(uaWithToggle)
  }

  private def cyaIndividualDetailsPage(mode: Mode, index: Index, srn: OptionalSchemeReferenceNumber): Call =
    CheckYourAnswersIndividualDetailsController.onPageLoad(Mode.journeyMode(mode), index, OptionalSchemeReferenceNumber(srn))

}
