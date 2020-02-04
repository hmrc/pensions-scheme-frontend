/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers.register.establishers.company.director

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import identifiers.register.establishers.company.director.DirectorNameId
import javax.inject.Inject
import models.{Index, NormalMode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.Enumerable
import viewmodels.{AlreadyDeletedViewModel, Message}
import views.html.alreadyDeleted

import scala.concurrent.{ExecutionContext, Future}

class AlreadyDeletedController @Inject()(
                                          appConfig: FrontendAppConfig,
                                          override val messagesApi: MessagesApi,
                                          authenticate: AuthAction,
                                          getData: DataRetrievalAction,
                                          requireData: DataRequiredAction,
                                          val controllerComponents: MessagesControllerComponents,
                                          val view: alreadyDeleted
                                      )(implicit val executionContext: ExecutionContext) extends FrontendBaseController with Retrievals with I18nSupport with Enumerable.Implicits {

  def onPageLoad(establisherIndex: Index, directorIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(srn = srn) andThen requireData).async {
    implicit request =>
      DirectorNameId(establisherIndex, directorIndex).retrieve.right.map { details =>
          Future.successful(Ok(view(appConfig, vm(establisherIndex, details.fullName, srn))))
      }

  }

  private def vm(establisherIndex: Index, directorName: String, srn: Option[String]) = AlreadyDeletedViewModel(
    Message("messages__alreadyDeleted__director_title"),
    directorName,
    controllers.register.establishers.company.routes.AddCompanyDirectorsController.onPageLoad(NormalMode, srn, establisherIndex)
  )

}
