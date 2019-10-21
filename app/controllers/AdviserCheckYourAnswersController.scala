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

import config.FrontendAppConfig
import connectors._
import controllers.actions._
import identifiers.{IsWorkingKnowledgeCompleteId, _}
import javax.inject.Inject
import models.{CheckMode, NormalMode}
import navigators.Navigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.WorkingKnowledge
import utils.checkyouranswers.Ops._
import utils.{CountryOptions, SectionComplete}
import viewmodels.AnswerSection
import views.html.check_your_answers_old

import scala.concurrent.ExecutionContext

class AdviserCheckYourAnswersController @Inject()(appConfig: FrontendAppConfig,
                                                  override val messagesApi: MessagesApi,
                                                  dataCacheConnector: UserAnswersCacheConnector,
                                                  authenticate: AuthAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  @WorkingKnowledge navigator: Navigator,
                                                  implicit val countryOptions: CountryOptions,
                                                  sectionComplete: SectionComplete
                                          )(implicit val ec: ExecutionContext) extends FrontendController with I18nSupport with Retrievals {

  def onPageLoad: Action[AnyContent] = (authenticate andThen getData() andThen requireData) {
    implicit request =>
      implicit val userAnswers = request.userAnswers

      val seqAnswerSection = {
          val adviserNameRow = AdviserNameId.row(routes.AdviserNameController.onPageLoad(CheckMode).url)
          val adviserEmailRow = AdviserEmailId.row(routes.AdviserEmailAddressController.onPageLoad(CheckMode).url)
          val adviserPhoneRow = AdviserPhoneId.row(routes.AdviserPhoneController.onPageLoad(CheckMode).url)
          val adviserAddressRow = AdviserAddressId.row(routes.AdviserAddressController.onPageLoad(CheckMode).url)
          Seq(AnswerSection(None, adviserNameRow ++ adviserEmailRow ++ adviserPhoneRow ++ adviserAddressRow))
      }
      Ok(
        check_your_answers_old(
          appConfig,
          seqAnswerSection,
          controllers.routes.AdviserCheckYourAnswersController.onSubmit(),
          existingSchemeName,
          hideEditLinks = request.viewOnly,
          hideSaveAndContinueButton = request.viewOnly
        )
      )
  }

  def onSubmit: Action[AnyContent] = (authenticate andThen getData() andThen requireData).async {
    implicit request =>
      sectionComplete.setCompleteFlag(request.externalId, IsWorkingKnowledgeCompleteId, request.userAnswers, value = true).map { _ =>
        Redirect(navigator.nextPage(AdviserCheckYourAnswersId, NormalMode, request.userAnswers))
      }
  }
}
