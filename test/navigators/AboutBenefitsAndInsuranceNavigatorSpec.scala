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
import models.MoneyPurchaseBenefits._
import models.TypeOfBenefits._
import models._
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor3
import play.api.libs.json.{JsString, Json, Writes}
import play.api.mvc.Call
import utils.{Enumerable, UserAnswers}

class AboutBenefitsAndInsuranceNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import AboutBenefitsAndInsuranceNavigatorSpec._

  val navigator: Navigator =
    applicationBuilder(dataRetrievalAction = new FakeDataRetrievalAction(Some(Json.obj()))).build().injector.instanceOf[Navigator]

  "AboutBenefitsAndInsuranceNavigator" when {

    "in NormalMode" must {
      def navigation: TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Next Page"),
          row(InvestmentRegulatedSchemeId)(false, occupationalPension),
          row(OccupationalPensionSchemeId)(false, typesofBenefits),
          row(TypeOfBenefitsId)(Defined, benefitsSecured),
          row(TypeOfBenefitsId)(MoneyPurchase, moneyPurchaseBenefits()),
          row(MoneyPurchaseBenefitsId)(Other, benefitsSecured),
          row(BenefitsSecuredByInsuranceId)(false, checkYouAnswers()),
          row(BenefitsSecuredByInsuranceId)(true, insuranceCompanyName(NormalMode)),
          row(InsuranceCompanyNameId)(someStringValue, policyNumber()),
          row(InsurancePolicyNumberId)(someStringValue, insurerPostcode()),
          row(InsurerEnterPostCodeId)(someSeqTolerantAddress, insurerAddressList()),
          row(InsurerSelectAddressId)(someTolerantAddress, checkYouAnswers()),
          row(InsurerConfirmAddressId)(someAddress, checkYouAnswers())
        )
      behave like navigatorWithRoutesForMode(NormalMode)(navigator, navigation ,EmptyOptionalSchemeReferenceNumber)
    }

    "in CheckMode" must {
      def navigation: TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Next Page"),
          row(InvestmentRegulatedSchemeId)(false, checkYouAnswers()),
          row(OccupationalPensionSchemeId)(false, checkYouAnswers()),
          row(TypeOfBenefitsId)(Defined, checkYouAnswers()),
          row(TypeOfBenefitsId)(MoneyPurchase, moneyPurchaseBenefits(CheckMode)),
          row(MoneyPurchaseBenefitsId)(CashBalance, checkYouAnswers()),
          row(BenefitsSecuredByInsuranceId)(false, checkYouAnswers()),
          row(BenefitsSecuredByInsuranceId)(true, insuranceCompanyName(CheckMode)),
          row(InsuranceCompanyNameId)(someStringValue, policyNumber(NormalMode)),
          row(InsurancePolicyNumberId)(someStringValue, checkYouAnswers()),
          row(InsurerConfirmAddressId)(someAddress, checkYouAnswers())
        )
      behave like navigatorWithRoutesForMode(CheckMode)(navigator, navigation ,EmptyOptionalSchemeReferenceNumber)
    }

    "in UpdateMode" must {
      def navigation: TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Next Page"),
          row(InsurancePolicyNumberId)(someStringValue, insurerPostcode(CheckUpdateMode))
        )
      behave like navigatorWithRoutesForMode(UpdateMode)(navigator, navigation ,EmptyOptionalSchemeReferenceNumber)
    }

    "in CheckUpdateMode" must {
      def navigation: TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Next Page"),
          row(BenefitsSecuredByInsuranceId)(false, anyMoreChanges),
          row(BenefitsSecuredByInsuranceId)(true, insuranceCompanyName(CheckUpdateMode)),
          row(InsuranceCompanyNameId)(someStringValue, policyNumber(UpdateMode)),
          row(InsurancePolicyNumberId)(someStringValue, anyMoreChanges),
          row(InsurerEnterPostCodeId)(someSeqTolerantAddress, insurerAddressList(CheckUpdateMode)),
          row(InsurerSelectAddressId)(someTolerantAddress, anyMoreChanges),
          row(InsurerConfirmAddressId)(someAddress, anyMoreChanges)
        )
      behave like navigatorWithRoutesForMode(CheckUpdateMode)(navigator, navigation ,EmptyOptionalSchemeReferenceNumber)
    }

  }

}

object AboutBenefitsAndInsuranceNavigatorSpec extends OptionValues {

  private implicit def writes[A: Enumerable]: Writes[A] = Writes(value => JsString(value.toString))

  private def occupationalPension: Call                               = OccupationalPensionSchemeController.onPageLoad(NormalMode)
  private def typesofBenefits: Call                                   = TypeOfBenefitsController.onPageLoad(NormalMode, EmptyOptionalSchemeReferenceNumber)
  private def moneyPurchaseBenefits(mode: Mode = NormalMode): Call    = MoneyPurchaseBenefitsController.onPageLoad(mode ,EmptyOptionalSchemeReferenceNumber)
  private def benefitsSecured: Call                                   = BenefitsSecuredByInsuranceController.onPageLoad(NormalMode, EmptyOptionalSchemeReferenceNumber)
  private def insuranceCompanyName(mode: Mode): Call                  = InsuranceCompanyNameController.onPageLoad(mode ,EmptyOptionalSchemeReferenceNumber)
  private def policyNumber(mode: Mode = NormalMode): Call             = InsurancePolicyNumberController.onPageLoad(mode ,EmptyOptionalSchemeReferenceNumber)
  private def insurerPostcode(mode: Mode = NormalMode): Call          = InsurerEnterPostcodeController.onPageLoad(mode ,EmptyOptionalSchemeReferenceNumber)
  private def insurerAddressList(mode: Mode = NormalMode): Call       = InsurerSelectAddressController.onPageLoad(mode ,EmptyOptionalSchemeReferenceNumber)
  private def checkYouAnswers(mode: Mode = NormalMode): Call          = CheckYourAnswersBenefitsAndInsuranceController.onPageLoad(mode ,EmptyOptionalSchemeReferenceNumber)
  private def anyMoreChanges: Call                                    = controllers.routes.AnyMoreChangesController.onPageLoad(EmptyOptionalSchemeReferenceNumber)
}
