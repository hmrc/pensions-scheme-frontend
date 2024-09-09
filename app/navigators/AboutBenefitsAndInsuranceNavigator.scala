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

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.routes._
import identifiers._
import models.TypeOfBenefits.Defined
import models._
import utils.{Enumerable, UserAnswers}

class AboutBenefitsAndInsuranceNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector,
                                                   appConfig: FrontendAppConfig
                                                  ) extends AbstractNavigator with Enumerable.Implicits {


  //scalastyle:off cyclomatic.complexity
  override protected def routeMap(from: NavigateFrom, srn: SchemeReferenceNumber): Option[NavigateTo] = {
    from.id match {
      case InvestmentRegulatedSchemeId =>
        NavigateTo.dontSave(OccupationalPensionSchemeController.onPageLoad(NormalMode, srn))
      case OccupationalPensionSchemeId => NavigateTo.dontSave(TypeOfBenefitsController.onPageLoad(NormalMode, srn))
      case TypeOfBenefitsId => typesOfBenefitsRoutes(from.userAnswers, NormalMode, srn)
      case MoneyPurchaseBenefitsId => NavigateTo.dontSave(BenefitsSecuredByInsuranceController.onPageLoad(NormalMode, srn))
      case BenefitsSecuredByInsuranceId => benefitsSecuredRoutes(from.userAnswers, NormalMode, srn)
      case InsuranceCompanyNameId => NavigateTo.dontSave(InsurancePolicyNumberController.onPageLoad(NormalMode, srn))
      case InsurancePolicyNumberId => NavigateTo.dontSave(InsurerEnterPostcodeController.onPageLoad(NormalMode, srn))
      case InsurerEnterPostCodeId => NavigateTo.dontSave(InsurerSelectAddressController.onPageLoad(NormalMode, srn))
      case InsurerSelectAddressId => checkYourAnswers(NormalMode, srn)
      case InsurerConfirmAddressId => checkYourAnswers(NormalMode, srn)
      case _ => None
    }
  }

  private def typesOfBenefitsRoutes(userAnswers: UserAnswers, mode: Mode = NormalMode, srn: SchemeReferenceNumber): Option[NavigateTo] = {
    userAnswers.get(TypeOfBenefitsId) match {
      case Some(Defined) => mode match {
        case NormalMode => NavigateTo.dontSave(BenefitsSecuredByInsuranceController.onPageLoad(NormalMode, srn))
        case CheckMode => checkYourAnswers(NormalMode, srn)
        case _ => anyMoreChanges(srn)
      }
      case _ => NavigateTo.dontSave(MoneyPurchaseBenefitsController.onPageLoad(mode, srn))
    }
  }

  private def benefitsSecuredRoutes(userAnswers: UserAnswers, mode: Mode, srn: SchemeReferenceNumber): Option[NavigateTo] = {
    userAnswers.get(BenefitsSecuredByInsuranceId) match {
      case Some(true) => NavigateTo.dontSave(InsuranceCompanyNameController.onPageLoad(mode, srn))
      case Some(false) => checkYourAnswers(mode, srn)
      case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad)
    }
  }

  override protected def editRouteMap(from: NavigateFrom, srn: SchemeReferenceNumber): Option[NavigateTo] =
    from.id match {
      case InvestmentRegulatedSchemeId => checkYourAnswers(NormalMode, srn)
      case OccupationalPensionSchemeId => checkYourAnswers(NormalMode, srn)
      case TypeOfBenefitsId => typesOfBenefitsRoutes(from.userAnswers, CheckMode, srn)
      case MoneyPurchaseBenefitsId => checkYourAnswers(NormalMode, srn)
      case BenefitsSecuredByInsuranceId => benefitsSecuredEditRoutes(from.userAnswers, CheckMode, srn)
      case InsuranceCompanyNameId => NavigateTo.dontSave(InsurancePolicyNumberController.onPageLoad(NormalMode, srn))
      case InsurancePolicyNumberId => checkYourAnswers(NormalMode, srn)
      case InsurerConfirmAddressId => checkYourAnswers(NormalMode, srn)
      case _ => None
    }

  private def benefitsSecuredEditRoutes(userAnswers: UserAnswers,
                                        mode: Mode,
                                        srn: SchemeReferenceNumber): Option[NavigateTo] = {
    userAnswers.get(BenefitsSecuredByInsuranceId) match {
      case Some(true) => NavigateTo.dontSave(InsuranceCompanyNameController.onPageLoad(mode, srn))
      case Some(false) => if (mode == CheckMode) {
        checkYourAnswers(NormalMode, srn)
      } else {
        anyMoreChanges(srn)
      }
      case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad)
    }
  }

  private def checkYourAnswers(mode: Mode, srn: SchemeReferenceNumber): Option[NavigateTo] =
    NavigateTo.dontSave(controllers.routes.CheckYourAnswersBenefitsAndInsuranceController.onPageLoad(mode, srn))

  private def anyMoreChanges(srn: SchemeReferenceNumber): Option[NavigateTo] =
    NavigateTo.dontSave(controllers.routes.AnyMoreChangesController.onPageLoad(srn))

  protected def updateRouteMap(from: NavigateFrom, srn: SchemeReferenceNumber): Option[NavigateTo] = from.id match {
    case InsurancePolicyNumberId => NavigateTo.dontSave(InsurerEnterPostcodeController.onPageLoad(CheckUpdateMode, srn))
    case _ => None
  }

  protected def checkUpdateRouteMap(from: NavigateFrom, srn: SchemeReferenceNumber): Option[NavigateTo] = {
    from.id match {
      case TypeOfBenefitsId => typesOfBenefitsRoutes(from.userAnswers, CheckUpdateMode, srn)
      case MoneyPurchaseBenefitsId => anyMoreChanges(srn)
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


