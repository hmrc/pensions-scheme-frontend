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

import com.google.inject.Singleton
import identifiers.Identifier
import identifiers.register._
import models.register.{SchemeDetails, SchemeType}
import models.{CheckMode, Mode, NormalMode}
import play.api.mvc.Call
import utils.{Enumerable, Navigator, UserAnswers}

@Singleton
class RegisterNavigator extends Navigator with Enumerable.Implicits {

  override protected val routeMap: PartialFunction[Identifier, UserAnswers => Call] = {
    case WhatYouWillNeedId =>
      _ => controllers.register.routes.SchemeDetailsController.onPageLoad(NormalMode)
    case SchemeDetailsId =>
      _ => controllers.register.routes.SchemeEstablishedCountryController.onPageLoad(NormalMode)
    case SchemeEstablishedCountryId =>
      _ => controllers.register.routes.MembershipController.onPageLoad(NormalMode)
    case MembershipId =>
      _ => controllers.register.routes.MembershipFutureController.onPageLoad(NormalMode)
    case MembershipFutureId =>
      _ => controllers.register.routes.InvestmentRegulatedController.onPageLoad(NormalMode)
    case InvestmentRegulatedId =>
      _ => controllers.register.routes.OccupationalPensionSchemeController.onPageLoad(NormalMode)
    case OccupationalPensionSchemeId =>
      _ => controllers.register.routes.BenefitsController.onPageLoad(NormalMode)
    case BenefitsId =>
      _ => controllers.register.routes.SecuredBenefitsController.onPageLoad(NormalMode)
    case SecuredBenefitsId =>
      securedBenefitsRoutes(NormalMode)
    case BenefitsInsurerId =>
      _ => controllers.register.routes.InsurerPostCodeLookupController.onPageLoad(NormalMode)
    case InsurerPostCodeLookupId =>
      _ => controllers.register.routes.InsurerAddressListController.onPageLoad(NormalMode)
    case InsurerAddressListId =>
      _ => controllers.register.routes.InsurerAddressController.onPageLoad(NormalMode)
    case InsurerAddressId =>
      _ => controllers.register.routes.UKBankAccountController.onPageLoad(NormalMode)
    case UKBankAccountId =>
      uKBankAccountRoutes(NormalMode)
    case UKBankDetailsId =>
      _ => controllers.register.routes.CheckYourAnswersController.onPageLoad()
    case CheckYourAnswersId =>
      checkYourAnswersRoutes()
    case SchemeReviewId =>
      schemeReviewRoutes()
    case DeclarationDormantId =>
      _ => controllers.register.routes.DeclarationController.onPageLoad()
    case DeclarationId =>
      _ => controllers.register.routes.DeclarationDutiesController.onPageLoad()
    case DeclarationDutiesId =>
      declarationDutiesRoutes()
  }

  private lazy val checkYourAnswers = controllers.register.routes.CheckYourAnswersController.onPageLoad()

  // scalastyle:off cyclomatic.complexity
  override protected def editRouteMap: PartialFunction[Identifier, UserAnswers => Call] = {
    case SchemeDetailsId =>
      _ => checkYourAnswers
    case SchemeEstablishedCountryId =>
      _ => checkYourAnswers
    case MembershipId =>
      _ => checkYourAnswers
    case MembershipFutureId =>
      _ => checkYourAnswers
    case InvestmentRegulatedId =>
      _ => checkYourAnswers
    case OccupationalPensionSchemeId =>
      _ => checkYourAnswers
    case BenefitsId =>
      _ => checkYourAnswers
    case InsurerPostCodeLookupId =>
      _ => controllers.register.routes.InsurerAddressListController.onPageLoad(CheckMode)
    case InsurerAddressListId =>
      _ => controllers.register.routes.InsurerAddressController.onPageLoad(CheckMode)
    case InsurerAddressId =>
      _ => checkYourAnswers
    case SecuredBenefitsId =>
      securedBenefitsRoutes(CheckMode)
    case BenefitsInsurerId =>
      _ => checkYourAnswers
    case UKBankAccountId =>
      uKBankAccountRoutes(CheckMode)
    case UKBankDetailsId =>
      _ => checkYourAnswers
  }
  // scalastyle:on cyclomatic.complexity

  private def securedBenefitsRoutes(mode: Mode)(answers: UserAnswers): Call = {
    (answers.get(SecuredBenefitsId), mode) match {
      case (Some(true), _) =>
        controllers.register.routes.BenefitsInsurerController.onPageLoad(mode)
      case (Some(false), NormalMode) =>
        controllers.register.routes.UKBankAccountController.onPageLoad(mode)
      case (Some(false), CheckMode) =>
        controllers.register.routes.CheckYourAnswersController.onPageLoad()
      case (None, _) =>
        controllers.routes.SessionExpiredController.onPageLoad()
    }
  }

  private def uKBankAccountRoutes(mode: Mode)(answers: UserAnswers): Call = {
    answers.get(UKBankAccountId) match {
      case Some(true) =>
        controllers.register.routes.UKBankDetailsController.onPageLoad(mode)
      case Some(false) =>
        controllers.register.routes.CheckYourAnswersController.onPageLoad()
      case None =>
        controllers.routes.SessionExpiredController.onPageLoad()
    }
  }

  private def schemeReviewRoutes()(userAnswers: UserAnswers): Call = {
    if (userAnswers.hasCompanies) {
      controllers.register.routes.DeclarationDormantController.onPageLoad()
    }
    else {
      controllers.register.routes.DeclarationController.onPageLoad()
    }
  }

  private def declarationDutiesRoutes()(userAnswers: UserAnswers): Call = {
    userAnswers.get(DeclarationDutiesId) match {
      case Some(true) =>
        controllers.register.routes.SchemeSuccessController.onPageLoad()
      case Some(false) =>
        controllers.register.adviser.routes.AdviserDetailsController.onPageLoad(NormalMode)
      case None =>
        controllers.routes.SessionExpiredController.onPageLoad()
    }
  }

  private def checkYourAnswersRoutes()(userAnswers: UserAnswers): Call = {
    if (userAnswers.allEstablishers.isEmpty) {
      controllers.register.establishers.routes.EstablisherKindController.onPageLoad(NormalMode, 0)
    }
    else if (userAnswers.allTrustees.nonEmpty) {
      controllers.register.routes.SchemeReviewController.onPageLoad()
    }
    else {
      userAnswers.get(SchemeDetailsId) match {
        case Some(SchemeDetails(_, schemeType)) if schemeType == SchemeType.SingleTrust =>
          controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode)
        case _ =>
          controllers.register.routes.SchemeReviewController.onPageLoad()
      }
    }
  }

}
