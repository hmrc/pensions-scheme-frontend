/*
 * Copyright 2023 HM Revenue & Customs
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
import controllers.PhoneNumberController
import controllers.actions._
import forms.PhoneFormProvider
import identifiers.SchemeNameId
import identifiers.register.trustees.partnership.{PartnershipDetailsId, PartnershipPhoneId}
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

class PartnershipPhoneNumberController @Inject()(val appConfig: FrontendAppConfig,
                                                 override val messagesApi: MessagesApi,
                                                 authenticate: AuthAction,
                                                 getData: DataRetrievalAction,
                                                 override val userAnswersService: UserAnswersService,
                                                 allowAccess: AllowAccessActionProvider,
                                                 requireData: DataRequiredAction,
                                                 override val navigator: Navigator,
                                                 formProvider: PhoneFormProvider,
                                                 val controllerComponents: MessagesControllerComponents,
                                                 val view: phoneNumber
                                                )(implicit val ec: ExecutionContext) extends PhoneNumberController
  with I18nSupport {

  protected val form: Form[String] = formProvider()

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        viewModel(mode, srn, index).retrieve.map {
          vm =>
            get(PartnershipPhoneId(index), form, vm)
        }
    }

  private def viewModel(mode: Mode, srn: Option[String], index: Index): Retrieval[CommonFormWithHintViewModel] =
    Retrieval {
      implicit request =>
        for {
          schemeName <- SchemeNameId.retrieve
          details <- PartnershipDetailsId(index).retrieve
        } yield {
            CommonFormWithHintViewModel(
              routes.PartnershipPhoneNumberController.onSubmit(mode, index, srn),
              Message("messages__enterPhoneNumber", Message("messages__thePartnership")),
              Message("messages__enterPhoneNumber", details.name),
              Some(Message("messages__contact_phone__hint", details.name, schemeName)),
              srn = srn
            )
        }
    }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        viewModel(mode, srn, index).retrieve.map {
          vm =>
            post(PartnershipPhoneId(index), mode, form, vm)
        }
    }
}
