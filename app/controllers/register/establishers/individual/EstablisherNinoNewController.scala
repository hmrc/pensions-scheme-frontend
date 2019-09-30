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

package controllers.register.establishers.individual

import config.FrontendAppConfig
import controllers.NinoController
import controllers.actions._
import forms.NINOFormProvider
import identifiers.register.establishers.individual.{EstablisherDetailsId, EstablisherEnterNINOId}
import javax.inject.Inject
import models.person.PersonDetails
import models.{Index, Mode}
import navigators.Navigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import utils.annotations.EstablishersIndividual
import viewmodels.{Message, NinoViewModel}

import scala.concurrent.ExecutionContext

class EstablisherNinoNewController @Inject()(
                                           val appConfig: FrontendAppConfig,
                                           val messagesApi: MessagesApi,
                                           val userAnswersService: UserAnswersService,
                                           @EstablishersIndividual val navigator: Navigator,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           allowAccess: AllowAccessActionProvider,
                                           requireData: DataRequiredAction,
                                           val formProvider: NINOFormProvider
                                 )(implicit val ec: ExecutionContext) extends NinoController with I18nSupport {

  private[controllers] val postCall = controllers.register.establishers.individual.routes.EstablisherNinoNewController.onSubmit _

  private def viewmodel(personDetails: PersonDetails, index: Index,  mode: Mode, srn: Option[String]): NinoViewModel =
    NinoViewModel(
      postCall(mode, Index(index), srn),
      title = Message("messages__enterNino", Message("messages__thePerson").resolve),
      heading = Message("messages__enterNino", personDetails.fullName),
      hint = Message("messages__common__nino_hint"),
      srn = srn
    )

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
    implicit request =>
      EstablisherDetailsId(index).retrieve.right.map {
        details =>
          get(EstablisherEnterNINOId(index), formProvider(details.fullName), viewmodel(details, index, mode, srn))
      }
  }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      EstablisherDetailsId(index).retrieve.right.map {
        details =>
          post(EstablisherEnterNINOId(index), mode, formProvider(details.fullName), viewmodel(details, index, mode, srn))
      }
  }

}
