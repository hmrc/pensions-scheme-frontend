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

package controllers.register.trustees.company

import config.{FeatureSwitchManagementService, FrontendAppConfig}
import controllers.Retrievals
import controllers.actions._
import identifiers.register.trustees.IsTrusteeNewId
import identifiers.register.trustees.company._
import javax.inject.Inject
import models.Mode.checkMode
import models.{Index, Mode}
import navigators.Navigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.{NoSuspendedCheck, TrusteesCompany}
import utils.{AllowChangeHelper, CountryOptions, Enumerable, UserAnswers}
import viewmodels.AnswerSection
import utils.checkyouranswers.Ops._
import views.html.check_your_answers
import utils.annotations.TrusteesCompany
import utils.Enumerable

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersCompanyDetailsController @Inject()(
                                                          appConfig: FrontendAppConfig,
                                                          override val messagesApi: MessagesApi,
                                                          authenticate: AuthAction,
                                                          getData: DataRetrievalAction,
                                                          @NoSuspendedCheck allowAccess: AllowAccessActionProvider,
                                                          requireData: DataRequiredAction,
                                                          implicit val countryOptions: CountryOptions,
                                                          @TrusteesCompany navigator: Navigator,
                                                          userAnswersService: UserAnswersService,
                                                          allowChangeHelper: AllowChangeHelper,
                                                          fs: FeatureSwitchManagementService
                                                        )(implicit val ec: ExecutionContext) extends FrontendController
  with Retrievals with I18nSupport with Enumerable.Implicits {

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        implicit val userAnswers: UserAnswers = request.userAnswers
        val companyDetails = Seq(AnswerSection(
          None,
          HasCompanyNumberId(index).row(routes.HasCompanyNumberController.onPageLoad(checkMode(mode), index, srn).url, mode) ++
            CompanyRegistrationNumberVariationsId(index)
              .row(routes.CompanyRegistrationNumberVariationsController.onPageLoad(checkMode(mode), srn, index).url, mode) ++
            NoCompanyNumberId(index).row(routes.NoCompanyNumberController.onPageLoad(checkMode(mode), index, srn).url, mode) ++
            HasCompanyUTRId(index).row(routes.HasCompanyUTRController.onPageLoad(checkMode(mode), index, srn).url, mode) ++
            CompanyUTRId(index).row(routes.CompanyUTRController.onPageLoad(checkMode(mode), srn, index).url, mode) ++
            CompanyNoUTRReasonId(index).row(routes.CompanyNoUTRReasonController.onPageLoad(checkMode(mode), index, srn).url, mode) ++
            HasCompanyVATId(index).row(routes.HasCompanyVATController.onPageLoad(checkMode(mode), index, srn).url, mode) ++
            CompanyVatVariationsId(index).row(routes.CompanyVatVariationsController.onPageLoad(checkMode(mode), index, srn).url, mode) ++
            HasCompanyPAYEId(index).row(routes.HasCompanyPAYEController.onPageLoad(checkMode(mode), index, srn).url, mode) ++
            CompanyPayeVariationsId(index).row(routes.CompanyPayeVariationsController.onPageLoad(checkMode(mode), index, srn).url, mode)
        ))

        Future.successful(Ok(check_your_answers(
          appConfig,
          companyDetails,
          routes.CheckYourAnswersCompanyDetailsController.onSubmit(mode, index, srn),
          existingSchemeName,
          mode = mode,
          hideEditLinks = request.viewOnly || !userAnswers.get(IsTrusteeNewId(index)).getOrElse(true),
          hideSaveAndContinueButton = allowChangeHelper.hideSaveAndContinueButton(request, IsTrusteeNewId(index), mode),
          srn = srn
        )))

    }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] = (
    authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      userAnswersService.setCompleteFlag(mode, srn, IsDetailsCompleteId(index), request.userAnswers, true).map { _ =>
        Redirect(controllers.routes.SchemeTaskListController.onPageLoad(mode, srn))
      }
  }

}
