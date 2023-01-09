/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.register.establishers.individual

import config.FrontendAppConfig
import controllers.NinoController
import controllers.actions._
import forms.NINOFormProvider
import identifiers.register.establishers.individual.{EstablisherEnterNINOId, EstablisherNameId}
import javax.inject.Inject
import models.person.PersonName
import models.requests.DataRequest
import models.{Index, Mode}
import navigators.Navigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import viewmodels.{Message, NinoViewModel}
import views.html.nino

import scala.concurrent.ExecutionContext

class EstablisherEnterNINOController @Inject()(val appConfig: FrontendAppConfig,
                                               override val messagesApi: MessagesApi,
                                               val userAnswersService: UserAnswersService,
                                               val navigator: Navigator,
                                               authenticate: AuthAction,
                                               getData: DataRetrievalAction,
                                               allowAccess: AllowAccessActionProvider,
                                               requireData: DataRequiredAction,
                                               val formProvider: NINOFormProvider,
                                               val view: nino,
                                               val controllerComponents: MessagesControllerComponents
                                              )
                                              (implicit val ec: ExecutionContext) extends NinoController with
  I18nSupport {

  private[controllers] val postCall = controllers.register.establishers.individual.routes
    .EstablisherEnterNINOController.onSubmit _

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        EstablisherNameId(index).retrieve.map {
          details =>
            get(EstablisherEnterNINOId(index), formProvider(details.fullName), viewmodel(details, index, mode, srn))
        }
    }

  private def viewmodel(personDetails: PersonName, index: Index, mode: Mode, srn: Option[String])
                       (implicit request: DataRequest[AnyContent]): NinoViewModel =
    NinoViewModel(
      postCall(mode, Index(index), srn),
      title = Message("messages__enterNINO", Message("messages__theIndividual")),
      heading = Message("messages__enterNINO", personDetails.fullName),
      hint = Message("messages__common__nino_hint"),
      srn = srn
    )

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] = (authenticate() andThen getData
  (mode, srn) andThen requireData).async {
    implicit request =>
      EstablisherNameId(index).retrieve.map {
        details =>
          post(EstablisherEnterNINOId(index), mode, formProvider(details.fullName), viewmodel(details, index, mode,
            srn))
      }
  }

}
