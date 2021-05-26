/*
 * Copyright 2021 HM Revenue & Customs
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
import identifiers.SchemeNameId
import identifiers.racdac.IsRacDacId
import models.AuthEntity.PSP
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.hstasklisthelper.HsTaskListHelperPsp
import views.html.pspTaskList

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class PspSchemeTaskListController @Inject()(
                                             override val messagesApi: MessagesApi,
                                             authenticate: AuthAction,
                                             getData: PspDataRetrievalAction,
                                             val controllerComponents: MessagesControllerComponents,
                                             val view: pspTaskList,
                                             hsTaskListHelperPsp: HsTaskListHelperPsp
                                        )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController with I18nSupport with Retrievals {

  private def sessionExpired:Result = Redirect(controllers.routes.SessionExpiredController.onPageLoad())

  def onPageLoad(srn: String): Action[AnyContent] = (authenticate(Some(PSP)) andThen getData(srn)) {
    implicit request =>

      request.userAnswers match {
        case Some(ua) =>
          (ua.get(IsRacDacId), ua.get(SchemeNameId)) match {
            case (Some(true), Some(_)) => Redirect(controllers.racdac.routes.CheckYourAnswersController.pspOnPageLoad(srn))
            case (_, Some(schemeName)) => Ok(view(hsTaskListHelperPsp.taskList(ua, srn), schemeName))
            case _ => sessionExpired
          }
        case _ => sessionExpired
      }
  }
}
