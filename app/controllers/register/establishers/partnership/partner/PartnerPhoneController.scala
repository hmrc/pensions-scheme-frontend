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
import controllers.PhoneNumberController
import controllers.actions._
import forms.PhoneFormProvider
import identifiers.register.establishers.partnership.partner.{PartnerNameId, PartnerPhoneId}
import javax.inject.Inject
import models.{Index, Mode}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.phoneNumber

import scala.concurrent.ExecutionContext

class PartnerPhoneController @Inject()(
                                        val appConfig: FrontendAppConfig,
                                        override val messagesApi: MessagesApi,
                                        authenticate: AuthAction,
                                        getData: DataRetrievalAction,
                                        override val userAnswersService: UserAnswersService,
                                        allowAccess: AllowAccessActionProvider,
                                        requireData: DataRequiredAction,
                                        val navigator: Navigator,
                                        formProvider: PhoneFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        val view: phoneNumber
                                      )(implicit val ec: ExecutionContext) extends PhoneNumberController with
  I18nSupport {


  protected val form: Form[String] = formProvider()

  def onPageLoad(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        viewModel(mode, srn, establisherIndex, partnerIndex).retrieve.map {
          vm =>
            get(PartnerPhoneId(establisherIndex, partnerIndex), form, vm)
        }
    }

  def onSubmit(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        viewModel(mode, srn, establisherIndex, partnerIndex).retrieve.map {
          vm =>
            post(PartnerPhoneId(establisherIndex, partnerIndex), mode, form, vm)
        }
    }

  private def viewModel(mode: Mode, srn: Option[String], establisherIndex: Index, partnerIndex: Index)
  : Retrieval[CommonFormWithHintViewModel] =
    Retrieval {
      implicit request =>
        PartnerNameId(establisherIndex, partnerIndex).retrieve.map {
          details =>
            CommonFormWithHintViewModel(
              routes.PartnerPhoneController.onSubmit(mode, establisherIndex, partnerIndex, srn),
              Message("messages__enterPhoneNumber", Message("messages__thePartner")),
              Message("messages__enterPhoneNumber", details.fullName),
              Some(Message("messages__contact_details__hint", details.fullName)),
              srn = srn
            )
        }
    }

}
