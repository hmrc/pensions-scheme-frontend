/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.register.establishers.company

import config.FrontendAppConfig
import controllers.PhoneNumberController
import controllers.actions._
import forms.PhoneFormProvider
import identifiers.register.establishers.company.{CompanyDetailsId, CompanyPhoneId}
import javax.inject.Inject
import models.{Index, Mode}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import utils.annotations.EstablishersCompany
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.phoneNumber

import scala.concurrent.ExecutionContext

class CompanyPhoneController @Inject()(val appConfig: FrontendAppConfig,
                                       override val messagesApi: MessagesApi,
                                       authenticate: AuthAction,
                                       getData: DataRetrievalAction,
                                       override val userAnswersService: UserAnswersService,
                                       allowAccess: AllowAccessActionProvider,
                                       requireData: DataRequiredAction,
                                       @EstablishersCompany val navigator: Navigator,
                                       formProvider: PhoneFormProvider,
                                       val view: phoneNumber,
                                       val controllerComponents: MessagesControllerComponents
                                      )(implicit val ec: ExecutionContext) extends PhoneNumberController with
  I18nSupport {

  protected val form: Form[String] = formProvider()

  def onPageLoad(mode: Mode, srn: Option[String], index: Index): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        viewModel(mode, srn, index).retrieve.right.map {
          vm =>
            get(CompanyPhoneId(index), form, vm)
        }
    }

  private def viewModel(mode: Mode, srn: Option[String], index: Index): Retrieval[CommonFormWithHintViewModel] =
    Retrieval {
      implicit request =>
        CompanyDetailsId(index).retrieve.right.map {
          details =>
            CommonFormWithHintViewModel(
              routes.CompanyPhoneController.onSubmit(mode, srn, index),
              Message("messages__enterPhoneNumber", Message("messages__theCompany")),
              Message("messages__enterPhoneNumber", details.companyName),
              Some(Message("messages__contact_details__hint", details.companyName)),
              srn = srn
            )
        }
    }

  def onSubmit(mode: Mode, srn: Option[String], index: Index): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        viewModel(mode, srn, index).retrieve.right.map {
          vm =>
            post(CompanyPhoneId(index), mode, form, vm)
        }
    }
}
