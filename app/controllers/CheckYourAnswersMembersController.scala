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
import controllers.actions._
import identifiers.{CurrentMembersId, FutureMembersId, IsAboutMembersCompleteId}
import javax.inject.Inject
import models.requests.DataRequest
import models.{CheckMode, CheckUpdateMode, Mode, UpdateMode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.{NoSuspendedCheck, TaskList}
import utils.checkyouranswers.Ops._
import utils.{Enumerable, UserAnswers}
import viewmodels.AnswerSection
import views.html.check_your_answers_old

import scala.concurrent.ExecutionContext

class CheckYourAnswersMembersController @Inject()(appConfig: FrontendAppConfig,
                                                  override val messagesApi: MessagesApi,
                                                  authenticate: AuthAction,
                                                  getData: DataRetrievalAction,
                                                  @NoSuspendedCheck allowAccess: AllowAccessActionProvider,
                                                  requireData: DataRequiredAction,
                                                  userAnswersService: UserAnswersService
                                                 )(implicit val ec: ExecutionContext) extends FrontendController
  with Enumerable.Implicits with I18nSupport with Retrievals {

  def onPageLoad(mode: Mode, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData) {
    implicit request =>
      implicit val userAnswers: UserAnswers = request.userAnswers
      val membersSection = AnswerSection(
        None,
        CurrentMembersId.row(routes.CurrentMembersController.onPageLoad(CheckMode).url, mode) ++
          FutureMembersId.row(routes.FutureMembersController.onPageLoad(CheckMode).url, mode)
      )
      Ok(check_your_answers_old(
        appConfig,
        Seq(membersSection),
        routes.CheckYourAnswersMembersController.onSubmit(mode, srn),
        existingSchemeName,
        mode = mode,
        hideEditLinks = request.viewOnly,
        hideSaveAndContinueButton = mode == UpdateMode || mode == CheckUpdateMode,
        srn = srn
      ))
  }

  def onSubmit(mode: Mode, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData() andThen requireData).async {
    implicit request =>
      userAnswersService.setCompleteFlag(mode, srn, IsAboutMembersCompleteId, request.userAnswers, value = true) map { _ =>
        Redirect(controllers.routes.SchemeTaskListController.onPageLoad(mode, srn))
      }
  }

}
