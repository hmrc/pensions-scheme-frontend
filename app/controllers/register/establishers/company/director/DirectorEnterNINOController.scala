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
import controllers.NinoController
import controllers.actions._
import forms.NINOFormProvider
import identifiers.register.establishers.company.director.{DirectorEnterNINOId, DirectorNameId}
import javax.inject.Inject
import models.requests.DataRequest
import models.{Index, Mode}
import navigators.Navigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import utils.annotations.EstablishersCompanyDirector
import viewmodels.{Message, NinoViewModel}
import views.html.nino

import scala.concurrent.ExecutionContext

class DirectorEnterNINOController @Inject()(
                                             val appConfig: FrontendAppConfig,
                                             override val messagesApi: MessagesApi,
                                             val userAnswersService: UserAnswersService,
                                             @EstablishersCompanyDirector val navigator: Navigator,
                                             authenticate: AuthAction,
                                             getData: DataRetrievalAction,
                                             allowAccess: AllowAccessActionProvider,
                                             requireData: DataRequiredAction,
                                             val formProvider: NINOFormProvider,
                                             val controllerComponents: MessagesControllerComponents,
                                             val view: nino
                                           )(implicit val ec: ExecutionContext) extends NinoController with I18nSupport {

  private[controllers] val postCall = controllers.register.establishers.company.director.routes.DirectorEnterNINOController.onSubmit _
  private[controllers] val hint: String = "messages__common__nino_hint"

  private def viewmodel(establisherIndex: Index, directorIndex: Index, mode: Mode, srn: Option[String], name: String)
                       (implicit request: DataRequest[AnyContent]): NinoViewModel =
    NinoViewModel(
      postCall(mode, Index(establisherIndex), Index(directorIndex), srn),
      title = Message("messages__enterNINO", Message("messages__theDirector")),
      heading = Message("messages__enterNINO", name),
      hint = hint,
      srn = srn
    )

  private val directorName: (Index, Index) => Retrieval[String] = (establisherIndex, directorIndex) => Retrieval {
    implicit request =>
      DirectorNameId(establisherIndex, directorIndex).retrieve.right.map(_.fullName)
  }

  def onPageLoad(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        directorName(establisherIndex, directorIndex).retrieve.right.map {
          name =>
            get(DirectorEnterNINOId(establisherIndex, directorIndex), formProvider(name),
              viewmodel(establisherIndex, directorIndex, mode, srn, name))
        }
    }

  def onSubmit(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        directorName(establisherIndex, directorIndex).retrieve.right.map {
          name =>
            post(DirectorEnterNINOId(establisherIndex, directorIndex), mode, formProvider(name),
              viewmodel(establisherIndex, directorIndex, mode, srn, name))
        }
    }

}
