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
import connectors.FakeUserAnswersCacheConnector
import controllers.routes._
import identifiers._
import models.NormalMode
import org.scalatest.OptionValues
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.UserAnswers

class AboutBenefitsAndInsuranceNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import AboutBenefitsAndInsuranceNavigatorSpec._

  private def routes() = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
    (InvestmentRegulatedSchemeId, emptyAnswers, occupationalPension, false, Some(checkYouAnswers), false),
    (OccupationalPensionSchemeId, emptyAnswers, typesofBenefits, false, Some(checkYouAnswers), false),
    (TypeOfBenefitsId, emptyAnswers, benefitsSecured, false, Some(checkYouAnswers), false),
    (BenefitsSecuredByInsuranceId, benefitsSecuredYes, insuranceCompanyName, false, Some(insuranceCompanyName), false),
    (BenefitsSecuredByInsuranceId, benefitsSecuredNo, checkYouAnswers, false, Some(checkYouAnswers), false),
    (InsuranceCompanyNameId, emptyAnswers, policyNumber, false, Some(checkYouAnswers), false),
    (InsurancePolicyNumberId, emptyAnswers, insurerPostcode, false, None, false),
    (InsurerEnterPostCodeId, emptyAnswers, insurerAddressList, false, None, false),
    (InsurerSelectAddressId, emptyAnswers, insurerAddress, false, None, false),
    (InsurerConfirmAddressId, emptyAnswers, checkYouAnswers, false, None, false)
  )

  "AboutBenefitsAndInsuranceNavigator" must {
    val navigator = new AboutBenefitsAndInsuranceNavigator(FakeUserAnswersCacheConnector, frontendAppConfig)

    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, routes(), dataDescriber)
    behave like nonMatchingNavigator(navigator)
  }
}

object AboutBenefitsAndInsuranceNavigatorSpec extends OptionValues {

  private val emptyAnswers = UserAnswers(Json.obj())
  private val benefitsSecuredYes = UserAnswers().set(BenefitsSecuredByInsuranceId)(true).asOpt.value
  private val benefitsSecuredNo = UserAnswers(Json.obj()).set(BenefitsSecuredByInsuranceId)(false).asOpt.value


  private def occupationalPension: Call = OccupationalPensionSchemeController.onPageLoad(NormalMode)
  private def typesofBenefits: Call = TypeOfBenefitsController.onPageLoad(NormalMode)
  private def benefitsSecured: Call = BenefitsSecuredByInsuranceController.onPageLoad(NormalMode)
  private def insuranceCompanyName: Call = InsuranceCompanyNameController.onPageLoad(NormalMode, None)
  private def policyNumber: Call = InsurancePolicyNumberController.onPageLoad(NormalMode)
  private def insurerPostcode: Call = InsurerEnterPostcodeController.onPageLoad(NormalMode)
  private def insurerAddressList: Call = InsurerSelectAddressController.onPageLoad(NormalMode)
  private def insurerAddress: Call = InsurerConfirmAddressController.onPageLoad(NormalMode)
  private def checkYouAnswers: Call = CheckYourAnswersBenefitsAndInsuranceController.onPageLoad()

  private def dataDescriber(answers: UserAnswers): String = answers.toString
}


