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

package controllers.register.trustees.partnership

import config.FrontendAppConfig
import controllers.EmailAddressController
import controllers.actions._
import forms.EmailFormProvider
import identifiers.register.trustees.partnership.{PartnershipDetailsId, PartnershipEmailId}
import javax.inject.Inject
import models.{Index, Mode}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.emailAddress

import scala.concurrent.ExecutionContext

class PartnershipEmailController @Inject()(val appConfig: FrontendAppConfig,
                                           override val messagesApi: MessagesApi,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           override val userAnswersService: UserAnswersService,
                                           allowAccess: AllowAccessActionProvider,
                                           requireData: DataRequiredAction,
                                           override val navigator: Navigator,
                                           formProvider: EmailFormProvider,
                                           val controllerComponents: MessagesControllerComponents,
                                           val view: emailAddress
                                          )(implicit val executionContext: ExecutionContext) extends
  EmailAddressController with I18nSupport {

  protected val form: Form[String] = formProvider()

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        viewModel(mode, srn, index).retrieve.right.map {
          vm =>
            get(PartnershipEmailId(index), form, vm)
        }
    }

  private def viewModel(mode: Mode, srn: Option[String], index: Index): Retrieval[CommonFormWithHintViewModel] =
    Retrieval {
      implicit request =>
        PartnershipDetailsId(index).retrieve.right.map {
          details =>
            CommonFormWithHintViewModel(
              controllers.register.trustees.partnership.routes.PartnershipEmailController.onSubmit(mode, index, srn),
              Message("messages__enterEmail", Message("messages__thePartnership")),
              Message("messages__enterEmail", details.name),
              Some(Message("messages__contact_details__hint", details.name)),
              srn = srn
            )
        }
    }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        viewModel(mode, srn, index).retrieve.right.map {
          vm =>
            post(PartnershipEmailId(index), mode, form, vm, None)
        }
    }
}
