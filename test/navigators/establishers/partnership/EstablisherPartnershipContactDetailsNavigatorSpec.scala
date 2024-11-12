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

package navigators.establishers.partnership

import base.SpecBase
import controllers.actions.FakeDataRetrievalAction
import controllers.register.establishers.partnership.routes._
import generators.Generators
import identifiers.Identifier
import identifiers.register.establishers.IsEstablisherNewId
import identifiers.register.establishers.partnership.{PartnershipEmailId, PartnershipPhoneNumberId}
import models._
import navigators.{Navigator, NavigatorBehaviour}
import org.scalatest.matchers.must.Matchers
import org.scalatest.prop._
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.UserAnswers


class EstablisherPartnershipContactDetailsNavigatorSpec extends SpecBase with Matchers with NavigatorBehaviour with Generators {

  import EstablisherPartnershipContactDetailsNavigatorSpec._

  val navigator: Navigator =
    applicationBuilder(dataRetrievalAction = new FakeDataRetrievalAction(Some(Json.obj()))).build().injector.instanceOf[Navigator]

  "NormalMode" must {
    def navigationForNewEstablisherPartnership: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Next Page"),
        row(PartnershipEmailId(index))(someStringValue, PartnershipPhoneNumberController.onPageLoad(NormalMode, index, None)),
        row(PartnershipPhoneNumberId(index))(someStringValue, cyaPage(NormalMode, index, None))
      )

    behave like navigatorWithRoutesForMode(NormalMode)(navigator, navigationForNewEstablisherPartnership, None)
  }

  "CheckMode" must {
    def checkModeRoutes: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Expected next page"),
        row(PartnershipEmailId(index))(someStringValue, cyaPage(NormalMode, index, None)),
        row(PartnershipPhoneNumberId(index))(someStringValue, cyaPage(NormalMode, index, None))
      )

    behave like navigatorWithRoutesForMode(CheckMode)(navigator, checkModeRoutes, None)
  }

  "UpdateMode" must {
    def navigationForUpdateModeEstablisherPartnership: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Expected next page"),
        row(PartnershipEmailId(index))(someStringValue, PartnershipPhoneNumberController.onPageLoad(UpdateMode, index, OptionalSchemeReferenceNumber(srn))),
        row(PartnershipPhoneNumberId(index))(someStringValue, cyaPage(UpdateMode, index, OptionalSchemeReferenceNumber(srn)))
      )

    behave like navigatorWithRoutesForMode(UpdateMode)(navigator, navigationForUpdateModeEstablisherPartnership, OptionalSchemeReferenceNumber(srn))
  }

  "CheckUpdateMode" must {
    def navigationForCheckUpdateModeEstablisherPartnership: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Expected next page"),
        row(PartnershipEmailId(index))(someStringValue, cyaPage(UpdateMode, index, OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers)),
        row(PartnershipEmailId(index))(someStringValue, anyMoreChangesPage(srn)),
        row(PartnershipPhoneNumberId(index))(someStringValue, cyaPage(UpdateMode, index, OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers)),
        row(PartnershipPhoneNumberId(index))(someStringValue, anyMoreChangesPage(srn))
      )

    behave like navigatorWithRoutesForMode(CheckUpdateMode)(navigator, navigationForCheckUpdateModeEstablisherPartnership, OptionalSchemeReferenceNumber(srn))
  }

}

object EstablisherPartnershipContactDetailsNavigatorSpec extends SpecBase with Matchers with NavigatorBehaviour with Generators {
  private lazy val index            = 0
  private val newEstablisherUserAnswers = UserAnswers().set(IsEstablisherNewId(index))(true).asOpt.value
  private val srn                   = Some(SchemeReferenceNumber("srn"))

  private def cyaPage(mode: Mode, index: Index, OptionalSchemeReferenceNumber(srn): OptionalSchemeReferenceNumber): Call =
    CheckYourAnswersPartnershipContactDetailsController.onPageLoad(Mode.journeyMode(mode), index, OptionalSchemeReferenceNumber(srn))

}


