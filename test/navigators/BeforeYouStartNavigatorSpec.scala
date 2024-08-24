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
import controllers.routes._
import identifiers._
import models.register.SchemeType
import models.{CheckMode, NormalMode}
import org.scalatest.prop.TableFor3
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.UserAnswers

class BeforeYouStartNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import BeforeYouStartNavigatorSpec._

  val navigator: Navigator =
    applicationBuilder(dataRetrievalAction = new FakeDataRetrievalAction(Some(Json.obj()))).build().injector.instanceOf[Navigator]

  "BeforeYouStartNavigator" when {

    "in NormalMode" must {
      def navigation: TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Next Page"),
          row(SchemeNameId)(someStringValue, schemeTypePage),
          row(SchemeTypeId)(SchemeType.GroupLifeDeath, haveAnyTrusteesPage),
          row(SchemeTypeId)(SchemeType.SingleTrust, establishedCountryPage),
          rowNoValue(HaveAnyTrusteesId)(establishedCountryPage),
          row(EstablishedCountryId)(someStringValue, workingKnowledgePage),
          rowNoValue(DeclarationDutiesId)(checkYourAnswersPage)
        )
      behave like navigatorWithRoutesForMode(NormalMode)(navigator, navigation, None)
    }

    "in CheckMode" must {
      def navigation: TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Next Page"),
          row(SchemeNameId)(someStringValue, checkYourAnswersPage),
          row(SchemeTypeId)(SchemeType.GroupLifeDeath, haveAnyTrusteesCheckPage),
          row(SchemeTypeId)(SchemeType.SingleTrust, checkYourAnswersPage),
          rowNoValue(HaveAnyTrusteesId)(checkYourAnswersPage),
          row(EstablishedCountryId)(someStringValue, checkYourAnswersPage),
          rowNoValue(DeclarationDutiesId)(checkYourAnswersPage)
        )
      behave like navigatorWithRoutesForMode(CheckMode)(navigator, navigation, None)
    }
  }
}

object BeforeYouStartNavigatorSpec {
  private val schemeTypePage: Call           = SchemeTypeController.onPageLoad(NormalMode)
  private val haveAnyTrusteesPage: Call      = HaveAnyTrusteesController.onPageLoad(NormalMode)
  private val haveAnyTrusteesCheckPage: Call = HaveAnyTrusteesController.onPageLoad(CheckMode)
  private val establishedCountryPage: Call   = EstablishedCountryController.onPageLoad(NormalMode)
  private val workingKnowledgePage: Call     = WorkingKnowledgeController.onPageLoad(NormalMode)
  private val checkYourAnswersPage: Call     = controllers.routes.CheckYourAnswersBeforeYouStartController.onPageLoad(NormalMode, srn)
}
