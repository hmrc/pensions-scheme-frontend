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
import controllers.Retrievals
import controllers.actions._
import identifiers.AnyMoreChangesId
import identifiers.register.establishers.{IsEstablisherCompleteId, IsEstablisherNewId}
import identifiers.register.establishers.company.{CompanyReviewId, IsCompanyCompleteId, director}
import identifiers.register.establishers.company.director._
import javax.inject.Inject
import models.Mode.checkMode
import models.{CheckMode, Index, Mode, NormalMode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.EstablishersCompanyDirector
import utils.checkyouranswers.Ops._
import utils._
import viewmodels.AnswerSection
import views.html.check_your_answers

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject()(appConfig: FrontendAppConfig,
                                           override val messagesApi: MessagesApi,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           allowAccess: AllowAccessActionProvider,
                                           requiredData: DataRequiredAction,
                                           userAnswersService: UserAnswersService,
                                           @EstablishersCompanyDirector navigator: Navigator,
                                           implicit val countryOptions: CountryOptions,
                                           allowChangeHelper: AllowChangeHelper
                                          )(implicit val ec: ExecutionContext) extends FrontendController with Retrievals with I18nSupport {

  def onPageLoad(companyIndex: Index, directorIndex: Index, mode: Mode, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requiredData).async {
    implicit request =>

      implicit val userAnswers = request.userAnswers

      val companyDirectorDetails = AnswerSection(
        Some("messages__director__cya__details_heading"),
        Seq(
          DirectorDetailsId(companyIndex, directorIndex).
            row(routes.DirectorDetailsController.onPageLoad(checkMode(mode), companyIndex, directorIndex, srn).url, mode),
          DirectorNinoId(companyIndex, directorIndex).
            row(routes.DirectorNinoController.onPageLoad(checkMode(mode), companyIndex, directorIndex, srn).url, mode),
          DirectorUniqueTaxReferenceId(companyIndex, directorIndex).
            row(routes.DirectorUniqueTaxReferenceController.onPageLoad(checkMode(mode), companyIndex, directorIndex, srn).url, mode)
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

      Future.successful(Ok(check_your_answers(
        appConfig,
        Seq(companyDirectorDetails, companyDirectorContactDetails),
        routes.CheckYourAnswersController.onSubmit(companyIndex, directorIndex, mode, srn),
        existingSchemeName,
        mode = mode,
        hideEditLinks = request.viewOnly,
        hideSaveAndContinueButton = allowChangeHelper.hideSaveAndContinueButton(request, IsNewDirectorId(companyIndex, directorIndex), mode),
        srn = srn
      )))

    }

  def onSubmit(companyIndex: Index, directorIndex: Index, mode: Mode, srn: Option[String]): Action[AnyContent] = (
    authenticate andThen getData(mode, srn) andThen requiredData).async {
    implicit request =>
      mode match{
        case NormalMode | CheckMode =>
          userAnswersService.setCompleteFlag(mode, srn, IsDirectorCompleteId(companyIndex, directorIndex), request.userAnswers, true).map { _ =>
            Redirect(navigator.nextPage(CheckYourAnswersId(companyIndex, directorIndex), mode, request.userAnswers, srn))
          }
        case _ =>
          val isEstablisherNew = request.userAnswers.get(IsEstablisherNewId(companyIndex)).getOrElse(false)
          if (isEstablisherNew) {
            userAnswersService.setCompleteFlag(mode, srn, IsDirectorCompleteId(companyIndex, directorIndex), request.userAnswers, value = true).map { result =>
                Redirect(navigator.nextPage(CheckYourAnswersId(companyIndex, directorIndex), mode, request.userAnswers, srn))
            }
          }
          else {
            request.userAnswers.upsert(IsEstablisherCompleteId(companyIndex))(true) { answers =>
              answers.upsert(IsDirectorCompleteId(companyIndex, directorIndex))(true) { updatedAnswers =>
                userAnswersService.upsert(mode, srn, updatedAnswers.json).map { json =>
                  Redirect(navigator.nextPage(CheckYourAnswersId(companyIndex, directorIndex), mode, UserAnswers(json), srn))
                }
              }
            }
          }
      }
  }
}
