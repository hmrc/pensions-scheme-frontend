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

package controllers.register.establishers.company.director

import config.FrontendAppConfig
import controllers.ReasonController
import controllers.actions._
import forms.ReasonFormProvider
import identifiers.register.establishers.company.director.{DirectorNameId, DirectorNoUTRReasonId}
import javax.inject.Inject
import models.requests.DataRequest
import models.{Index, Mode}
import navigators.Navigator
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import utils.annotations.EstablishersCompanyDirector
import viewmodels.{Message, ReasonViewModel}
import views.html.reason

import scala.concurrent.ExecutionContext

class DirectorNoUTRReasonController @Inject()(override val appConfig: FrontendAppConfig,
                                              override val messagesApi: MessagesApi,
                                              override val userAnswersService: UserAnswersService,
                                              @EstablishersCompanyDirector override val navigator: Navigator,
                                              authenticate: AuthAction,
                                              getData: DataRetrievalAction,
                                              allowAccess: AllowAccessActionProvider,
                                              requireData: DataRequiredAction,
                                              formProvider: ReasonFormProvider,
                                              val controllerComponents: MessagesControllerComponents,
                                              val view: reason
                                             )(implicit val ec: ExecutionContext) extends ReasonController {

  def onPageLoad(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        DirectorNameId(establisherIndex, directorIndex).retrieve.right.map { details =>
          val directorName = details.fullName
          get(DirectorNoUTRReasonId(establisherIndex, directorIndex), viewModel(mode, establisherIndex,
            directorIndex, srn, directorName), form(directorName))
        }
    }

  def onSubmit(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        DirectorNameId(establisherIndex, directorIndex).retrieve.right.map { details =>
          val directorName = details.fullName
          post(DirectorNoUTRReasonId(establisherIndex, directorIndex), mode, viewModel(mode, establisherIndex,
            directorIndex, srn, directorName), form(directorName))
        }
    }


  private def form(directorName: String)(implicit request: DataRequest[AnyContent]) =
    formProvider("messages__reason__error_utrRequired", directorName)

  private def viewModel(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: Option[String],
                        directorName: String): ReasonViewModel = {
    ReasonViewModel(
      postCall = routes.DirectorNoUTRReasonController.onSubmit(mode, establisherIndex, directorIndex, srn),
      title = Message("messages__whyNoUTR", Message("messages__theDirector")),
      heading = Message("messages__whyNoUTR", directorName),
      srn = srn
    )
  }

}
