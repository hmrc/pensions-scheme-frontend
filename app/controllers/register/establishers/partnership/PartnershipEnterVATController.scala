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
import controllers.EnterVATController
import controllers.actions._
import forms.EnterVATFormProvider
import identifiers.register.establishers.partnership.{PartnershipDetailsId, PartnershipEnterVATId}

import javax.inject.Inject
import models.requests.DataRequest
import models.{Index, Mode, ReferenceValue, SchemeReferenceNumber}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import viewmodels.{EnterVATViewModel, Message}
import views.html.enterVATView

import scala.concurrent.ExecutionContext

class PartnershipEnterVATController @Inject()(
                                               override val appConfig: FrontendAppConfig,
                                               override val messagesApi: MessagesApi,
                                               override val userAnswersService: UserAnswersService,
                                               override val navigator: Navigator,
                                               authenticate: AuthAction,
                                               getData: DataRetrievalAction,
                                               allowAccess: AllowAccessActionProvider,
                                               requireData: DataRequiredAction,
                                               formProvider: EnterVATFormProvider,
                                               val controllerComponents: MessagesControllerComponents,
                                               val view: enterVATView
                                             )(implicit val ec: ExecutionContext) extends EnterVATController {

  def onPageLoad(mode: Mode, index: Index, srn: SchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        PartnershipDetailsId(index).retrieve.map { details =>
          val partnershipName = details.name
          get(PartnershipEnterVATId(index), viewModel(mode, index, srn, partnershipName), form(partnershipName))
        }
    }

  private def form(companyName: String)
                  (implicit request: DataRequest[AnyContent]): Form[ReferenceValue] = formProvider(companyName)

  private def viewModel(mode: Mode, index: Index, srn: SchemeReferenceNumber, partnershipName: String): EnterVATViewModel = {
    EnterVATViewModel(
      postCall = routes.PartnershipEnterVATController.onSubmit(mode, index, srn),
      title = Message("messages__enterVAT", Message("messages__thePartnership")),
      heading = Message("messages__enterVAT", partnershipName),
      hint = Message("messages__enterVAT__hint", partnershipName),
      subHeading = None,
      srn = srn
    )
  }

  def onSubmit(mode: Mode, index: Index, srn: SchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        PartnershipDetailsId(index).retrieve.map { details =>
          val partnershipName = details.name
          post(PartnershipEnterVATId(index), mode, viewModel(mode, index, srn, partnershipName), form(partnershipName))
        }
    }
}
