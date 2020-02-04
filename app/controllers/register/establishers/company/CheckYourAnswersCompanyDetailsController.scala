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

package controllers.register.establishers.company

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import controllers.routes._
import identifiers.register.establishers.IsEstablisherNewId
import identifiers.register.establishers.company._
import javax.inject.Inject
import models.Mode.checkMode
import models.{Index, Mode}
import navigators.Navigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.{FrontendBaseController, FrontendController}
import utils._
import utils.annotations.{EstablishersCompany, NoSuspendedCheck}
import utils.checkyouranswers.Ops._
import viewmodels.{AnswerSection, CYAViewModel, Message}
import views.html.checkYourAnswers
import controllers.helpers.CheckYourAnswersControllerHelper._

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersCompanyDetailsController @Inject()(
                                                          appConfig: FrontendAppConfig,
                                                          override val messagesApi: MessagesApi,
                                                          authenticate: AuthAction,
                                                          getData: DataRetrievalAction,
                                                          @NoSuspendedCheck allowAccess: AllowAccessActionProvider,
                                                          requireData: DataRequiredAction,
                                                          implicit val countryOptions: CountryOptions,
                                                          @EstablishersCompany navigator: Navigator,
                                                          userAnswersService: UserAnswersService,
                                                          allowChangeHelper: AllowChangeHelper,
                                                          val controllerComponents: MessagesControllerComponents,
                                                          val view: checkYourAnswers
                                                        )(implicit val executionContext: ExecutionContext) extends FrontendBaseController
  with Retrievals with I18nSupport with Enumerable.Implicits {


  def onPageLoad(mode: Mode, srn: Option[String], index: Index): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        implicit val userAnswers: UserAnswers = request.userAnswers

        val companyDetails = Seq(AnswerSection(
          None,
          HasCompanyCRNId(index).row(routes.HasCompanyCRNController.onPageLoad(checkMode(mode), srn, index).url, mode) ++
            CompanyEnterCRNId(index)
              .row(routes.CompanyEnterCRNController.onPageLoad(checkMode(mode), srn, index).url, mode) ++
            CompanyNoCRNReasonId(index).row(routes.CompanyNoCRNReasonController.onPageLoad(checkMode(mode), srn, index).url, mode) ++
            HasCompanyUTRId(index).row(routes.HasCompanyUTRController.onPageLoad(checkMode(mode), srn, index).url, mode) ++
            CompanyEnterUTRId(index).row(routes.CompanyEnterUTRController.onPageLoad(checkMode(mode), srn, index).url, mode) ++
            CompanyNoUTRReasonId(index).row(routes.CompanyNoUTRReasonController.onPageLoad(checkMode(mode), srn, index).url, mode) ++
            HasCompanyVATId(index).row(routes.HasCompanyVATController.onPageLoad(checkMode(mode), srn, index).url, mode) ++
            CompanyEnterVATId(index).row(routes.CompanyEnterVATController.onPageLoad(checkMode(mode), index, srn).url, mode) ++
            HasCompanyPAYEId(index).row(routes.HasCompanyPAYEController.onPageLoad(checkMode(mode), srn, index).url, mode) ++
            CompanyEnterPAYEId(index).row(routes.CompanyEnterPAYEController.onPageLoad(checkMode(mode), index, srn).url, mode) ++
            IsCompanyDormantId(index).row(routes.IsCompanyDormantController.onPageLoad(checkMode(mode), srn, index).url, mode)
        ))

        val isNew = isNewItem(mode, userAnswers, IsEstablisherNewId(index))

        val titleCompanyDetails: Message = if (isNew) Message("checkYourAnswers.hs.title") else Message("messages__detailsFor", Message("messages__theCompany").resolve)

        val vm = CYAViewModel(
          answerSections = companyDetails,
          href = SchemeTaskListController.onPageLoad(mode, srn),
          schemeName = existingSchemeName,
          returnOverview = false,
          hideEditLinks = request.viewOnly || !userAnswers.get(IsEstablisherNewId(index)).getOrElse(true),
          srn = srn,
          hideSaveAndContinueButton = allowChangeHelper.hideSaveAndContinueButton(request, IsEstablisherNewId(index), mode),
          title = titleCompanyDetails,
          h1 = headingDetails(mode, companyName(CompanyDetailsId(index)), isNew)
        )

        Future.successful(Ok(view(appConfig, vm)))
    }
}
