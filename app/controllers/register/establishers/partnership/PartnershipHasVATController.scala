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

package controllers.register.establishers.partnership

import config.FrontendAppConfig
import controllers.HasReferenceNumberController
import controllers.actions._
import controllers.register.establishers.partnership.routes._
import forms.HasReferenceNumberFormProvider
import identifiers.register.establishers.partnership.{PartnershipDetailsId, PartnershipHasVATId}
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

class PartnershipHasVATController @Inject()(val appConfig: FrontendAppConfig,
                                            override val messagesApi: MessagesApi,
                                            val userAnswersService: UserAnswersService,
                                            val navigator: Navigator,
                                            authenticate: AuthAction,
                                            getData: DataRetrievalAction,
                                            allowAccess: AllowAccessActionProvider,
                                            requireData: DataRequiredAction,
                                            formProvider: HasReferenceNumberFormProvider,
                                            val controllerComponents: MessagesControllerComponents,
                                            val view: hasReferenceNumber
                                           )(implicit val executionContext: ExecutionContext) extends HasReferenceNumberController {

  def form(partnershipName: String)(implicit request: DataRequest[AnyContent]): Form[Boolean] = formProvider("messages__vat__formError", partnershipName)

  private def viewModel(mode: Mode, index: Index, srn: Option[String], partnershipName: String): CommonFormWithHintViewModel =
    CommonFormWithHintViewModel(
      postCall = PartnershipHasVATController.onSubmit(mode, index, srn),
      title = Message("messages__hasVAT", Message("messages__thePartnership")),
      heading = Message("messages__hasVAT", partnershipName),
      hint = None,
      srn = srn
    )

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        PartnershipDetailsId(index).retrieve.map {
          details =>
            get(PartnershipHasVATId(index), form(details.name), viewModel(mode, index, srn, details.name))
        }
    }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        PartnershipDetailsId(index).retrieve.map {
          details =>
            post(PartnershipHasVATId(index), mode: Mode, form(details.name), viewModel(mode, index, srn, details.name))
        }
    }
}
