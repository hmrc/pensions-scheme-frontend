/*
 * Copyright 2017 HM Revenue & Customs
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

class CheckYourAnswersHelper(userAnswers: UserAnswers) {

  def addressYears(index: Int): Option[AnswerRow] = userAnswers.addressYears(index) map {
    x => AnswerRow("addressYears.checkYourAnswersLabel", s"addressYears.$x", true,
      controllers.register.establishers.individual.routes.AddressYearsController.onPageLoad(CheckMode, index).url)
  }

  def benefits: Option[AnswerRow] = userAnswers.benefits map {
    x => AnswerRow("benefits.checkYourAnswersLabel", s"benefits.$x", true,
      controllers.register.routes.BenefitsController.onPageLoad(CheckMode).url)
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
