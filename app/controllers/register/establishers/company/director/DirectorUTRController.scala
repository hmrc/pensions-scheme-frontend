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

package controllers.register.establishers.company.director

import config.FrontendAppConfig
import controllers.UTRController
import controllers.actions._
import forms.UTRFormProvider
import identifiers.register.establishers.company.director.{DirectorNameId, DirectorEnterUTRId}
import javax.inject.Inject
import models.{Index, Mode, ReferenceValue}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import utils.annotations.EstablishersCompanyDirector
import viewmodels.{Message, UTRViewModel}

import scala.concurrent.ExecutionContext

class DirectorUTRController @Inject()(
                                       override val appConfig: FrontendAppConfig,
                                       override val messagesApi: MessagesApi,
                                       override val userAnswersService: UserAnswersService,
                                       @EstablishersCompanyDirector override val navigator: Navigator,
                                       authenticate: AuthAction,
                                       getData: DataRetrievalAction,
                                       allowAccess: AllowAccessActionProvider,
                                       requireData: DataRequiredAction,
                                       formProvider: UTRFormProvider
                                     )(implicit val ec: ExecutionContext) extends UTRController {

  private def form: Form[ReferenceValue] = formProvider()

  private def viewModel(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: Option[String], directorName: String): UTRViewModel = {
    UTRViewModel(
      postCall = controllers.register.establishers.company.director.routes.DirectorUTRController.onSubmit(mode, establisherIndex, directorIndex, srn),
      title = Message("messages__directorUtr__title"),
      heading = Message("messages__directorUtr__heading", directorName),
      hint = Message("messages_utr__hint"),
      srn = srn
    )
  }

  def onPageLoad(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        DirectorNameId(establisherIndex, directorIndex).retrieve.right.map { details =>
          val directorName = details.fullName
          get(DirectorEnterUTRId(establisherIndex, directorIndex), viewModel(mode, establisherIndex, directorIndex, srn, directorName), form)
        }
    }

  def onSubmit(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        DirectorNameId(establisherIndex, directorIndex).retrieve.right.map { details =>
          val directorName = details.fullName
          post(DirectorEnterUTRId(establisherIndex, directorIndex), mode, viewModel(mode, establisherIndex, directorIndex, srn, directorName), form)
        }
    }


}
