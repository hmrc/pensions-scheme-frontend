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

package utils

import models.CheckMode
import viewmodels.AnswerRow
import controllers.register.routes

import scala.util.Success

class CheckYourAnswersHelper(userAnswers: UserAnswers) {

  def establisherKind: Option[AnswerRow] = userAnswers.establisherKind map {
    x => AnswerRow("establisherKind.checkYourAnswersLabel", s"establisherKind.$x", true, controllers.register.establishers.routes.EstablisherKindController.onPageLoad(CheckMode).url)
  }

  def schemeEstablishedCountry: Option[AnswerRow] = userAnswers.schemeEstablishedCountry map {
    x => AnswerRow("schemeEstablishedCountry.checkYourAnswersLabel", s"$x", false, routes.SchemeEstablishedCountryController.onPageLoad(CheckMode).url)
  }

  def uKBankAccount: Option[AnswerRow] = userAnswers.uKBankAccount map {
    x => AnswerRow("uKBankAccount.checkYourAnswersLabel", if(x) "site.yes" else "site.no", true, routes.UKBankAccountController.onPageLoad(CheckMode).url)
  }

  def uKBankDetails: Option[AnswerRow] = userAnswers.uKBankDetails map {
    x => AnswerRow("uKBankDetails.checkYourAnswersLabel", s"${x.accountName} ${x.bankName}", false, routes.UKBankDetailsController.onPageLoad(CheckMode).url)
  }

  def addressYears(index: Int): Option[AnswerRow] = {
    userAnswers.addressYears(index) match {
      case Success(Some(x)) => Some(AnswerRow("addressYears.checkYourAnswersLabel", s"addressYears.$x", true,
        controllers.register.establishers.individual.routes.AddressYearsController.onPageLoad(CheckMode, index).url))
      case _ => None
    }
  }

  def benefits: Option[AnswerRow] = userAnswers.benefits map {
    x => AnswerRow("benefits.checkYourAnswersLabel", s"benefits.$x", true,
      routes.BenefitsController.onPageLoad(CheckMode).url)
  }

  def benefitsInsurer: Option[AnswerRow] = userAnswers.benefitsInsurer map {
    x => AnswerRow("benefitsInsurer.checkYourAnswersLabel", s"${x.companyName} ${x.policyNumber}", false,
      routes.BenefitsInsurerController.onPageLoad(CheckMode).url)
  }

  def membership: Option[AnswerRow] = userAnswers.membership map {
    x => AnswerRow("membership.checkYourAnswersLabel", s"membership.$x", true,
      routes.MembershipController.onPageLoad(CheckMode).url)
  }

  def membershipFuture: Option[AnswerRow] = userAnswers.membershipFuture map {
    x => AnswerRow("membershipFuture.checkYourAnswersLabel", s"membershipFuture.$x", true,
      routes.MembershipFutureController.onPageLoad(CheckMode).url)
  }

  def investmentRegulated: Option[AnswerRow] = userAnswers.investmentRegulated map {
    x => AnswerRow("investmentRegulated.checkYourAnswersLabel", if(x) "site.yes" else "site.no", true,
      routes.InvestmentRegulatedController.onPageLoad(CheckMode).url)
  }

  def securedBenefits: Option[AnswerRow] = userAnswers.securedBenefits map {
    x => AnswerRow("securedBenefits.checkYourAnswersLabel", if(x) "site.yes" else "site.no", true,
      routes.SecuredBenefitsController.onPageLoad(CheckMode).url)
  }

  def occupationalPensionScheme: Option[AnswerRow] = userAnswers.occupationalPensionScheme map {
    x => AnswerRow("occupationalPensionScheme.checkYourAnswersLabel", if(x) "site.yes" else "site.no", true,
      routes.OccupationalPensionSchemeController.onPageLoad(CheckMode).url)
  }

  def schemeDetails: Option[AnswerRow] = userAnswers.schemeDetails map {
    x => AnswerRow("schemeDetails.checkYourAnswersLabel", s"${x.schemeName} ${x.schemeType}", false,
      routes.SchemeDetailsController.onPageLoad(CheckMode).url)
  }
}
