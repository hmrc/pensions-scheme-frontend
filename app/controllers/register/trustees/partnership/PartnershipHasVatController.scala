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

package controllers.register.trustees.partnership

import config.FrontendAppConfig
import controllers.HasReferenceNumberController
import controllers.actions._
import controllers.register.trustees.partnership.routes._
import forms.HasReferenceNumberFormProvider
import identifiers.register.trustees.partnership.{PartnershipDetailsId, PartnershipHasVATId}
import javax.inject.Inject
import models.{Index, Mode}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import viewmodels.{CommonFormWithHintViewModel, Message}

import scala.concurrent.ExecutionContext

class PartnershipHasVatController @Inject()(val appConfig: FrontendAppConfig,
                                            val messagesApi: MessagesApi,
                                            val userAnswersService: UserAnswersService,
                                            val navigator: Navigator,
                                            authenticate: AuthAction,
                                            getData: DataRetrievalAction,
                                            allowAccess: AllowAccessActionProvider,
                                            requireData: DataRequiredAction,
                                            formProvider: HasReferenceNumberFormProvider
                                           )(implicit val ec: ExecutionContext) extends HasReferenceNumberController {

  def form(partnershipName: String): Form[Boolean] = formProvider("messages__vat__formError", partnershipName)

  private def viewModel(mode: Mode, index: Index, srn: Option[String], partnershipName: String): CommonFormWithHintViewModel =
    CommonFormWithHintViewModel(
      postCall = PartnershipHasVatController.onSubmit(mode, index, srn),
      title = Message("messages__vat__title", partnershipName),
      heading = Message("messages__vat__heading", partnershipName),
      hint = None,
      srn = srn
    )

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        PartnershipDetailsId(index).retrieve.right.map {
          details =>
            get(PartnershipHasVATId(index), form(details.name), viewModel(mode, index, srn, details.name))
        }
    }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        PartnershipDetailsId(index).retrieve.right.map {
          details =>
            post(PartnershipHasVATId(index), mode: Mode, form(details.name), viewModel(mode, index, srn, details.name))
        }
    }
}
