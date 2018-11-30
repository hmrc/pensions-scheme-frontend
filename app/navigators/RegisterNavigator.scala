/*
 * Copyright 2018 HM Revenue & Customs
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
import identifiers.UserResearchDetailsId
import identifiers.register._
import models.{CheckMode, Mode, NormalMode}
import utils.{Navigator, UserAnswers}

//scalastyle:off cyclomatic.complexity
class RegisterNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector, appConfig: FrontendAppConfig) extends Navigator {

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] =
    from.id match {
      case ContinueRegistrationId =>
        NavigateTo.dontSave(controllers.routes.WhatYouWillNeedController.onPageLoad())
      case SchemeDetailsId =>
        NavigateTo.save(controllers.register.routes.SchemeEstablishedCountryController.onPageLoad(NormalMode))
      case SchemeEstablishedCountryId =>
        NavigateTo.save(controllers.register.routes.MembershipController.onPageLoad(NormalMode))
      case MembershipId =>
        NavigateTo.save(controllers.register.routes.MembershipFutureController.onPageLoad(NormalMode))
      case MembershipFutureId =>
        NavigateTo.save(controllers.register.routes.InvestmentRegulatedController.onPageLoad(NormalMode))
      case InvestmentRegulatedId =>
        NavigateTo.save(controllers.register.routes.OccupationalPensionSchemeController.onPageLoad(NormalMode))
      case OccupationalPensionSchemeId =>
        NavigateTo.save(controllers.register.routes.BenefitsController.onPageLoad(NormalMode))
      case BenefitsId =>
        NavigateTo.save(controllers.register.routes.SecuredBenefitsController.onPageLoad(NormalMode))
      case SecuredBenefitsId =>
        securedBenefitsRoutes(NormalMode, from.userAnswers)
      case BenefitsInsurerId =>
        NavigateTo.save(controllers.register.routes.InsurerPostCodeLookupController.onPageLoad(NormalMode))
      case InsurerPostCodeLookupId =>
        NavigateTo.save(controllers.register.routes.InsurerAddressListController.onPageLoad(NormalMode))
      case InsurerAddressListId =>
        NavigateTo.save(controllers.register.routes.InsurerAddressController.onPageLoad(NormalMode))
      case InsurerAddressId =>
        NavigateTo.save(controllers.register.routes.UKBankAccountController.onPageLoad(NormalMode))
      case UKBankAccountId =>
        uKBankAccountRoutes(NormalMode, from.userAnswers)
      case UKBankDetailsId =>
        NavigateTo.save(controllers.register.routes.CheckYourAnswersController.onPageLoad())
      case CheckYourAnswersId =>
        checkYourAnswersRoutes()
      case SchemeReviewId =>
        schemeReviewRoutes(from.userAnswers)
      case DeclarationDormantId =>
        NavigateTo.save(controllers.register.routes.DeclarationController.onPageLoad())
      case DeclarationId =>
        NavigateTo.save(controllers.register.routes.DeclarationDutiesController.onPageLoad())
      case DeclarationDutiesId =>
        declarationDutiesRoutes(from.userAnswers)
      case UserResearchDetailsId => NavigateTo.dontSave(appConfig.managePensionsSchemeOverviewUrl)
      case _ => None
    }

  private lazy val checkYourAnswers = controllers.register.routes.CheckYourAnswersController.onPageLoad()

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] =
    from.id match {
      case SchemeDetailsId =>
        NavigateTo.save(checkYourAnswers)
      case SchemeEstablishedCountryId =>
        NavigateTo.save(checkYourAnswers)
      case MembershipId =>
        NavigateTo.save(checkYourAnswers)
      case MembershipFutureId =>
        NavigateTo.save(checkYourAnswers)
      case InvestmentRegulatedId =>
        NavigateTo.save(checkYourAnswers)
      case OccupationalPensionSchemeId =>
        NavigateTo.save(checkYourAnswers)
      case BenefitsId =>
        NavigateTo.save(checkYourAnswers)
      case InsurerPostCodeLookupId =>
        NavigateTo.save(controllers.register.routes.InsurerAddressListController.onPageLoad(CheckMode))
      case InsurerAddressListId =>
        NavigateTo.save(controllers.register.routes.InsurerAddressController.onPageLoad(CheckMode))
      case InsurerAddressId =>
        NavigateTo.save(checkYourAnswers)
      case SecuredBenefitsId =>
        securedBenefitsRoutes(CheckMode, from.userAnswers)
      case BenefitsInsurerId =>
        benefitsInsurerRoutes(from.userAnswers)
      case UKBankAccountId =>
        uKBankAccountRoutes(CheckMode, from.userAnswers)
      case UKBankDetailsId =>
        NavigateTo.save(checkYourAnswers)
      case _ => None
    }

  private def benefitsInsurerRoutes(answers: UserAnswers): Option[NavigateTo] = {
    if (answers.get(InsurerAddressId).nonEmpty) {
      NavigateTo.save(checkYourAnswers)
    } else {
      NavigateTo.save(controllers.register.routes.InsurerPostCodeLookupController.onPageLoad(CheckMode))
    }
  }

  private def securedBenefitsRoutes(mode: Mode, answers: UserAnswers): Option[NavigateTo] = {
    (answers.get(SecuredBenefitsId), mode) match {
      case (Some(true), _) =>
        NavigateTo.save(controllers.register.routes.BenefitsInsurerController.onPageLoad(mode))
      case (Some(false), NormalMode) =>
        NavigateTo.save(controllers.register.routes.UKBankAccountController.onPageLoad(mode))
      case (Some(false), CheckMode) =>
        NavigateTo.save(controllers.register.routes.CheckYourAnswersController.onPageLoad())
      case (None, _) =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def uKBankAccountRoutes(mode: Mode, answers: UserAnswers): Option[NavigateTo] = {
    answers.get(UKBankAccountId) match {
      case Some(true) =>
        NavigateTo.save(controllers.register.routes.UKBankDetailsController.onPageLoad(mode))
      case Some(false) =>
        NavigateTo.save(controllers.register.routes.CheckYourAnswersController.onPageLoad())
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def schemeReviewRoutes(userAnswers: UserAnswers): Option[NavigateTo] = {
    if (userAnswers.hasCompanies) {
      NavigateTo.save(controllers.register.routes.DeclarationDormantController.onPageLoad())
    }
    else {
      NavigateTo.save(controllers.register.routes.DeclarationController.onPageLoad())
    }
  }

  private def declarationDutiesRoutes(userAnswers: UserAnswers): Option[NavigateTo] = {
    userAnswers.get(DeclarationDutiesId) match {
      case Some(true) =>
        workingKnowldgeAnswersRoutes()
      case Some(false) =>
        NavigateTo.save(controllers.register.adviser.routes.AdviserDetailsController.onPageLoad(NormalMode))
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def checkYourAnswersRoutes(): Option[NavigateTo] = {
    if (appConfig.isHubEnabled) {
      NavigateTo.save(controllers.routes.SchemeTaskListController.onPageLoad())
    } else {
      NavigateTo.save(controllers.register.establishers.routes.AddEstablisherController.onPageLoad(NormalMode))
    }
  }

  private def workingKnowldgeAnswersRoutes(): Option[NavigateTo] = {
    if (appConfig.isHubEnabled) {
      NavigateTo.save(controllers.routes.SchemeTaskListController.onPageLoad())
    } else {
      NavigateTo.dontSave(controllers.register.routes.SchemeSuccessController.onPageLoad())
    }
  }

}
