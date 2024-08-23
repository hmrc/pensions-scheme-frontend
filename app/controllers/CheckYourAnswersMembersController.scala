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
import identifiers.{CurrentMembersId, FutureMembersId}
import models.AdministratorOrPractitioner.Practitioner
import models.AuthEntity.PSP
import models._
import models.requests.DataRequest
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.NoSuspendedCheck
import utils.checkyouranswers.Ops._
import utils.{Enumerable, UserAnswers}
import viewmodels.{AnswerSection, CYAViewModel, Message}
import views.html.checkYourAnswers

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersMembersController @Inject()(override val messagesApi: MessagesApi,
                                                  authenticate: AuthAction,
                                                  getData: DataRetrievalAction,
                                                  getPspData: PspDataRetrievalAction,
                                                  @NoSuspendedCheck allowAccess: AllowAccessActionProvider,
                                                  requireData: DataRequiredAction,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  val view: checkYourAnswers
                                                 )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController
  with Enumerable.Implicits with I18nSupport with Retrievals {

  def onPageLoad(mode: Mode, srn: SchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        Future.successful(Ok(view(vm(mode, srn))))
    }

  def pspOnPageLoad(srn: SchemeReferenceNumber): Action[AnyContent] =
    (authenticate(Some(PSP)) andThen getPspData(srn) andThen allowAccess((srn), allowPsa = true, allowPsp = true) andThen requireData).async {
      implicit request =>
        Future.successful(Ok(view(vm(UpdateMode, (srn)))))
    }

  private def vm(mode: Mode, srn: SchemeReferenceNumber)(implicit request: DataRequest[AnyContent]): CYAViewModel = {
    implicit val userAnswers: UserAnswers = request.userAnswers
    val membersSection = AnswerSection(
      None,
      CurrentMembersId.row(routes.CurrentMembersController.onPageLoad(CheckMode, srn).url, mode) ++
        FutureMembersId.row(routes.FutureMembersController.onPageLoad(CheckMode, srn).url, mode)
    )

    val heading = (name: String) => if (mode == NormalMode) Message("checkYourAnswers.hs.title") else
      Message("messages__membershipDetailsFor", name)

    val returnToTaskListCall:Option[Call] = (request.administratorOrPractitioner, srn) match {
      case (Practitioner, (srn)) => Option(controllers.routes.PspSchemeTaskListController.onPageLoad(srn))
      case _ => None
    }

    CYAViewModel(
      answerSections = Seq(membersSection),
      href = controllers.routes.PsaSchemeTaskListController.onPageLoad(mode, srn),
      schemeName = existingSchemeName,
      returnOverview = false,
      hideEditLinks = request.viewOnly,
      srn = srn,
      hideSaveAndContinueButton = mode == UpdateMode || mode == CheckUpdateMode,
      title = heading(Message("messages__theScheme")),
      h1 = heading(existingSchemeName.getOrElse(Message("messages__theScheme"))),
      anotherReturn = returnToTaskListCall
    )
  }

}
