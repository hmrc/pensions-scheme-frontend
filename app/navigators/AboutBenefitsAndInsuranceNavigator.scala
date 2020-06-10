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

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.routes._
import identifiers._
import models.Mode.journeyMode
import models.{CheckMode, CheckUpdateMode, Mode, NormalMode, UpdateMode}
import utils.UserAnswers

class AboutBenefitsAndInsuranceNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector,
                                                   appConfig: FrontendAppConfig
                                                  ) extends AbstractNavigator {


  //scalastyle:off cyclomatic.complexity
  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = {
    from.id match {
      case InvestmentRegulatedSchemeId =>
        NavigateTo.dontSave(OccupationalPensionSchemeController.onPageLoad(NormalMode))
      case OccupationalPensionSchemeId => NavigateTo.dontSave(TypeOfBenefitsController.onPageLoad(NormalMode))
      case TypeOfBenefitsId => NavigateTo.dontSave(BenefitsSecuredByInsuranceController.onPageLoad(NormalMode, None))
      case BenefitsSecuredByInsuranceId => benefitsSecuredRoutes(from.userAnswers, NormalMode)
      case InsuranceCompanyNameId => NavigateTo.dontSave(InsurancePolicyNumberController.onPageLoad(NormalMode, None))
      case InsurancePolicyNumberId => NavigateTo.dontSave(InsurerEnterPostcodeController.onPageLoad(NormalMode, None))
      case InsurerEnterPostCodeId => NavigateTo.dontSave(InsurerSelectAddressController.onPageLoad(NormalMode, None))
      case InsurerSelectAddressId => checkYourAnswers(NormalMode)
      case InsurerConfirmAddressId => checkYourAnswers(NormalMode)
      case _ => None
    }
  }

  private def benefitsSecuredRoutes(userAnswers: UserAnswers, mode: Mode): Option[NavigateTo] = {
    userAnswers.get(BenefitsSecuredByInsuranceId) match {
      case Some(true) => NavigateTo.dontSave(InsuranceCompanyNameController.onPageLoad(mode, None))
      case Some(false) => checkYourAnswers(mode)
      case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] =
    from.id match {
      case InvestmentRegulatedSchemeId => checkYourAnswers(NormalMode)
      case OccupationalPensionSchemeId => checkYourAnswers(NormalMode)
      case TypeOfBenefitsId => checkYourAnswers(NormalMode)
      case BenefitsSecuredByInsuranceId => benefitsSecuredEditRoutes(from.userAnswers, CheckMode)
      case InsuranceCompanyNameId => NavigateTo.dontSave(InsurancePolicyNumberController.onPageLoad(NormalMode, None))
      case InsurancePolicyNumberId => checkYourAnswers(NormalMode)
      case InsurerConfirmAddressId => checkYourAnswers(NormalMode)
      case _ => None
    }

  private def benefitsSecuredEditRoutes(userAnswers: UserAnswers,
                                        mode: Mode,
                                        srn: Option[String] = None): Option[NavigateTo] = {
    userAnswers.get(BenefitsSecuredByInsuranceId) match {
      case Some(true) => NavigateTo.dontSave(InsuranceCompanyNameController.onPageLoad(mode, srn))
      case Some(false) => if (mode == CheckMode) {
        checkYourAnswers(NormalMode, srn)
      } else {
        anyMoreChanges(srn)
      }
      case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def checkYourAnswers(mode: Mode, srn: Option[String] = None): Option[NavigateTo] =
    NavigateTo.dontSave(controllers.routes.CheckYourAnswersBenefitsAndInsuranceController.onPageLoad(mode, srn))

  private def anyMoreChanges(srn: Option[String]): Option[NavigateTo] =
    NavigateTo.dontSave(controllers.routes.AnyMoreChangesController.onPageLoad(srn))

  protected def updateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = from.id match {
    case InsurancePolicyNumberId => NavigateTo.dontSave(InsurerEnterPostcodeController.onPageLoad(CheckUpdateMode, srn))
    case _ => None
  }

  protected def checkUpdateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = {
    from.id match {
      case BenefitsSecuredByInsuranceId => benefitsSecuredEditRoutes(from.userAnswers, CheckUpdateMode, srn)
      case InsuranceCompanyNameId => NavigateTo.dontSave(InsurancePolicyNumberController.onPageLoad(UpdateMode, srn))
      case InsurancePolicyNumberId => anyMoreChanges(srn)
      case InsurerEnterPostCodeId =>
        NavigateTo.dontSave(InsurerSelectAddressController.onPageLoad(CheckUpdateMode, srn))
      case InsurerSelectAddressId => anyMoreChanges(srn)
      case InsurerConfirmAddressId => anyMoreChanges(srn)
      case _ => None
    }
  }
}


