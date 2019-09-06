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
import identifiers.{DeclarationDutiesId, _}
import javax.inject.Inject
import models.{CheckMode, CheckUpdateMode, Mode, UpdateMode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils._
import utils.annotations.NoSuspendedCheck
import utils.checkyouranswers.Ops._
import viewmodels.AnswerSection
import views.html.check_your_answers_old

import scala.concurrent.ExecutionContext

class CheckYourAnswersBeforeYouStartController @Inject()(appConfig: FrontendAppConfig,
                                                         override val messagesApi: MessagesApi,
                                                         authenticate: AuthAction,
                                                         getData: DataRetrievalAction,
                                                         @NoSuspendedCheck allowAccess: AllowAccessActionProvider,
                                                         requireData: DataRequiredAction,
                                                         implicit val countryOptions: CountryOptions,
                                                         userAnswersService: UserAnswersService
                                                        )(implicit val ec: ExecutionContext) extends FrontendController
  with Enumerable.Implicits with I18nSupport with Retrievals {

  def onPageLoad(mode: Mode, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData) {
    implicit request =>

      implicit val userAnswers: UserAnswers = request.userAnswers

      val beforeYouStart = AnswerSection(
        None,
        SchemeNameId.row(routes.SchemeNameController.onPageLoad(CheckMode).url, mode) ++
        SchemeTypeId.row(routes.SchemeTypeController.onPageLoad(CheckMode).url, mode) ++
        HaveAnyTrusteesId.row(routes.HaveAnyTrusteesController.onPageLoad(CheckMode).url, mode) ++
        EstablishedCountryId.row(routes.EstablishedCountryController.onPageLoad(CheckMode).url, mode) ++
        DeclarationDutiesId.row(routes.WorkingKnowledgeController.onPageLoad(CheckMode).url, mode)
      )

      Ok(check_your_answers_old(
        appConfig,
        Seq(beforeYouStart),
        routes.CheckYourAnswersBeforeYouStartController.onSubmit(mode, srn),
        existingSchemeName,
        returnOverview = !userAnswers.get(IsBeforeYouStartCompleteId).getOrElse(false),
        mode,
        hideEditLinks = request.viewOnly, srn,
        hideSaveAndContinueButton = mode == UpdateMode || mode == CheckUpdateMode
      ))
  }

  def onSubmit(mode: Mode, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData() andThen requireData).async {
    implicit request =>
      userAnswersService.setCompleteFlag(mode, srn, IsBeforeYouStartCompleteId, request.userAnswers, value = true) map { _ =>
        Redirect(controllers.routes.SchemeTaskListController.onPageLoad(mode, srn))
      }
  }
}
