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

import config.FrontendAppConfig
import controllers.actions._
import controllers.{CheckYourAnswersControllerCommon, Retrievals}
import identifiers.register.trustees.IsTrusteeNewId
import identifiers.register.trustees.company._
import javax.inject.Inject
import models.Mode.checkMode
import models.{Index, Mode}
import navigators.Navigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import utils.annotations.NoSuspendedCheck
import utils.checkyouranswers.Ops._
import utils.{AllowChangeHelper, CountryOptions, Enumerable, UserAnswers}
import viewmodels.{AnswerSection, CYAViewModel}
import views.html.checkYourAnswers

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersCompanyDetailsController @Inject()(
                                                          appConfig: FrontendAppConfig,
                                                          override val messagesApi: MessagesApi,
                                                          authenticate: AuthAction,
                                                          getData: DataRetrievalAction,
                                                          @NoSuspendedCheck allowAccess: AllowAccessActionProvider,
                                                          requireData: DataRequiredAction,
                                                          implicit val countryOptions: CountryOptions,
                                                          navigator: Navigator,
                                                          userAnswersService: UserAnswersService,
                                                          allowChangeHelper: AllowChangeHelper
                                                        )(implicit val ec: ExecutionContext) extends CheckYourAnswersControllerCommon
  with Retrievals with I18nSupport with Enumerable.Implicits {

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        implicit val userAnswers: UserAnswers = request.userAnswers
        val companyDetails = Seq(AnswerSection(
          None,
          HasCompanyCRNId(index).row(routes.HasCompanyCRNController.onPageLoad(checkMode(mode), index, srn).url, mode) ++
            CompanyEnterCRNId(index)
              .row(routes.CompanyEnterCRNController.onPageLoad(checkMode(mode), srn, index).url, mode) ++
            CompanyNoCRNReasonId(index).row(routes.CompanyNoCRNReasonController.onPageLoad(checkMode(mode), index, srn).url, mode) ++
            HasCompanyUTRId(index).row(routes.HasCompanyUTRController.onPageLoad(checkMode(mode), index, srn).url, mode) ++
            CompanyEnterUTRId(index).row(routes.CompanyEnterUTRController.onPageLoad(checkMode(mode), srn, index).url, mode) ++
            CompanyNoUTRReasonId(index).row(routes.CompanyNoUTRReasonController.onPageLoad(checkMode(mode), index, srn).url, mode) ++
            HasCompanyVATId(index).row(routes.HasCompanyVATController.onPageLoad(checkMode(mode), index, srn).url, mode) ++
            CompanyEnterVATId(index).row(routes.CompanyEnterVATController.onPageLoad(checkMode(mode), index, srn).url, mode) ++
            HasCompanyPAYEId(index).row(routes.HasCompanyPAYEController.onPageLoad(checkMode(mode), index, srn).url, mode) ++
            CompanyEnterPAYEId(index).row(routes.CompanyEnterPAYEController.onPageLoad(checkMode(mode), index, srn).url, mode)
        ))

        val vm = CYAViewModel(
          answerSections = companyDetails,
          href = controllers.routes.SchemeTaskListController.onPageLoad(mode, srn),
          schemeName = existingSchemeName,
          returnOverview = false,
          hideEditLinks = request.viewOnly || !userAnswers.get(IsTrusteeNewId(index)).getOrElse(true),
          srn = srn,
          hideSaveAndContinueButton = allowChangeHelper.hideSaveAndContinueButton(request, IsTrusteeNewId(index), mode),
          title = titleCompanyDetails(mode),
          h1 =  headingDetails(mode, trusteeCompanyName(index))
        )

        Future.successful(Ok(checkYourAnswers( appConfig,vm)))
    }
}
