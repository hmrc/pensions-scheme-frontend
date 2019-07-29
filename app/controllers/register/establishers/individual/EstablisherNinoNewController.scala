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
import forms.NinoNewFormProvider
import identifiers.register.establishers.EstablisherNewNinoId
import identifiers.register.establishers.individual.EstablisherDetailsId
import javax.inject.Inject
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
                                           val formProvider: NinoNewFormProvider
                                 )(implicit val ec: ExecutionContext) extends NinoController with I18nSupport {

  private[controllers] val postCall = controllers.register.establishers.individual.routes.EstablisherNinoNewController.onSubmit _
  private[controllers] val title: Message = "messages__common_nino__title"
  private[controllers] val heading: Message = "messages__common_nino__h1"
  private[controllers] val hint: Message = "messages__common__nino_hint"

  private def viewmodel(index: Index,  mode: Mode, srn: Option[String]): Retrieval[NinoViewModel] =
    Retrieval {
      implicit request =>
        EstablisherDetailsId(index).retrieve.right.map {
          details =>
            NinoViewModel(
              postCall(mode, Index(index), srn),
              title = title,
              heading = heading,
              hint = hint,
              personName = details.fullName,
              srn = srn
            )
        }
    }

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
    implicit request =>
      viewmodel(index, mode, srn).retrieve.right.map {
        vm =>
          get(EstablisherNewNinoId(index), formProvider(vm.personName), vm)
      }
  }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      viewmodel(index, mode, srn).retrieve.right.map {
        vm =>
          post(EstablisherNewNinoId(index), mode, formProvider(vm.personName), vm)
      }
  }

}
