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

package navigators

import base.SpecBase
import controllers.register.trustees.individual.routes._
import generators.Generators
import identifiers.Identifier
import identifiers.register.trustees.individual._
import models.{CheckMode, CheckUpdateMode, Index, Mode, NormalMode, ReferenceValue, UpdateMode}
import org.joda.time.LocalDate
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Properties
import org.scalatest.MustMatchers
import org.scalatest.prop._
import play.api.mvc.Call
import utils.UserAnswers

class TrusteesIndividualDetailsNavigatorSpec extends SpecBase with MustMatchers with NavigatorBehaviour with Generators {
  import TrusteesIndividualDetailsNavigatorSpec._


  val navigator: Navigator = injector.instanceOf[TrusteesIndividualDetailsNavigator]

  def normalModeRoutes(mode: Mode): TableFor3[Identifier, UserAnswers, Call] =
    Table(
      ("Id", "UserAnswers", "Next Page"),
      row(TrusteeNameId(index))(somePersonNameValue, controllers.register.trustees.routes.AddTrusteeController.onPageLoad(mode, None)),
      row(TrusteeDOBId(index))(someDate, controllers.register.trustees.individual.routes.TrusteeHasNINOController.onPageLoad(mode, index, None)),
      row(TrusteeHasNINOId(index))(true, controllers.register.trustees.individual.routes.TrusteeNinoNewController.onPageLoad(mode, index, None)),
      row(TrusteeHasNINOId(index))(false, controllers.register.trustees.individual.routes.TrusteeNoNINOReasonController.onPageLoad(mode, index, None)),
      row(TrusteeNewNinoId(index))(someRefValue, controllers.register.trustees.individual.routes.TrusteeHasUTRController.onPageLoad(mode, index, None)),
      row(TrusteeNoNINOReasonId(index))(someStringValue, controllers.register.trustees.individual.routes.TrusteeHasUTRController.onPageLoad(mode, index, None)),
      row(TrusteeHasUTRId(index))(true, TrusteeUTRController.onPageLoad(mode, index, None)),
      row(TrusteeHasUTRId(index))(false, TrusteeNoUTRReasonController.onPageLoad(mode, index, None)),
      row(TrusteeNoUTRReasonId(index))(someStringValue, cyaIndividualDetailsPage(mode, index)),
      row(TrusteeUTRId(index))(someStringValue, cyaIndividualDetailsPage(mode, index))
    )

  behave like navigatorWithRoutesForMode(NormalMode)(navigator, normalModeRoutes(NormalMode))

  def checkModeRoutes(mode: Mode): TableFor3[Identifier, UserAnswers, Call] =
    Table(
      ("Id", "UserAnswers", "Expected next page"),
      row(TrusteeDOBId(index))(someDate, cyaIndividualDetailsPage(CheckMode, index)),
      row(TrusteeHasNINOId(index))(true, controllers.register.trustees.individual.routes.TrusteeNinoNewController.onPageLoad(CheckMode, index, None)),
      row(TrusteeNewNinoId(index))(someRefValue, cyaIndividualDetailsPage(CheckMode, index)),
      row(TrusteeHasNINOId(index))(false, controllers.register.trustees.individual.routes.TrusteeNoNINOReasonController.onPageLoad(CheckMode, index, None)),
      row(TrusteeNoNINOReasonId(index))(someStringValue, cyaIndividualDetailsPage(CheckMode, index)),
      row(TrusteeHasUTRId(index))(true, TrusteeUTRController.onPageLoad(CheckMode, index, None)),
      row(TrusteeUTRId(index))(someStringValue, cyaIndividualDetailsPage(CheckMode, index)),
      row(TrusteeHasUTRId(index))(false, TrusteeNoUTRReasonController.onPageLoad(CheckMode, index, None)),
      row(TrusteeNoUTRReasonId(index))(someStringValue, cyaIndividualDetailsPage(CheckMode, index))
    )

  behave like navigatorWithRoutesForMode(CheckMode)(navigator, checkModeRoutes(CheckMode))

  def updateMode(mode: Mode): TableFor3[Identifier, UserAnswers, Call] =
    Table(
      ("Id", "UserAnswers", "Next Page"),
      row(TrusteeNewNinoId(index))(someRefValue, anyMoreChangesPage)
    )

  behave like navigatorWithRoutesForMode(UpdateMode)(navigator, updateMode(UpdateMode))

}

object TrusteesIndividualDetailsNavigatorSpec extends SpecBase with MustMatchers with NavigatorBehaviour with Generators {
  private lazy val index = 0 // intsAboveValue(-1).sample.value
  private val someDate =  LocalDate.now() // arbitrary[LocalDate].sample.value

  private def cyaIndividualDetailsPage(mode: Mode, index: Index): Call = CheckYourAnswersIndividualDetailsController.onPageLoad(Mode.journeyMode(mode), index, None)

}
