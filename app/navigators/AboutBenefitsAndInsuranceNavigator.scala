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

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import identifiers._
import controllers.routes._
import models.{CheckMode, Mode, NormalMode}
import utils.{Navigator, UserAnswers}

class AboutBenefitsAndInsuranceNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector, appConfig: FrontendAppConfig) extends Navigator {


  //scalastyle:off cyclomatic.complexity
  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = {
    from.id match {
      case InvestmentRegulatedSchemeId => NavigateTo.dontSave(OccupationalPensionSchemeController.onPageLoad(NormalMode))
      case OccupationalPensionSchemeId => NavigateTo.dontSave(TypeOfBenefitsController.onPageLoad(NormalMode))
      case TypeOfBenefitsId => NavigateTo.dontSave(BenefitsSecuredByInsuranceController.onPageLoad(NormalMode))
      case BenefitsSecuredByInsuranceId => benefitsSecuredRoutes(from.userAnswers)
      case InsuranceCompanyNameId => NavigateTo.dontSave(InsurancePolicyNumberController.onPageLoad(NormalMode))
      case InsurancePolicyNumberId => NavigateTo.dontSave(InsurerEnterPostcodeController.onPageLoad(NormalMode))
      case InsurerEnterPostCodeId => NavigateTo.dontSave(InsurerSelectAddressController.onPageLoad(NormalMode))
      case InsurerSelectAddressId => NavigateTo.dontSave(InsurerConfirmAddressController.onPageLoad(NormalMode))
      case InsurerConfirmAddressId => checkYourAnswers
      case _ => None
    }
  }

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] = {
    from.id match {
      case InvestmentRegulatedSchemeId => checkYourAnswers
      case OccupationalPensionSchemeId => checkYourAnswers
      case TypeOfBenefitsId => checkYourAnswers
      case BenefitsSecuredByInsuranceId => benefitsSecuredRoutes(from.userAnswers)
      case InsuranceCompanyNameId => checkYourAnswers
      case InsurancePolicyNumberId => checkYourAnswers
      case InsurerConfirmAddressId => checkYourAnswers
      case _ => None
    }
  }

  private def benefitsSecuredRoutes(userAnswers: UserAnswers): Option[NavigateTo] = {
    userAnswers.get(BenefitsSecuredByInsuranceId) match {
      case Some(true) => NavigateTo.dontSave(InsuranceCompanyNameController.onPageLoad(NormalMode))
      case Some(false) => checkYourAnswers
      case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def checkYourAnswers: Option[NavigateTo] =
    NavigateTo.dontSave(controllers.routes.CheckYourAnswersBenefitsAndInsuranceController.onPageLoad())
}


