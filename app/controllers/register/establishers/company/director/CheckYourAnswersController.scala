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

import config.{FeatureSwitchManagementService, FrontendAppConfig}
import controllers.Retrievals
import controllers.actions._
import controllers.register.establishers.company.routes._
import identifiers.register.establishers.company.director._
import javax.inject.Inject
import models.Mode.checkMode
import models._
import navigators.Navigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils._
import utils.annotations.{EstablishersCompanyDirector, NoSuspendedCheck}
import utils.checkyouranswers.Ops._
import viewmodels.AnswerSection
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
                                           allowChangeHelper: AllowChangeHelper,
                                           fs: FeatureSwitchManagementService
                                          )(implicit val ec: ExecutionContext) extends FrontendController with Retrievals with I18nSupport {

  def onPageLoad(companyIndex: Index, directorIndex: Index, mode: Mode, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requiredData).async {
    implicit request =>

      implicit val userAnswers: UserAnswers = request.userAnswers
      implicit val featureSwitchManagementService: FeatureSwitchManagementService = fs

      val directorAnswerSection = AnswerSection(
        None,
        Seq(
          DirectorNameId(companyIndex, directorIndex)
            .row(routes.DirectorNameController.onPageLoad(checkMode(mode), companyIndex, directorIndex, srn).url, mode),

          DirectorDOBId(companyIndex, directorIndex)
            .row(routes.DirectorDOBController.onPageLoad(checkMode(mode), companyIndex, directorIndex, srn).url, mode),

          DirectorHasNINOId(companyIndex, directorIndex)
            .row(routes.DirectorHasNINOController.onPageLoad(checkMode(mode), companyIndex, directorIndex, srn).url, mode),

          DirectorNewNinoId(companyIndex, directorIndex)
            .row(routes.DirectorNinoNewController.onPageLoad(checkMode(mode), companyIndex, directorIndex, srn).url, mode),

          DirectorNoNINOReasonId(companyIndex, directorIndex)
            .row(routes.DirectorNoNINOReasonController.onPageLoad(checkMode(mode), companyIndex, directorIndex, srn).url, mode),

          DirectorHasUTRId(companyIndex, directorIndex)
            .row(routes.DirectorHasUTRController.onPageLoad(checkMode(mode), companyIndex, directorIndex, srn).url, mode),

          DirectorUTRId(companyIndex, directorIndex)
            .row(routes.DirectorUTRController.onPageLoad(checkMode(mode), companyIndex, directorIndex, srn).url, mode),

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


      val directorNinoDetails = (mode, userAnswers.get(IsNewDirectorId(companyIndex, directorIndex))) match {
        case (_, Some(true)) | (NormalMode|CheckMode, _) =>
          DirectorNinoId(companyIndex, directorIndex)
            .row(routes.DirectorNinoController.onPageLoad(checkMode(mode), companyIndex, directorIndex, srn).url, mode)

        case _ =>
          DirectorNewNinoId(companyIndex, directorIndex)
            .row(routes.DirectorNinoNewController.onPageLoad(checkMode(mode), companyIndex, directorIndex, srn).url, mode)
      }

      val companyDirectorDetails = AnswerSection(
        Some("messages__director__cya__details_heading"),
        Seq(
          DirectorDetailsId(companyIndex, directorIndex)
            .row(routes.DirectorDetailsController.onPageLoad(checkMode(mode), companyIndex, directorIndex, srn).url, mode),
          directorNinoDetails,
          DirectorUniqueTaxReferenceId(companyIndex, directorIndex)
            .row(routes.DirectorUniqueTaxReferenceController.onPageLoad(checkMode(mode), companyIndex, directorIndex, srn).url, mode)
        ).flatten
      )

      val companyDirectorContactDetails = AnswerSection(
        Some("messages__director__cya__contact__details_heading"),
        Seq(
          DirectorAddressId(companyIndex, directorIndex).
            row(routes.DirectorAddressController.onPageLoad(checkMode(mode), companyIndex, directorIndex, srn).url),
          DirectorAddressYearsId(companyIndex, directorIndex).
            row(routes.DirectorAddressYearsController.onPageLoad(checkMode(mode), companyIndex, directorIndex, srn).url, mode),
          DirectorPreviousAddressId(companyIndex, directorIndex).
            row(routes.DirectorPreviousAddressController.onPageLoad(checkMode(mode), companyIndex, directorIndex, srn).url, mode),
          DirectorContactDetailsId(companyIndex, directorIndex).
            row(routes.DirectorContactDetailsController.onPageLoad(checkMode(mode), companyIndex, directorIndex, srn).url)
        ).flatten
      )

      val answerSections =
        Seq(directorAnswerSection)

      Future.successful(Ok(checkYourAnswers(
        appConfig,
        answerSections,
        AddCompanyDirectorsController.onPageLoad(mode, srn, companyIndex),
        existingSchemeName,
        mode = mode,
        hideEditLinks = request.viewOnly,
        hideSaveAndContinueButton = allowChangeHelper.hideSaveAndContinueButton(request, IsNewDirectorId(companyIndex, directorIndex), mode),
        srn = srn
      )))
    }
}
