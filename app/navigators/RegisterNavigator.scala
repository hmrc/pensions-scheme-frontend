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

import com.google.inject.{Inject, Singleton}
import identifiers.Identifier
import identifiers.register._
import models.NormalMode
import models.register.Benefits
import models.register.establishers._
import play.api.mvc.Call
import utils.{Navigator, UserAnswers}

@Singleton
class RegisterNavigator @Inject() extends Navigator {

  override protected val routeMap: PartialFunction[Identifier, UserAnswers => Call] = {
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
  }

  private def securedBenefitsRoutes()(answers: UserAnswers): Call = {
    answers.get(SecuredBenefitsId) match {
      case Some(true) =>
        controllers.register.routes.BenefitsInsurerController.onPageLoad(NormalMode)
      case Some(false) =>
        controllers.register.routes.UKBankAccountController.onPageLoad(NormalMode)
      case None =>
        controllers.routes.SessionExpiredController.onPageLoad()
    }
  }
}