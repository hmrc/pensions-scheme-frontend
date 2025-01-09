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

package controllers.register.establishers.partnership.partner

import config.FrontendAppConfig
import controllers.UTRController
import controllers.actions._
import forms.UTRFormProvider
import identifiers.register.establishers.partnership.partner.{PartnerEnterUTRId, PartnerNameId}
import javax.inject.Inject
import models.{Index, Mode, ReferenceValue}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import viewmodels.{Message, UTRViewModel}
import views.html.utr

import scala.concurrent.ExecutionContext
import models.SchemeReferenceNumber

class PartnerEnterUTRController @Inject()(
                                           override val appConfig: FrontendAppConfig,
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

  def onPageLoad(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: Option[SchemeReferenceNumber]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        PartnerNameId(establisherIndex, partnerIndex).retrieve.map { details =>
          val partnerName = details.fullName
          get(PartnerEnterUTRId(establisherIndex, partnerIndex), viewModel(mode, establisherIndex, partnerIndex, srn,
            partnerName), form)
        }
    }

  def onSubmit(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: Option[SchemeReferenceNumber]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        PartnerNameId(establisherIndex, partnerIndex).retrieve.map { details =>
          val partnerName = details.fullName
          post(PartnerEnterUTRId(establisherIndex, partnerIndex), mode, viewModel(mode, establisherIndex,
            partnerIndex, srn, partnerName), form)
        }
    }

  private def form: Form[ReferenceValue] = formProvider()

  private def viewModel(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: Option[SchemeReferenceNumber],
                        partnerName: String): UTRViewModel = {
    UTRViewModel(
      postCall = controllers.register.establishers.partnership.partner.routes.PartnerEnterUTRController.onSubmit
      (mode, establisherIndex, partnerIndex, srn),
      title = Message("messages__enterUTR", Message("messages__thePartner")),
      heading = Message("messages__enterUTR", partnerName),
      hint = Message("messages_utr__hint"),
      srn = srn
    )
  }


}
