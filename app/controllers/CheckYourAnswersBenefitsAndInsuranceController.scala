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

package controllers

import config.{FeatureSwitchManagementService, FrontendAppConfig}
import controllers.actions._
import identifiers._
import javax.inject.Inject
import models.CheckMode
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.Toggles.enableHubV2
import utils.checkyouranswers.Ops._
import utils.{CountryOptions, Enumerable, SectionComplete}
import viewmodels.AnswerSection
import views.html.check_your_answers

import scala.concurrent.ExecutionContext

class CheckYourAnswersBenefitsAndInsuranceController @Inject()(appConfig: FrontendAppConfig,
                                                               override val messagesApi: MessagesApi,
                                                               authenticate: AuthAction,
                                                               getData: DataRetrievalAction,
                                                               requireData: DataRequiredAction,
                                                               sectionComplete: SectionComplete,
                                                               fs: FeatureSwitchManagementService,
                                                               implicit val countryOptions: CountryOptions
                                                              )(implicit val ec: ExecutionContext) extends FrontendController with Enumerable.Implicits with I18nSupport {

  def onPageLoad: Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>
      implicit val userAnswers = request.userAnswers
      val benefitsAndInsuranceSection = AnswerSection(
        None,
        InvestmentRegulatedSchemeId.row(routes.InvestmentRegulatedSchemeController.onPageLoad(CheckMode).url) ++
          OccupationalPensionSchemeId.row(routes.OccupationalPensionSchemeController.onPageLoad(CheckMode).url) ++
          TypeOfBenefitsId.row(routes.TypeOfBenefitsController.onPageLoad(CheckMode).url) ++
          BenefitsSecuredByInsuranceId.row(routes.BenefitsSecuredByInsuranceController.onPageLoad(CheckMode).url) ++
          InsuranceCompanyNameId.row(routes.InsuranceCompanyNameController.onPageLoad(CheckMode).url) ++
          InsurancePolicyNumberId.row(routes.InsurancePolicyNumberController.onPageLoad(CheckMode).url) ++
          InsurerConfirmAddressId.row(routes.InsurerConfirmAddressController.onPageLoad(CheckMode).url)
      )
      Ok(check_your_answers(
        appConfig,
        Seq(benefitsAndInsuranceSection),
        routes.CheckYourAnswersBenefitsAndInsuranceController.onSubmit(),
        fs.get(enableHubV2)
      ))
  }

  def onSubmit: Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      sectionComplete.setCompleteFlag(request.externalId, IsAboutBenefitsAndInsuranceCompleteId, request.userAnswers, value = true) map { _ =>
        Redirect(controllers.register.routes.SchemeTaskListController.onPageLoad())
      }
  }

}
