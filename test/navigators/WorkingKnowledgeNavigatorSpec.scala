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

package navigators

import base.SpecBase
import controllers.actions.FakeDataRetrievalAction
import identifiers._
import models.{CheckMode, EmptyOptionalSchemeReferenceNumber, Mode, NormalMode}
import org.scalatest.prop.TableFor3
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.UserAnswers

class WorkingKnowledgeNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import WorkingKnowledgeNavigatorSpec._

  val navigator: Navigator =
    applicationBuilder(dataRetrievalAction = new FakeDataRetrievalAction(Some(Json.obj()))).build().injector.instanceOf[Navigator]

  "WorkingKnowledgeNavigator" when {

    "in NormalMode" must {
      def navigation: TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Next Page"),
          rowNoValue(AdviserNameId)(adviserEmail(NormalMode)),
          rowNoValue(AdviserEmailId)(adviserPhone(NormalMode)),
          rowNoValue(AdviserPhoneId)(adviserPostCodeLookup(NormalMode)),
          rowNoValue(AdviserAddressPostCodeLookupId)(adviserAddressList(NormalMode)),
          rowNoValue(AdviserAddressListId)(checkYourAnswersPage),
          rowNoValue(AdviserAddressId)(checkYourAnswersPage),
          rowNoValue(AdviserCheckYourAnswersId)(taskList)
        )
      behave like navigatorWithRoutesForMode(NormalMode)(navigator, navigation, EmptyOptionalSchemeReferenceNumber)
    }

    "in CheckMode" must {
      def navigation: TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Next Page"),
          rowNoValue(AdviserNameId)(adviserCYA),
          rowNoValue(AdviserEmailId)(adviserCYA),
          rowNoValue(AdviserAddressPostCodeLookupId)(adviserAddressList(CheckMode)),
          rowNoValue(AdviserAddressListId)(checkYourAnswersPage),
          rowNoValue(AdviserAddressId)(checkYourAnswersPage),
          rowNoValue(AdviserPhoneId)(adviserCYA)
        )
      behave like navigatorWithRoutesForMode(CheckMode)(navigator, navigation, EmptyOptionalSchemeReferenceNumber)
    }
  }
}

object WorkingKnowledgeNavigatorSpec {
  private def taskList: Call = controllers.routes.PsaSchemeTaskListController.onPageLoad(NormalMode, EmptyOptionalSchemeReferenceNumber)

  private def adviserAddressList(mode: Mode) = controllers.routes.AdviserAddressListController.onPageLoad(mode)

  private def adviserPostCodeLookup(mode: Mode) = controllers.routes.AdviserPostCodeLookupController.onPageLoad(mode)

  private def checkYourAnswersPage = controllers.routes.AdviserCheckYourAnswersController.onPageLoad()

  private def adviserEmail(mode: Mode = NormalMode): Call = controllers.routes.AdviserEmailAddressController.onPageLoad(mode)
  
  private def adviserPhone(mode: Mode = NormalMode): Call = controllers.routes.AdviserPhoneController.onPageLoad(mode)

  private def adviserCYA: Call = controllers.routes.AdviserCheckYourAnswersController.onPageLoad()
}
