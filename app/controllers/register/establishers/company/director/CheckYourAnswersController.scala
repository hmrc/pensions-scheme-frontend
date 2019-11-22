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

package controllers.register.establishers.company.director

import config.FrontendAppConfig
import controllers.actions._
import controllers.register.establishers.company.routes._
import controllers.{CheckYourAnswersControllerCommon, Retrievals}
import identifiers.register.establishers.company.director._
import javax.inject.Inject
import models.Mode.checkMode
import models._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import utils._
import utils.annotations.NoSuspendedCheck
import utils.checkyouranswers.Ops._
import viewmodels.{AnswerSection, CYAViewModel}
import views.html.checkYourAnswers

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject()(appConfig: FrontendAppConfig,
                                           override val messagesApi: MessagesApi,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           @NoSuspendedCheck allowAccess: AllowAccessActionProvider,
                                           requiredData: DataRequiredAction,
                                           userAnswersService: UserAnswersService,
                                           implicit val countryOptions: CountryOptions,
                                           allowChangeHelper: AllowChangeHelper
                                          )(implicit val ec: ExecutionContext) extends CheckYourAnswersControllerCommon with Retrievals with I18nSupport {

  def onPageLoad(companyIndex: Index, directorIndex: Index, mode: Mode, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requiredData).async {
    implicit request =>

      implicit val userAnswers: UserAnswers = request.userAnswers

      val directorAnswerSection = AnswerSection(
        None,
        Seq(
          DirectorNameId(companyIndex, directorIndex)
            .row(routes.DirectorNameController.onPageLoad(checkMode(mode), companyIndex, directorIndex, srn).url, mode),

          DirectorDOBId(companyIndex, directorIndex)
            .row(routes.DirectorDOBController.onPageLoad(checkMode(mode), companyIndex, directorIndex, srn).url, mode),

          DirectorHasNINOId(companyIndex, directorIndex)
            .row(routes.DirectorHasNINOController.onPageLoad(checkMode(mode), companyIndex, directorIndex, srn).url, mode),

          DirectorEnterNINOId(companyIndex, directorIndex)
            .row(routes.DirectorEnterNINOController.onPageLoad(checkMode(mode), companyIndex, directorIndex, srn).url, mode),

          DirectorNoNINOReasonId(companyIndex, directorIndex)
            .row(routes.DirectorNoNINOReasonController.onPageLoad(checkMode(mode), companyIndex, directorIndex, srn).url, mode),

          DirectorHasUTRId(companyIndex, directorIndex)
            .row(routes.DirectorHasUTRController.onPageLoad(checkMode(mode), companyIndex, directorIndex, srn).url, mode),

          DirectorEnterUTRId(companyIndex, directorIndex)
            .row(routes.DirectorEnterUTRController.onPageLoad(checkMode(mode), companyIndex, directorIndex, srn).url, mode),

          DirectorNoUTRReasonId(companyIndex, directorIndex)
            .row(routes.DirectorNoUTRReasonController.onPageLoad(checkMode(mode), companyIndex, directorIndex, srn).url, mode),

          DirectorAddressId(companyIndex, directorIndex)
            .row(routes.DirectorAddressController.onPageLoad(checkMode(mode), companyIndex, directorIndex, srn).url, mode),

          DirectorAddressYearsId(companyIndex, directorIndex)
            .row(routes.DirectorAddressYearsController.onPageLoad(checkMode(mode), companyIndex, directorIndex, srn).url, mode),

          DirectorPreviousAddressId(companyIndex, directorIndex)
            .row(routes.DirectorPreviousAddressController.onPageLoad(checkMode(mode), companyIndex, directorIndex, srn).url, mode),

          DirectorEmailId(companyIndex, directorIndex)
            .row(routes.DirectorEmailController.onPageLoad(checkMode(mode), companyIndex, directorIndex, srn).url, mode),

          DirectorPhoneNumberId(companyIndex, directorIndex)
            .row(routes.DirectorPhoneNumberController.onPageLoad(checkMode(mode), companyIndex, directorIndex, srn).url, mode)
        ).flatten
      )

      val vm = CYAViewModel(
        answerSections = Seq(directorAnswerSection),
        href = AddCompanyDirectorsController.onPageLoad(mode, srn, companyIndex),
        schemeName = existingSchemeName,
        returnOverview = false,
        hideEditLinks = request.viewOnly,
        srn = srn,
        hideSaveAndContinueButton = allowChangeHelper.hideSaveAndContinueButton(request, IsNewDirectorId(companyIndex, directorIndex), mode),
        title = titleCompanyDirectorDetails(mode, isNewEstablisherCompanyDirector(mode, userAnswers, companyIndex, directorIndex)),
        h1 =  headingEstablisherCompanyDirectorOrPartnerDetails(mode, establisherCompanyDirectorName(companyIndex, directorIndex),
          isNewEstablisherCompanyDirector(mode, userAnswers, companyIndex, directorIndex))
      )

      Future.successful(Ok(checkYourAnswers(appConfig,vm )))
    }
}
