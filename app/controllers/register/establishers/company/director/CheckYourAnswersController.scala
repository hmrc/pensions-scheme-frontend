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
import identifiers.register.establishers.company.director._
import javax.inject.Inject
import models.{CheckMode, Index, Mode, NormalMode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.EstablishersCompanyDirector
import utils.checkyouranswers.Ops._
import utils.{CountryOptions, Navigator, SectionComplete}
import viewmodels.AnswerSection
import views.html.check_your_answers

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject()(appConfig: FrontendAppConfig,
                                           override val messagesApi: MessagesApi,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           requiredData: DataRequiredAction,
                                           sectionComplete: SectionComplete,
                                           @EstablishersCompanyDirector navigator: Navigator,
                                           implicit val countryOptions: CountryOptions
                                          )(implicit val ec: ExecutionContext) extends FrontendController with Retrievals with I18nSupport {

  def onPageLoad(companyIndex: Index, directorIndex: Index, mode: Mode, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requiredData).async {
      implicit request =>
        val companyDirectorDetails = AnswerSection(
          Some("messages__director__cya__details_heading"),
          Seq(
            DirectorDetailsId(companyIndex, directorIndex).
              row(routes.DirectorDetailsController.onPageLoad(CheckMode, companyIndex, directorIndex, srn).url, mode),
            DirectorNinoId(companyIndex, directorIndex).
              row(routes.DirectorNinoController.onPageLoad(CheckMode, companyIndex, directorIndex, srn).url, mode),
            DirectorUniqueTaxReferenceId(companyIndex, directorIndex).
              row(routes.DirectorUniqueTaxReferenceController.onPageLoad(CheckMode, companyIndex, directorIndex, srn).url, mode)
          ).flatten
        )

        val companyDirectorContactDetails = AnswerSection(
          Some("messages__director__cya__contact__details_heading"),
          Seq(
            DirectorAddressId(companyIndex, directorIndex).
              row(routes.DirectorAddressController.onPageLoad(CheckMode, companyIndex, directorIndex, srn).url),
            DirectorAddressYearsId(companyIndex, directorIndex).
              row(routes.DirectorAddressYearsController.onPageLoad(CheckMode, companyIndex, directorIndex, srn).url, mode),
            DirectorPreviousAddressId(companyIndex, directorIndex).
              row(routes.DirectorPreviousAddressController.onPageLoad(CheckMode, companyIndex, directorIndex, srn).url, mode),
            DirectorContactDetailsId(companyIndex, directorIndex).
              row(routes.DirectorContactDetailsController.onPageLoad(CheckMode, companyIndex, directorIndex, srn).url)
          ).flatten
        )

        Future.successful(Ok(check_your_answers(
          appConfig,
          Seq(companyDirectorDetails, companyDirectorContactDetails),
          routes.CheckYourAnswersController.onSubmit(companyIndex, directorIndex, mode, srn),
          existingSchemeName,
          mode = mode
        )))

    }

  def onSubmit(companyIndex: Index, directorIndex: Index, mode: Mode, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requiredData).async {
      implicit request =>
        sectionComplete.setCompleteFlag(request.externalId, IsDirectorCompleteId(companyIndex, directorIndex), request.userAnswers, true).map { _ =>
          Redirect(navigator.nextPage(CheckYourAnswersId(companyIndex, directorIndex), NormalMode, request.userAnswers))
        }
    }
}
