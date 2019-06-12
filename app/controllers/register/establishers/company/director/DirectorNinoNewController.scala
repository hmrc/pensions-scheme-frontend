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
import controllers.NinoController
import controllers.actions._
import forms.NinoNewFormProvider
import identifiers.register.establishers.company.director.{DirectorDetailsId, DirectorNewNinoId}
import javax.inject.Inject
import models.{Index, Mode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import utils.Navigator
import utils.annotations.EstablishersCompanyDirector
import viewmodels.{Message, NinoViewModel}

class DirectorNinoNewController @Inject()(
                                           val appConfig: FrontendAppConfig,
                                           val messagesApi: MessagesApi,
                                           val userAnswersService: UserAnswersService,
                                           @EstablishersCompanyDirector val navigator: Navigator,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           allowAccess: AllowAccessActionProvider,
                                           requireData: DataRequiredAction,
                                           val formProvider: NinoNewFormProvider
                                 ) extends NinoController with I18nSupport {

  private[controllers] val postCall = controllers.register.establishers.company.director.routes.DirectorNinoNewController.onSubmit _
  private[controllers] val title: String = "messages__director_yes_nino__title"
  private[controllers] val heading: String = "messages__common_nino__h1"
  private[controllers] val hint: String = "messages__common__nino_hint"

  private def viewmodel(establisherIndex: Index, directorIndex: Index, mode: Mode, srn: Option[String]): Retrieval[NinoViewModel] =
    Retrieval {
      implicit request =>
        DirectorDetailsId(establisherIndex, directorIndex).retrieve.right.map {
          details =>
            NinoViewModel(
              postCall(mode, Index(establisherIndex), Index(directorIndex), srn),
              title = title,
              heading = heading,
              hint = hint,
              personName = details.fullName,
              srn = srn
            )
        }
    }

  def onPageLoad(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
    implicit request =>
      viewmodel(establisherIndex, directorIndex, mode, srn).retrieve.right.map {
        vm =>
          get(DirectorNewNinoId(establisherIndex, directorIndex), formProvider(vm.personName), vm)
      }
  }

  def onSubmit(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      viewmodel(establisherIndex, directorIndex, mode, srn).retrieve.right.map {
        vm =>
          post(DirectorNewNinoId(establisherIndex, directorIndex), mode, formProvider(vm.personName), vm)
      }
  }

}
