/*
 * Copyright 2023 HM Revenue & Customs
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
import identifiers.register._
import identifiers.{Identifier, VariationDeclarationId}
import models._
import models.register.SchemeType
import org.scalatest.OptionValues
import org.scalatest.matchers.must.Matchers
import org.scalatest.prop.TableFor3
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.UserAnswers

//scalastyle:off line.size.limit
class RegisterNavigatorSpec extends SpecBase with Matchers with NavigatorBehaviour {

  import RegisterNavigatorSpec._

  val navigator: Navigator = applicationBuilder(dataRetrievalAction = new FakeDataRetrievalAction(Some(Json.obj()))).build().injector.instanceOf[Navigator]

  "RegisterNavigator" when {

    "in NormalMode" must {
      def navigation: TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Next Page"),
          rowNoValue(DeclarationId)(schemeSuccess, ua = Some(hasEstablishers)),
          rowNoValue(DeclarationDormantId)(declaration, ua = Some(beforeYouStartCompleted)),
          rowNoValue(ContinueRegistrationId)(taskList, ua = Some(beforeYouStartCompleted)),
          rowNoValue(ContinueRegistrationId)(beforeYouStart, ua = Some(beforeYouStartInProgress)),
          rowNoValue(ContinueRegistrationId)(beforeYouStart)
        )
      behave like navigatorWithRoutesForMode(NormalMode)(navigator, navigation, None)
    }

    "in UpdateMode" must {
      def navigation: TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Next Page"),
          rowNoValue(VariationDeclarationId)(variationSucess)
        )
      behave like navigatorWithRoutesForMode(UpdateMode)(navigator, navigation, Some("srn"))
    }
  }
}

//noinspection MutatorLikeMethodIsParameterless
object RegisterNavigatorSpec extends OptionValues{
  private val hasCompanies = UserAnswers().establisherCompanyDetails(0, CompanyDetails("test-company-name"))
  private val hasEstablishers = hasCompanies.schemeName("test-scheme-name").schemeType(SchemeType.GroupLifeDeath)
  private val beforeYouStartInProgress = UserAnswers().schemeName("Test Scheme")
  private val beforeYouStartCompleted = beforeYouStartInProgress.schemeType(SchemeType.SingleTrust).
    establishedCountry(country = "GB").declarationDuties(haveWorkingKnowledge = true)

  private def schemeSuccess = controllers.register.routes.SchemeSuccessController.onPageLoad()

  private def beforeYouStart = controllers.routes.BeforeYouStartController.onPageLoad()

  private def declaration = controllers.register.routes.DeclarationController.onPageLoad

  private def taskList: Call = controllers.routes.PsaSchemeTaskListController.onPageLoad(NormalMode, None)
  private def variationSucess: Call = controllers.register.routes.SchemeVariationsSuccessController.onPageLoad("srn")
}
