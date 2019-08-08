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
import generators.Generators
import identifiers.Identifier
import identifiers.register.trustees.individual._
import models.{CheckMode, CheckUpdateMode, Index, Mode, NormalMode, UpdateMode}
import org.joda.time.LocalDate
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.MustMatchers
import org.scalatest.prop._
import play.api.mvc.Call
import utils.UserAnswers
import org.scalacheck.Arbitrary.arbitrary
import controllers.register.trustees.individual.routes._
import models.Mode._
import scala.concurrent.ExecutionContext.Implicits.global

class TrusteesIndividualNavigatorSpec extends SpecBase with MustMatchers with NavigatorBehaviour with Generators {

  import TrusteesIndividualNavigatorSpec._


  val navigator: Navigator = injector.instanceOf[TrusteesIndividualNavigator]

  def normalAndUpdateModeRoutes(mode: Mode): TableFor3[Identifier, UserAnswers, Call] =
    Table(
      ("Id", "UserAnswers", "Next Page"),
      row(TrusteeDOBId(index))(someDate, controllers.register.trustees.individual.routes.TrusteeHasNINOController.onPageLoad(mode, index, None)),
      row(TrusteeHasNINOId(index))(true, controllers.register.trustees.individual.routes.TrusteeNinoNewController.onPageLoad(mode, index, None)),
      row(TrusteeHasNINOId(index))(false, controllers.register.trustees.individual.routes.TrusteeNoNINOReasonController.onPageLoad(mode, index, None)),
      row(TrusteeNewNinoId(index))(someRefValue, controllers.register.trustees.individual.routes.TrusteeHasUTRController.onPageLoad(mode, index, None)),
      row(TrusteeNoNINOReasonId(index))(someStringValue, controllers.register.trustees.individual.routes.TrusteeHasUTRController.onPageLoad(mode, index, None)),
      row(TrusteeHasUTRId(index))(true, TrusteeUTRController.onPageLoad(mode, index, None)),
      row(TrusteeHasUTRId(index))(false, TrusteeNoUTRReasonController.onPageLoad(mode, index, None)),
      row(TrusteeNoUTRReasonId(index))(someStringValue, cyaIndividualDetailsPage(mode)),
      row(TrusteeUTRId(index))(someStringValue, cyaIndividualDetailsPage(mode))
    )

  behave like navigatorWithRoutesForMode(NormalMode)(navigator, normalAndUpdateModeRoutes(NormalMode))

  "TrusteesIndividualNavigator in CheckMode" should {
    "navigate from TrusteeDOBId" in {
      val userAnswers = UserAnswers().set(TrusteeDOBId(index))(someDate).asOpt.value
      navigator.nextPage(TrusteeDOBId(index), CheckMode, userAnswers, None) mustEqual cyaIndividualDetailsPage(CheckMode)
    }
    "navigate from TrusteeHasNINOId when user answers true" in {
      val userAnswers = UserAnswers().set(TrusteeHasNINOId(index))(true).asOpt.value
      navigator.nextPage(TrusteeHasNINOId(index), CheckMode, userAnswers, None) mustEqual controllers.register.trustees.individual.routes.TrusteeNinoNewController.onPageLoad(CheckMode, index, None)
    }
    "navigate from TrusteeNewNinoId" in {
      val userAnswers = UserAnswers().set(TrusteeNewNinoId(index))(someRefValue).asOpt.value
      navigator.nextPage(TrusteeNewNinoId(index), CheckMode, userAnswers, None) mustEqual cyaIndividualDetailsPage(CheckMode)
    }
    "navigate from TrusteeHasNINOId when user answers false" in {
      val userAnswers = UserAnswers().set(TrusteeHasNINOId(index))(false).asOpt.value
      navigator.nextPage(TrusteeHasNINOId(index), CheckMode, userAnswers, None) mustEqual controllers.register.trustees.individual.routes.TrusteeNoNINOReasonController.onPageLoad(CheckMode, index, None)
    }
    "navigate from TrusteeNoNINOReasonId" in {
      val userAnswers = UserAnswers().set(TrusteeNoNINOReasonId(index))(someStringValue).asOpt.value
      navigator.nextPage(TrusteeNoNINOReasonId(index), CheckMode, userAnswers, None) mustEqual cyaIndividualDetailsPage(CheckMode)
    }
    "navigate from TrusteeHasUTRId when user answers true" in {
      val userAnswers = UserAnswers().set(TrusteeHasUTRId(index))(true).asOpt.value
      navigator.nextPage(TrusteeHasUTRId(index), CheckMode, userAnswers, None) mustEqual TrusteeUTRController.onPageLoad(CheckMode, index, None)
    }
    "navigate from TrusteeUTRId" in {
      val userAnswers = UserAnswers().set(TrusteeUTRId(index))(someStringValue).asOpt.value
      navigator.nextPage(TrusteeUTRId(index), CheckMode, userAnswers, None) mustEqual cyaIndividualDetailsPage(CheckMode)
    }
    "navigate from TrusteeHasUTRId when user answers false" in {
      val userAnswers = UserAnswers().set(TrusteeHasUTRId(index))(false).asOpt.value
      navigator.nextPage(TrusteeHasUTRId(index), CheckMode, userAnswers, None) mustEqual TrusteeNoUTRReasonController.onPageLoad(CheckMode, index, None)
    }
    "navigate from TrusteeNoUTRReasonId" in {
      val userAnswers = UserAnswers().set(TrusteeNoUTRReasonId(index))(someStringValue).asOpt.value
      navigator.nextPage(TrusteeNoUTRReasonId(index), CheckMode, userAnswers, None) mustEqual cyaIndividualDetailsPage(CheckMode)
    }
  }


//  behave like navigatorWithRoutesForMode(UpdateMode)(navigator, normalAndUpdateModeRoutes(UpdateMode))
//
//  behave like navigatorWithRoutesForMode(CheckUpdateMode)(navigator, routesCheckUpdateMode(CheckUpdateMode))
//  behave like navigatorWithRoutesForMode(CheckUpdateMode)(navigator, setNewTrusteeIdentifier(routesCheckMode(CheckUpdateMode)))

}

object TrusteesIndividualNavigatorSpec {
  private val index = 0 // intsAboveValue(-1).sample.value
  private val someDate =  LocalDate.now() // arbitrary[LocalDate].sample.value

  private def cyaIndividualDetailsPage(mode: Mode): Call = CheckYourAnswersIndividualDetailsController.onPageLoad(journeyMode(mode), index, None)
}
