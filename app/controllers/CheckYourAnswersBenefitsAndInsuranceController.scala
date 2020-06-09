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

package controllers

import config.FrontendAppConfig
import controllers.actions._
import identifiers._
import javax.inject.Inject
import models.Mode._
import models.{CheckUpdateMode, Mode, NormalMode, UpdateMode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.annotations.NoSuspendedCheck
import utils.checkyouranswers.Ops._
import utils.{CountryOptions, Enumerable, UserAnswers}
import viewmodels.{AnswerSection, CYAViewModel, Message}
import views.html.checkYourAnswers

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersBenefitsAndInsuranceController @Inject()(appConfig: FrontendAppConfig,
                                                               override val messagesApi: MessagesApi,
                                                               authenticate: AuthAction,
                                                               getData: DataRetrievalAction,
                                                               @NoSuspendedCheck allowAccess: AllowAccessActionProvider,
                                                               requireData: DataRequiredAction,
                                                               userAnswersService: UserAnswersService,
                                                               implicit val countryOptions: CountryOptions,
                                                               val controllerComponents: MessagesControllerComponents,
                                                               val view: checkYourAnswers
                                                              )(implicit val executionContext: ExecutionContext) extends FrontendBaseController
  with Enumerable.Implicits with I18nSupport with Retrievals {

  def onPageLoad(mode: Mode, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
    implicit request =>
      implicit val userAnswers: UserAnswers = request.userAnswers
      val benefitsAndInsuranceSection = AnswerSection(
        None,
        InvestmentRegulatedSchemeId.row(routes.InvestmentRegulatedSchemeController.onPageLoad(checkMode(mode)).url, mode) ++
          OccupationalPensionSchemeId.row(routes.OccupationalPensionSchemeController.onPageLoad(checkMode(mode)).url, mode) ++
          TypeOfBenefitsId.row(routes.TypeOfBenefitsController.onPageLoad(checkMode(mode)).url, mode) ++
          BenefitsSecuredByInsuranceId.row(routes.BenefitsSecuredByInsuranceController.onPageLoad(checkMode(mode), srn).url, mode) ++
          InsuranceCompanyNameId.row(routes.InsuranceCompanyNameController.onPageLoad(checkMode(mode), srn).url, mode) ++
          InsurancePolicyNumberId.row(routes.InsurancePolicyNumberController.onPageLoad(checkMode(mode), srn).url, mode) ++
          InsurerConfirmAddressId.row(routes.InsurerConfirmAddressController.onPageLoad(checkMode(mode), srn).url, mode)
      )

      val heading = (name: String) => if (mode == NormalMode) Message("checkYourAnswers.hs.title") else
        Message("messages__benefitsAndInsuranceDetailsFor", name)

      val vm = CYAViewModel(
        answerSections = Seq(benefitsAndInsuranceSection),
        href = controllers.routes.SchemeTaskListController.onPageLoad(mode, srn),
        schemeName = existingSchemeName,
        returnOverview = false,
        hideEditLinks = request.viewOnly,
        srn = srn,
        hideSaveAndContinueButton = mode == UpdateMode || mode == CheckUpdateMode,
        title = heading(Message("messages__theScheme")),
        h1 = heading(existingSchemeName.getOrElse(Message("messages__theScheme")))
      )

      Future.successful(Ok(view(vm)))
  }
}
