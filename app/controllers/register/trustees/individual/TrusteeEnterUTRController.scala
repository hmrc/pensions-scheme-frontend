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

package controllers.register.trustees.individual

import config.FrontendAppConfig
import controllers.UTRController
import controllers.actions._
import forms.UTRFormProvider
import identifiers.register.trustees.individual.{TrusteeNameId, TrusteeUTRId}
import javax.inject.Inject
import models.requests.DataRequest
import models.{Index, Mode, ReferenceValue}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import viewmodels.{Message, UTRViewModel}
import views.html.utr

import scala.concurrent.ExecutionContext

class TrusteeEnterUTRController @Inject()(val appConfig: FrontendAppConfig,
                                          override val messagesApi: MessagesApi,
                                     val userAnswersService: UserAnswersService,
                                     val navigator: Navigator,
                                     authenticate: AuthAction,
                                     getData: DataRetrievalAction,
                                     allowAccess: AllowAccessActionProvider,
                                     requireData: DataRequiredAction,
                                     formProvider: UTRFormProvider,
                                     val controllerComponents: MessagesControllerComponents,
                                     val view: utr
                                     )(implicit val ec: ExecutionContext) extends UTRController {

  private def form: Form[ReferenceValue] = formProvider()

  private def viewModel(mode: Mode, index: Index, srn: Option[String], trusteeName: String
                       )(implicit request: DataRequest[AnyContent]): UTRViewModel = {
    UTRViewModel(
      postCall = routes.TrusteeEnterUTRController.onSubmit(mode, index, srn),
      title = Message("messages__enterUTR", Message("messages__theIndividual")),
      heading = Message("messages__enterUTR", trusteeName),
      hint = Message("messages_utr__hint"),
      srn = srn
    )
  }

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        TrusteeNameId(index).retrieve.right.map { trusteeName =>
          get(TrusteeUTRId(index), viewModel(mode, index, srn, trusteeName.fullName), form)
        }
    }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      TrusteeNameId(index).retrieve.right.map { trusteeName =>
        post(TrusteeUTRId(index), mode, viewModel(mode, index, srn, trusteeName.fullName), form)
      }
  }
}
