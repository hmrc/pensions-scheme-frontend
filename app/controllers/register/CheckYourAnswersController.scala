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

package controllers.register

import config.FrontendAppConfig
import controllers.actions._
import identifiers.register._
import javax.inject.Inject
import models.{CheckMode, NormalMode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.CheckYourAnswers.Ops._
import utils.annotations.Register
import utils.{CountryOptions, Enumerable, Navigator}
import viewmodels.AnswerSection
import views.html.check_your_answers

class CheckYourAnswersController @Inject()(appConfig: FrontendAppConfig,
                                           override val messagesApi: MessagesApi,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           implicit val countryOptions: CountryOptions,
                                           @Register navigator: Navigator) extends FrontendController with Enumerable.Implicits with I18nSupport {

  def onPageLoad: Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>

      val schemeDetailsSection = AnswerSection(
        Some("messages__scheme_details__title"),
          SchemeDetailsId.row(routes.SchemeDetailsController.onPageLoad(CheckMode).url) ++
          SchemeEstablishedCountryId.row(routes.SchemeEstablishedCountryController.onPageLoad(CheckMode).url) ++
          MembershipId.row(routes.MembershipController.onPageLoad(CheckMode).url) ++
          MembershipFutureId.row(routes.MembershipFutureController.onPageLoad(CheckMode).url) ++
          InvestmentRegulatedId.row(routes.InvestmentRegulatedController.onPageLoad(CheckMode).url) ++
          OccupationalPensionSchemeId.row(routes.OccupationalPensionSchemeController.onPageLoad(CheckMode).url)
      )

      val schemeBenefitsSection = AnswerSection(
        Some("messages__scheme_benefits_section"),
          BenefitsId.row(routes.BenefitsController.onPageLoad(CheckMode).url) ++
          SecuredBenefitsId.row(routes.SecuredBenefitsController.onPageLoad(CheckMode).url) ++
          BenefitsInsurerId.row(routes.BenefitsInsurerController.onPageLoad(CheckMode).url) ++
          InsurerAddressId.row(routes.InsurerAddressController.onPageLoad(CheckMode).url)
      )

      val bankAccountSection = AnswerSection(
        Some("messages__uk_bank_account_details__title"),
          UKBankAccountId.row(routes.UKBankAccountController.onPageLoad(CheckMode).url) ++
          UKBankDetailsId.row(routes.UKBankDetailsController.onPageLoad(CheckMode).url)
      )

      Ok(check_your_answers(
        appConfig,
        Seq(schemeDetailsSection, schemeBenefitsSection, bankAccountSection),
        Some("messages_cya_secondary_header"),
        routes.CheckYourAnswersController.onSubmit())
      )
  }

  def onSubmit: Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>
      Redirect(navigator.nextPage(CheckYourAnswersId, NormalMode, request.userAnswers))
  }

}
