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
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.TaskList
import utils.hstasklisthelper.HsTaskListHelperRegistration
import views.html.register.establishers.psaTaskListRegistrationEstablishers

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import models.SchemeReferenceNumber

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

  def onPageLoad(mode: Mode, index: Index, srn: Option[SchemeReferenceNumber]): Action[AnyContent] = (authenticate(Some(PSA)) andThen getData(mode, srn, refreshData = false)
    andThen allowAccess(srn)).async {
    implicit request =>
      val schemeNameOpt: Option[String] = request.userAnswers.flatMap(_.get(SchemeNameId))
      (srn, request.userAnswers, schemeNameOpt) match {
        case (None, Some(userAnswers), Some(schemeName)) =>
          Future.successful(Ok(viewRegistration(hsTaskListHelperRegistration.taskListEstablisher(userAnswers, None, srn, index.id),
            schemeName,
            controllers.register.establishers.routes.AddEstablisherController.onPageLoad(mode, srn).url
          )))
        case (Some(_), _, _) =>
          Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad))
        case _ =>
          Future.successful(Redirect(appConfig.managePensionsSchemeOverviewUrl))
      }
  }
}