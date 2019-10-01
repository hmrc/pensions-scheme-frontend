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

package controllers.register.establishers.company

import config.FrontendAppConfig
import controllers.EmailAddressController
import controllers.actions._
import forms.EmailFormProvider
import identifiers.register.establishers.company.{CompanyDetailsId, CompanyEmailId}
import javax.inject.Inject
import models.{Index, Mode}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import utils.annotations.EstablishersCompany
import viewmodels.{CommonFormWithHintViewModel, Message}

import scala.concurrent.ExecutionContext

class CompanyEmailController @Inject()(val appConfig: FrontendAppConfig,
                                       override val messagesApi: MessagesApi,
                                       authenticate: AuthAction,
                                       getData: DataRetrievalAction,
                                       override val userAnswersService: UserAnswersService,
                                       allowAccess: AllowAccessActionProvider,
                                       requireData: DataRequiredAction,
                                       @EstablishersCompany val navigator: Navigator,
                                       formProvider: EmailFormProvider
                                      )(implicit val ec: ExecutionContext) extends EmailAddressController with I18nSupport {

  protected val form: Form[String] = formProvider()

  private def viewModel(mode: Mode, srn: Option[String], index: Index): Retrieval[CommonFormWithHintViewModel] =
    Retrieval {
      implicit request =>
        CompanyDetailsId(index).retrieve.right.map {
          details =>
            CommonFormWithHintViewModel(
              routes.CompanyEmailController.onSubmit(mode, srn, index),
              Message("messages__enterEmail", Message("messages__theCompany").resolve),
              Message("messages__enterEmail", details.companyName),
              Some(Message("messages__email__hint")),
              srn = srn
            )
        }
    }

  def onPageLoad(mode: Mode, srn: Option[String], index: Index): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        viewModel(mode, srn, index).retrieve.right.map {
          vm =>
            get(CompanyEmailId(index), form, vm)
        }
    }

  def onSubmit(mode: Mode, srn: Option[String], index: Index): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
    implicit request =>
      viewModel(mode, srn, index).retrieve.right.map {
        vm =>
          post(CompanyEmailId(index), mode, form, vm, None)
      }
  }
}
