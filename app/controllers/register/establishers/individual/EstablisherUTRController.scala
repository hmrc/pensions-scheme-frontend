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
import controllers.UTRController
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import forms.UTRFormProvider
import identifiers.register.establishers.individual.{EstablisherNameId, EstablisherUTRId}
import javax.inject.Inject
import models.{Index, Mode, ReferenceValue}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import viewmodels.{Message, UTRViewModel}

import scala.concurrent.ExecutionContext

class EstablisherUTRController @Inject()(override val appConfig: FrontendAppConfig,
                                         override val messagesApi: MessagesApi,
                                         override val userAnswersService: UserAnswersService,
                                         val navigator: Navigator,
                                         authenticate: AuthAction,
                                         getData: DataRetrievalAction,
                                         allowAccess: AllowAccessActionProvider,
                                         requireData: DataRequiredAction,
                                         formProvider: UTRFormProvider)
                                        (implicit val ec: ExecutionContext) extends UTRController {

  private def form: Form[ReferenceValue] = formProvider()

  private def viewModel(mode: Mode, index: Index, srn: Option[String], companyName: String): UTRViewModel = {
    UTRViewModel(
      postCall = routes.EstablisherUTRController.onSubmit(mode, index, srn),
      title = Message("messages__personUTR__title"),
      heading = Message("messages__dynamic_whatIsUTR", companyName),
      hint = Message("messages_utr__hint"),
      srn = srn
    )
  }

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        EstablisherNameId(index).retrieve.right.map { details =>
          get(EstablisherUTRId(index), viewModel(mode, index, srn, details.fullName), form)
        }
    }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        EstablisherNameId(index).retrieve.right.map { details =>
          post(EstablisherUTRId(index), mode, viewModel(mode, index, srn, details.fullName), form)
        }
    }
}
