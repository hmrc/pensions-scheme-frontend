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

package controllers.register.establishers.company.director

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import controllers.register.establishers.company
import identifiers.register.establishers.company.director.{CheckYourAnswersId, IsDirectorCompleteId}
import javax.inject.Inject
import models.{Index, NormalMode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{CheckYourAnswersFactory, Navigator, SectionComplete}
import utils.annotations.EstablishersCompanyDirector
import viewmodels.AnswerSection
import views.html.check_your_answers

import scala.concurrent.Future

class CheckYourAnswersController @Inject()(appConfig: FrontendAppConfig,
                                           override val messagesApi: MessagesApi,
                                           @EstablishersCompanyDirector navigator: Navigator,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           checkYourAnswersFactory: CheckYourAnswersFactory,
                                           sectionComplete: SectionComplete)
  extends FrontendController with Retrievals with I18nSupport {

  def onPageLoad(companyIndex: Index, directorIndex: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveSchemeName { schemeName =>
        val checkYourAnswersHelper = checkYourAnswersFactory.checkYourAnswersHelper(request.userAnswers)

        val companyDirectorDetails = AnswerSection(
          Some("messages__director__cya__details_heading"),
          checkYourAnswersHelper.directorDetails(companyIndex.id, directorIndex.id) ++
            checkYourAnswersHelper.directorNino(companyIndex.id, directorIndex.id) ++
            checkYourAnswersHelper.directorUniqueTaxReference(companyIndex.id, directorIndex.id)
        )

        val companyDirectorContactDetails = AnswerSection(
          Some("messages__director__cya__contact__details_heading"),
          checkYourAnswersHelper.directorAddress(companyIndex.id, directorIndex.id) ++
            checkYourAnswersHelper.directorAddressYears(companyIndex.id, directorIndex.id) ++
            checkYourAnswersHelper.directorPreviousAddress(companyIndex.id, directorIndex.id) ++
            checkYourAnswersHelper.directorContactDetails(companyIndex.id, directorIndex.id)
        )

        Future.successful(Ok(check_your_answers(
          appConfig,
          Seq(companyDirectorDetails, companyDirectorContactDetails),
          Some(schemeName),
          company.director.routes.CheckYourAnswersController.onSubmit(companyIndex, directorIndex)))
        )
      }
  }

  def onSubmit(companyIndex: Index, directorIndex: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      sectionComplete.setCompleteFlag(IsDirectorCompleteId(companyIndex, directorIndex), request.userAnswers, true).map { _ =>
        Redirect(navigator.nextPage(CheckYourAnswersId(companyIndex, directorIndex), NormalMode, request.userAnswers))
      }
  }
}
