/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.register.establishers.partnership

import config.FrontendAppConfig
import controllers.UTRController
import controllers.actions._
import forms.UTRFormProvider
import identifiers.register.establishers.partnership.{PartnershipDetailsId, PartnershipEnterUTRId}

import javax.inject.Inject
import models.{Index, Mode, OptionalSchemeReferenceNumber, ReferenceValue, SchemeReferenceNumber}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import viewmodels.{Message, UTRViewModel}
import views.html.utr

import scala.concurrent.ExecutionContext

class PartnershipEnterUTRController @Inject()(override val appConfig: FrontendAppConfig,
                                              override val messagesApi: MessagesApi,
                                              override val userAnswersService: UserAnswersService,
                                              override val navigator: Navigator,
                                              authenticate: AuthAction,
                                              getData: DataRetrievalAction,
                                              allowAccess: AllowAccessActionProvider,
                                              requireData: DataRequiredAction,
                                              formProvider: UTRFormProvider,
                                              val controllerComponents: MessagesControllerComponents,
                                              val view: utr
                                             )(implicit val ec: ExecutionContext) extends UTRController {

  def onPageLoad(mode: Mode, index: Index, srn: OptionalSchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        PartnershipDetailsId(index).retrieve.map { details =>
          get(PartnershipEnterUTRId(index), viewModel(mode, index, srn, details.name), form)
        }
    }

  private def form: Form[ReferenceValue] = formProvider()

  private def viewModel(mode: Mode, index: Index, srn: OptionalSchemeReferenceNumber, partnershipName: String): UTRViewModel = {
    UTRViewModel(
      postCall = routes.PartnershipEnterUTRController.onSubmit(mode, index, srn),
      title = Message("messages__enterUTR", Message("messages__thePartnership")),
      heading = Message("messages__enterUTR", partnershipName),
      hint = Message("messages_utr__hint"),
      srn = srn
    )
  }

  def onSubmit(mode: Mode, index: Index, srn: OptionalSchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        PartnershipDetailsId(index).retrieve.map { details =>
          post(PartnershipEnterUTRId(index), mode, viewModel(mode, index, srn, details.name), form)
        }
    }
}
