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

package controllers.register.trustees.company

import config.FrontendAppConfig
import controllers.PhoneNumberController
import controllers.actions._
import forms.PhoneFormProvider
import identifiers.SchemeNameId
import identifiers.register.trustees.company.{CompanyDetailsId, CompanyPhoneId}
import models.{Index, Mode, OptionalSchemeReferenceNumber}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.phoneNumber

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class CompanyPhoneController @Inject()(val appConfig: FrontendAppConfig,
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
                                      )(implicit val ec: ExecutionContext) extends PhoneNumberController with I18nSupport {

  protected val form: Form[String] = formProvider()

  def onPageLoad(mode: Mode, index: Index, srn: OptionalSchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        viewModel(mode, srn, index).retrieve.map {
          vm =>
            get(CompanyPhoneId(index), form, vm)
        }
    }

  def onSubmit(mode: Mode, index: Index, srn: OptionalSchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        viewModel(mode, srn, index).retrieve.map {
          vm =>
            post(CompanyPhoneId(index), mode, form, vm)
        }
    }

  private def viewModel(mode: Mode, srn: OptionalSchemeReferenceNumber, index: Index): Retrieval[CommonFormWithHintViewModel] =
    Retrieval {
      implicit request =>
        for {
          schemeName <- SchemeNameId.retrieve
          details <- CompanyDetailsId(index).retrieve
        } yield {
            CommonFormWithHintViewModel(
              routes.CompanyPhoneController.onSubmit(mode, index, srn),
              Message("messages__trustee_phone__title"),
              Message("messages__enterPhoneNumber", details.companyName),
              Some(Message("messages__contact_phone__hint", details.companyName, schemeName)),
              srn = srn
            )
        }
    }
}
