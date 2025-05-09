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

package controllers.register.trustees.partnership

import config.FrontendAppConfig
import controllers.HasReferenceNumberController
import controllers.actions._
import forms.HasUTRFormProvider
import identifiers.register.trustees.partnership.{PartnershipDetailsId, PartnershipHasUTRId}
import models.requests.DataRequest
import models.{EmptyOptionalSchemeReferenceNumber, Index, Mode, OptionalSchemeReferenceNumber}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.hasReferenceNumber

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class PartnershipHasUTRController @Inject()(override val appConfig: FrontendAppConfig,
                                            override val messagesApi: MessagesApi,
                                            override val userAnswersService: UserAnswersService,
                                            override val navigator: Navigator,
                                            authenticate: AuthAction,
                                            allowAccess: AllowAccessActionProvider,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            formProvider: HasUTRFormProvider,
                                            val controllerComponents: MessagesControllerComponents,
                                            val view: hasReferenceNumber
                                           )(implicit val executionContext: ExecutionContext) extends
  HasReferenceNumberController {

  def onPageLoad(mode: Mode, index: Index, srn: OptionalSchemeReferenceNumber = EmptyOptionalSchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        PartnershipDetailsId(index).retrieve.map {
          details =>
            get(PartnershipHasUTRId(index), form(details.name), viewModel(mode, index, srn, details.name))
        }
    }

  private def viewModel(mode: Mode, index: Index, srn: OptionalSchemeReferenceNumber, companyName: String): CommonFormWithHintViewModel =
    CommonFormWithHintViewModel(
      postCall = routes.PartnershipHasUTRController.onSubmit(mode, index, srn),
      title = Message("messages__hasUTR", Message("messages__thePartnership")),
      heading = Message("messages__hasUTR", companyName),
      hint = Some(Message("messages__hasUtr__p1")),
      srn = srn
    )

  private def form(partnershipName: String)(implicit request: DataRequest[AnyContent]): Form[Boolean] =
    formProvider("messages__hasUtr__partnership_error_required", partnershipName)

  def onSubmit(mode: Mode, index: Index, srn: OptionalSchemeReferenceNumber = EmptyOptionalSchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        PartnershipDetailsId(index).retrieve.map {
          details =>
            post(PartnershipHasUTRId(index), mode, form(details.name), viewModel(mode, index, srn, details.name))
        }
    }
}
