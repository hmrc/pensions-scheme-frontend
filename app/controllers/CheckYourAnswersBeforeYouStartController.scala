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
import models._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils._
import utils.annotations.NoSuspendedCheck
import utils.checkyouranswers.Ops._
import viewmodels.{AnswerSection, CYAViewModel, Message}
import views.html.checkYourAnswers

import scala.concurrent.{ExecutionContext, Future}

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

  def onPageLoad(mode: Mode, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
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

      def heading(name: String): String = if (mode == NormalMode) Message("checkYourAnswers.hs.title") else
        Message("messages__informationFor", name)

      val vm = CYAViewModel(
        answerSections = Seq(beforeYouStart),
        href = controllers.routes.SchemeTaskListController.onPageLoad(mode, srn),
        schemeName = existingSchemeName,
        returnOverview = !userAnswers.isBeforeYouStartCompleted(mode),
        hideEditLinks = request.viewOnly,
        srn = srn,
        hideSaveAndContinueButton = mode == UpdateMode || mode == CheckUpdateMode,
        title = heading(Message("messages__theScheme").resolve),
        h1 =  heading(existingSchemeName.getOrElse(Message("messages__theScheme").resolve))
      )

      Future.successful(Ok(checkYourAnswers(appConfig, vm)))
  }
}
