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
import models.{CheckMode, Mode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.checkyouranswers.Ops._
import utils.{Enumerable, SectionComplete}
import viewmodels.AnswerSection
import views.html.check_your_answers

import scala.concurrent.ExecutionContext

class CheckYourAnswersMembersController @Inject()(appConfig: FrontendAppConfig,
                                                  override val messagesApi: MessagesApi,
                                                  authenticate: AuthAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  sectionComplete: SectionComplete,
                                                  allowAccess:AllowAccessForNonSuspendedUsersActionProvider
                                                 )(implicit val ec: ExecutionContext) extends FrontendController
  with Enumerable.Implicits with I18nSupport with Retrievals {

  def onPageLoad(mode: Mode, srn: Option[String]): Action[AnyContent] = (authenticate andThen allowAccess(srn) andThen getData(mode, srn) andThen requireData) {
    implicit request =>
      implicit val userAnswers = request.userAnswers
      val membersSection = AnswerSection(
        None,
        CurrentMembersId.row(routes.CurrentMembersController.onPageLoad(CheckMode).url, mode) ++
          FutureMembersId.row(routes.FutureMembersController.onPageLoad(CheckMode).url, mode)
      )
      Ok(check_your_answers(
        appConfig,
        Seq(membersSection),
        routes.CheckYourAnswersMembersController.onSubmit(mode, srn),
        existingSchemeName,
        mode = mode,
        viewOnly = request.viewOnly
      ))
  }

  def onSubmit(mode: Mode, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData() andThen requireData).async {
    implicit request =>
      sectionComplete.setCompleteFlag(request.externalId, IsAboutMembersCompleteId, request.userAnswers, value = true) map { _ =>
        Redirect(controllers.routes.SchemeTaskListController.onPageLoad())
      }
  }

}
