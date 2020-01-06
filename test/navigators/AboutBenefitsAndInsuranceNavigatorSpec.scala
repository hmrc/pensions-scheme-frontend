/*
 * Copyright 2020 HM Revenue & Customs
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
import connectors.FakeUserAnswersCacheConnector
import controllers.routes._
import identifiers._
import models.{CheckMode, CheckUpdateMode, Mode, NormalMode, UpdateMode}
import org.scalatest.OptionValues
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.UserAnswers

class AboutBenefitsAndInsuranceNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import AboutBenefitsAndInsuranceNavigatorSpec._

  private def routes() = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
    (InvestmentRegulatedSchemeId, emptyAnswers, occupationalPension, false, Some(checkYouAnswers()), false),
    (OccupationalPensionSchemeId, emptyAnswers, typesofBenefits, false, Some(checkYouAnswers()), false),
    (TypeOfBenefitsId, emptyAnswers, benefitsSecured, false, Some(checkYouAnswers()), false),
    (BenefitsSecuredByInsuranceId, benefitsSecuredYes, insuranceCompanyName(NormalMode), false, Some(insuranceCompanyName(CheckMode)), false),
    (BenefitsSecuredByInsuranceId, benefitsSecuredNo, checkYouAnswers(), false, Some(checkYouAnswers()), false),
    (BenefitsSecuredByInsuranceId, emptyAnswers, sessionExpired, false, None, false),
    (InsuranceCompanyNameId, emptyAnswers, policyNumber(), false, Some(policyNumber(NormalMode)), false),
    (InsurancePolicyNumberId, emptyAnswers, insurerPostcode(), false, Some(checkYouAnswers()), false),
    (InsurerEnterPostCodeId, emptyAnswers, insurerAddressList(), false, None, false),
    (InsurerSelectAddressId, emptyAnswers, checkYouAnswers(), false, None, false),
    (InsurerConfirmAddressId, emptyAnswers, checkYouAnswers(), false, Some(checkYouAnswers()), false)
  )

  private def updateRoutes() = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
    (BenefitsSecuredByInsuranceId, benefitsSecuredYes, none, false, Some(insuranceCompanyName(CheckUpdateMode)), false),
    (BenefitsSecuredByInsuranceId, benefitsSecuredNo, none, false, Some(anyMoreChanges), false),
    (BenefitsSecuredByInsuranceId, emptyAnswers, none, false, Some(sessionExpired), false),
    (InsuranceCompanyNameId, emptyAnswers, none, false, Some(policyNumber(UpdateMode)), false),
    (InsurancePolicyNumberId, emptyAnswers, insurerPostcode(CheckUpdateMode), false, Some(anyMoreChanges), false),
    (InsurerEnterPostCodeId, emptyAnswers, none, false, Some(insurerAddressList(CheckUpdateMode)), false),
    (InsurerSelectAddressId, emptyAnswers, none, false, Some(anyMoreChanges), false),
    (InsurerConfirmAddressId, emptyAnswers, none, false, Some(anyMoreChanges), false)
  )

  "AboutBenefitsAndInsuranceNavigator" must {
    val navigator = new AboutBenefitsAndInsuranceNavigator(FakeUserAnswersCacheConnector, frontendAppConfig)

    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, routes(), dataDescriber)
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, updateRoutes, dataDescriber, UpdateMode)
    behave like nonMatchingNavigator(navigator)
    behave like nonMatchingNavigator(navigator, UpdateMode)
  }
}

object AboutBenefitsAndInsuranceNavigatorSpec extends OptionValues {

  private val emptyAnswers = UserAnswers(Json.obj())
  private val benefitsSecuredYes = UserAnswers().set(BenefitsSecuredByInsuranceId)(true).asOpt.value
  private val benefitsSecuredNo = UserAnswers(Json.obj()).set(BenefitsSecuredByInsuranceId)(false).asOpt.value


  private def none: Call = controllers.routes.IndexController.onPageLoad()
  private def occupationalPension: Call = OccupationalPensionSchemeController.onPageLoad(NormalMode)
  private def typesofBenefits: Call = TypeOfBenefitsController.onPageLoad(NormalMode)
  private def benefitsSecured: Call = BenefitsSecuredByInsuranceController.onPageLoad(NormalMode, None)
  private def insuranceCompanyName(mode: Mode): Call = InsuranceCompanyNameController.onPageLoad(mode, None)
  private def policyNumber(mode: Mode = NormalMode): Call = InsurancePolicyNumberController.onPageLoad(mode, None)
  private def insurerPostcode(mode: Mode = NormalMode): Call = InsurerEnterPostcodeController.onPageLoad(mode, None)
  private def insurerAddressList(mode: Mode = NormalMode): Call = InsurerSelectAddressController.onPageLoad(mode, None)
  private def insurerAddress(mode: Mode = NormalMode): Call = InsurerConfirmAddressController.onPageLoad(mode, None)
  private def checkYouAnswers(mode: Mode = NormalMode): Call = CheckYourAnswersBenefitsAndInsuranceController.onPageLoad(mode, None)
  private def anyMoreChanges: Call = controllers.routes.AnyMoreChangesController.onPageLoad(None)
  private def sessionExpired = controllers.routes.SessionExpiredController.onPageLoad()

  private def dataDescriber(answers: UserAnswers): String = answers.toString
}


