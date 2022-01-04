/*
 * Copyright 2022 HM Revenue & Customs
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
import forms.HasPAYEFormProvider
import identifiers.register.trustees.partnership.{PartnershipDetailsId, PartnershipHasPAYEId}
import javax.inject.Inject
import models.requests.DataRequest
import models.{Index, Mode}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.hasReferenceNumber

import scala.concurrent.ExecutionContext

class PartnershipHasPAYEController @Inject()(override val appConfig: FrontendAppConfig,
                                             override val messagesApi: MessagesApi,
                                             override val userAnswersService: UserAnswersService,
                                             override val navigator: Navigator,
                                             authenticate: AuthAction,
                                             allowAccess: AllowAccessActionProvider,
                                             getData: DataRetrievalAction,
                                             requireData: DataRequiredAction,
                                             formProvider: HasPAYEFormProvider,
                                             val controllerComponents: MessagesControllerComponents,
                                             val view: hasReferenceNumber
                                            )(implicit val executionContext: ExecutionContext) extends
  HasReferenceNumberController {

  def onPageLoad(mode: Mode, index: Index, srn: Option[String] = None): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        PartnershipDetailsId(index).retrieve.right.map {
          details =>
            get(PartnershipHasPAYEId(index), form(details.name), viewModel(mode, index, srn, details.name))
        }
    }

  private def viewModel(mode: Mode, index: Index, srn: Option[String], partnershipName: String): CommonFormWithHintViewModel =
    CommonFormWithHintViewModel(
      postCall = routes.PartnershipHasPAYEController.onSubmit(mode, index, srn),
      title = Message("messages__hasPAYE", Message("messages__thePartnership")),
      heading = Message("messages__hasPAYE", partnershipName),
      hint = Some(Message("messages__hasPaye__p1")),
      srn = srn,
      formFieldName = Some("hasPaye")
    )

  private def form(partnershipName: String)(implicit request: DataRequest[AnyContent]): Form[Boolean] =
    formProvider("messages__partnershipHasPaye__error__required", partnershipName)

  def onSubmit(mode: Mode, index: Index, srn: Option[String] = None): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        PartnershipDetailsId(index).retrieve.right.map {
          details =>
            post(PartnershipHasPAYEId(index), mode, form(details.name), viewModel(mode, index, srn, details.name))
        }
    }
}
