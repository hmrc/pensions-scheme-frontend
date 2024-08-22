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
import controllers.actions.FakeDataRetrievalAction
import controllers.register.establishers.individual.routes.{CheckYourAnswersContactDetailsController, EstablisherPhoneController}
import generators.Generators
import identifiers.Identifier
import identifiers.register.establishers.IsEstablisherNewId
import identifiers.register.establishers.individual.{EstablisherEmailId, EstablisherPhoneId}
import models._
import navigators.{Navigator, NavigatorBehaviour}
import org.scalatest.matchers.must.Matchers
import org.scalatest.prop._
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.UserAnswers

class EstablishersIndividualContactDetailsNavigatorSpec extends SpecBase with Matchers with NavigatorBehaviour with Generators {

  import EstablishersIndividualContactDetailsNavigatorSpec._

  val navigator: Navigator =
    applicationBuilder(dataRetrievalAction = new FakeDataRetrievalAction(Some(Json.obj()))).build().injector.instanceOf[Navigator]

  "NormalMode" must {
    def navigationForNewEstablisherIndividual: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Next Page"),
        row(EstablisherEmailId(index))(someStringValue, EstablisherPhoneController.onPageLoad(NormalMode, index, None)),
        row(EstablisherPhoneId(index))(someStringValue, cyaContactDetailsPage(NormalMode, index, None))
      )

    behave like navigatorWithRoutesForMode(NormalMode)(navigator, navigationForNewEstablisherIndividual, None)
  }

  "CheckMode" must {
    def checkModeRoutes: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Expected next page"),
        row(EstablisherEmailId(index))(someStringValue, cyaContactDetailsPage(NormalMode, index, None)),
        row(EstablisherPhoneId(index))(someStringValue, cyaContactDetailsPage(NormalMode, index, None))
      )

    behave like navigatorWithRoutesForMode(CheckMode)(navigator, checkModeRoutes, None)
  }

  "UpdateMode" must {
    def navigationForUpdateModeEstablisherIndividual: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Expected next page"),
        row(EstablisherEmailId(index))(someStringValue, EstablisherPhoneController.onPageLoad(UpdateMode, index, srn)),
        row(EstablisherPhoneId(index))(someStringValue, cyaContactDetailsPage(UpdateMode, index, srn))
      )

    behave like navigatorWithRoutesForMode(UpdateMode)(navigator, navigationForUpdateModeEstablisherIndividual, srn)
  }

  "CheckUpdateMode" must {
    def navigationForCheckUpdateModeEstablisherIndividual: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Expected next page"),
        row(EstablisherEmailId(index))(someStringValue, cyaContactDetailsPage(UpdateMode, index, srn), Some(newEstablisherUserAnswers)),
        row(EstablisherEmailId(index))(someStringValue, anyMoreChangesPage(srn)),
        row(EstablisherPhoneId(index))(someStringValue, cyaContactDetailsPage(UpdateMode, index, srn), Some(newEstablisherUserAnswers)),
        row(EstablisherPhoneId(index))(someStringValue, anyMoreChangesPage(srn))
      )

    behave like navigatorWithRoutesForMode(CheckUpdateMode)(navigator, navigationForCheckUpdateModeEstablisherIndividual, srn)
  }

}

object EstablishersIndividualContactDetailsNavigatorSpec extends SpecBase with Matchers with NavigatorBehaviour with Generators {
  private lazy val index = 0
  private val newEstablisherUserAnswers = UserAnswers().set(IsEstablisherNewId(index))(value = true).asOpt.value
  private val srn = Some("srn")

  private def cyaContactDetailsPage(mode: Mode, index: Index, srn: SchemeReferenceNumber): Call =
    CheckYourAnswersContactDetailsController.onPageLoad(Mode.journeyMode(mode), index, srn)

}


