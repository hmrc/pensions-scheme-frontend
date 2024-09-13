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

package controllers.register.establishers

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import identifiers.SchemeNameId
import models.AuthEntity.PSA
import models._
import models.requests.OptionalDataRequest
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.UserAnswers
import utils.annotations.TaskList
import utils.hstasklisthelper.HsTaskListHelperRegistration
import viewmodels.SchemeDetailsTaskListEstablishers
import views.html.register.establishers.psaTaskListRegistrationEstablishers

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PsaSchemeTaskListRegistrationEstablisherController @Inject()(appConfig: FrontendAppConfig,
                                                                   override val messagesApi: MessagesApi,
                                                                   authenticate: AuthAction,
                                                                   getData: DataRetrievalAction,
                                                                   @TaskList allowAccess: AllowAccessActionProvider,
                                                                   val controllerComponents: MessagesControllerComponents,
                                                                   val viewRegistration: psaTaskListRegistrationEstablishers,
                                                                   hsTaskListHelperRegistration: HsTaskListHelperRegistration
                                                                  )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController with I18nSupport with Retrievals {

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate(Some(PSA)) andThen getData(mode, srn, refreshData = false) andThen allowAccess(srn)).async {
      implicit request =>
        val schemeNameOpt = request.userAnswers.flatMap(_.get(SchemeNameId))

        (srn, request.userAnswers, schemeNameOpt) match {
          case (None, Some(userAnswers), Some(schemeName)) => handleValidRequest(userAnswers, schemeName, mode, srn, index)
          case (Some(_), _, _) => Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad))
          case _ => Future.successful(Redirect(appConfig.managePensionsSchemeOverviewUrl))
        }
    }

  private def handleValidRequest(userAnswers: UserAnswers, schemeName: String, mode: Mode, srn: Option[String], index: Int)
                                (implicit request: OptionalDataRequest[AnyContent]): Future[Result] = {
    try {
      val taskList = hsTaskListHelperRegistration.taskListEstablisher(userAnswers, None, srn, index)
      renderOkResponse(taskList, schemeName, mode, srn)

    } catch {
      case e: RuntimeException if e.getMessage == "INVALID-ESTABLISHER" =>
        Future.successful(Redirect(controllers.register.routes.MemberNotFoundController.onEstablishersPageLoad()))
    }
  }

  private def renderOkResponse(taskList: SchemeDetailsTaskListEstablishers, schemeName: String, mode: Mode, srn: Option[String])
                              (implicit request: OptionalDataRequest[AnyContent]): Future[Result] = {
    Future.successful(
      Ok(
        viewRegistration(
          taskList,
          schemeName,
          controllers.register.establishers.routes.AddEstablisherController.onPageLoad(mode, srn).url
        )
      )
    )
  }
}
