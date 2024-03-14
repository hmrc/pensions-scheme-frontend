/*
 * Copyright 2024 HM Revenue & Customs
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

import controllers.actions._
import identifiers.{DeclarationDutiesId, _}
import models.AdministratorOrPractitioner.Practitioner

import javax.inject.Inject
import models.AuthEntity.PSP
import models._
import models.requests.DataRequest
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils._
import utils.annotations.NoSuspendedCheck
import utils.checkyouranswers.Ops._
import viewmodels.{AnswerSection, CYAViewModel, Message}
import views.html.checkYourAnswers

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersBeforeYouStartController @Inject()(override val messagesApi: MessagesApi,
                                                         authenticate: AuthAction,
                                                         getData: DataRetrievalAction,
                                                         getPspData: PspDataRetrievalAction,
                                                         @NoSuspendedCheck allowAccess: AllowAccessActionProvider,
                                                         requireData: DataRequiredAction,
                                                         implicit val countryOptions: CountryOptions,
                                                         val controllerComponents: MessagesControllerComponents,
                                                         val view: checkYourAnswers
                                                        )(implicit val ec: ExecutionContext) extends
  FrontendBaseController
  with Enumerable.Implicits with I18nSupport with Retrievals {

  def onPageLoad(mode: Mode, srn: Option[String]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>

        Future.successful(Ok(view(vm(mode, srn))))
    }

    def pspOnPageLoad(srn: String): Action[AnyContent] =
      (authenticate(Some(PSP)) andThen getPspData(srn) andThen allowAccess(Some(srn), allowPsa = true, allowPsp = true) andThen requireData).async {
        implicit request =>
          Future.successful(Ok(view(vm(UpdateMode, Some(srn)))))
      }

  private def vm(mode: Mode, srn: Option[String])(implicit request: DataRequest[AnyContent]): CYAViewModel = {
    implicit val userAnswers: UserAnswers = request.userAnswers

    val beforeYouStart = AnswerSection(
      None,
      SchemeNameId.row(routes.SchemeNameController.onPageLoad(CheckMode).url, mode) ++
        SchemeTypeId.row(routes.SchemeTypeController.onPageLoad(CheckMode).url, mode) ++
        HaveAnyTrusteesId.row(routes.HaveAnyTrusteesController.onPageLoad(CheckMode).url, mode) ++
        EstablishedCountryId.row(routes.EstablishedCountryController.onPageLoad(CheckMode).url, mode) ++
        DeclarationDutiesId.row(routes.WorkingKnowledgeController.onPageLoad(CheckMode).url, mode)
    )

    val heading = (titleOrHeading: Message) =>
      if (mode == NormalMode) Message("checkYourAnswers.hs.title") else titleOrHeading

    val returnToTaskListCall:Option[Call] = (request.administratorOrPractitioner, srn) match {
      case (Practitioner, Some(srn)) => Option(controllers.routes.PspSchemeTaskListController.onPageLoad(srn))
      case _ => None
    }

    CYAViewModel(
      answerSections = Seq(beforeYouStart),
      href = controllers.routes.PsaSchemeTaskListController.onPageLoad(mode, srn),
      schemeName = existingSchemeName,
      returnOverview = !userAnswers.isBeforeYouStartCompleted(mode),
      hideEditLinks = request.viewOnly,
      srn = srn,
      hideSaveAndContinueButton = mode == UpdateMode || mode == CheckUpdateMode,
      title = heading(Message("messages__informationFor_title")),
      h1 = heading(Message("messages__informationFor_heading",
        existingSchemeName.getOrElse(Message("messages__theScheme")))),
      anotherReturn = returnToTaskListCall
    )
  }
}
