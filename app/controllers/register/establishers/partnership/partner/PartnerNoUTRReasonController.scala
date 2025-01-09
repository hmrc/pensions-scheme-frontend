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
import controllers.ReasonController
import controllers.actions._
import forms.ReasonFormProvider
import identifiers.register.establishers.partnership.partner.{PartnerNameId, PartnerNoUTRReasonId}
import javax.inject.Inject
import models.requests.DataRequest
import models.{Index, Mode}
import navigators.Navigator
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import viewmodels.{Message, ReasonViewModel}
import views.html.reason

import scala.concurrent.ExecutionContext
import models.SchemeReferenceNumber

class PartnerNoUTRReasonController @Inject()(override val appConfig: FrontendAppConfig,
                                             override val messagesApi: MessagesApi,
                                             override val userAnswersService: UserAnswersService,
                                             override val navigator: Navigator,
                                             authenticate: AuthAction,
                                             getData: DataRetrievalAction,
                                             allowAccess: AllowAccessActionProvider,
                                             requireData: DataRequiredAction,
                                             formProvider: ReasonFormProvider,
                                             val controllerComponents: MessagesControllerComponents,
                                             val view: reason
                                            )(implicit val ec: ExecutionContext) extends ReasonController {

  def onPageLoad(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: Option[SchemeReferenceNumber]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        PartnerNameId(establisherIndex, partnerIndex).retrieve.map { details =>
          val partnerName = details.fullName
          get(PartnerNoUTRReasonId(establisherIndex, partnerIndex), viewModel(mode, establisherIndex, partnerIndex,
            srn, partnerName), form(partnerName))
        }
    }

  def onSubmit(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: Option[SchemeReferenceNumber]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        PartnerNameId(establisherIndex, partnerIndex).retrieve.map { details =>
          val partnerName = details.fullName
          post(PartnerNoUTRReasonId(establisherIndex, partnerIndex), mode, viewModel(mode, establisherIndex,
            partnerIndex, srn, partnerName), form(partnerName))
        }
    }

  private def form(partnerName: String)(implicit request: DataRequest[AnyContent]) =
    formProvider("messages__reason__error_utrRequired", partnerName)

  private def viewModel(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: Option[SchemeReferenceNumber],
                        partnerName: String): ReasonViewModel = {
    ReasonViewModel(
      postCall = routes.PartnerNoUTRReasonController.onSubmit(mode, establisherIndex, partnerIndex, srn),
      title = Message("messages__whyNoUTR", Message("messages__thePartner")),
      heading = Message("messages__whyNoUTR", partnerName),
      srn = srn
    )
  }

}
