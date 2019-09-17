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
    val navigationForNewEstablisherIndividual: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Next Page"),
        row(EstablisherNameId(index))(somePersonNameValue, addEstablisher(NormalMode, None)),
        row(EstablisherDOBId(index))(someDate, EstablisherHasNINOController.onPageLoad(NormalMode, index, None)),
        row(EstablisherHasNINOId(index))(true, EstablisherNinoNewController.onPageLoad(NormalMode, index, None)),
        row(EstablisherHasNINOId(index))(false, EstablisherNoNINOReasonController.onPageLoad(NormalMode, index, None)),
        row(EstablisherNewNinoId(index))(someRefValue, EstablisherHasUTRController.onPageLoad(NormalMode, index, None)),
        row(EstablisherNoNINOReasonId(index))(someStringValue, EstablisherHasUTRController.onPageLoad(NormalMode, index, None)),
        row(EstablisherHasUTRId(index))(true, EstablisherUTRController.onPageLoad(NormalMode, index, None)),
        row(EstablisherHasUTRId(index))(false, EstablisherNoUTRReasonController.onPageLoad(NormalMode, index, None)),
        row(EstablisherNoUTRReasonId(index))(someStringValue, CheckYourAnswersDetailsController.onPageLoad(journeyMode(NormalMode), index, None)),
        row(EstablisherUTRId(index))(someRefValue, CheckYourAnswersDetailsController.onPageLoad(journeyMode(NormalMode), index, None))
      )

    behave like navigatorWithRoutesForMode(NormalMode)(navigator, navigationForNewEstablisherIndividual, None)
  }

  "UpdateMode" must {
    val navigationForVarianceModeEstablisherIndividual: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Expected next page"),
        row(EstablisherNameId(index))(somePersonNameValue, addEstablisher(UpdateMode, srn), Some(newEstablisherUserAnswers))
      )

    behave like navigatorWithRoutesForMode(UpdateMode)(navigator, navigationForVarianceModeEstablisherIndividual, srn)
  }


}

object EstablishersIndividualDetailsNavigatorSpec extends SpecBase with MustMatchers with NavigatorBehaviour with Generators {
  private lazy val index = 0
  private val newEstablisherUserAnswers = UserAnswers().set(IsEstablisherNewId(index))(true).asOpt.value
  private val srn = Some("srn")
  private val someDate =  LocalDate.now()
  private def addEstablisher(mode: Mode, srn: Option[String]): Call = AddEstablisherController.onPageLoad(mode, srn)
}
