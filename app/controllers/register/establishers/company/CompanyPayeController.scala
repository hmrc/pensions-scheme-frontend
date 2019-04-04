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

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.PayeController
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import forms.PayeFormProvider
import identifiers.register.establishers.company.{CompanyDetailsId, CompanyPayeId}
import models.{Index, Mode, Paye}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import utils.Navigator
import utils.annotations.EstablishersCompany
import viewmodels.{Message, PayeViewModel}

class CompanyPayeController @Inject()(
                                       val appConfig: FrontendAppConfig,
                                       override val messagesApi: MessagesApi,
                                       override val userAnswersService: UserAnswersService,
                                       @EstablishersCompany val navigator: Navigator,
                                       authenticate: AuthAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       formProvider: PayeFormProvider
                                     ) extends PayeController with I18nSupport {

  protected val form: Form[Paye] = formProvider("messages__companyPaye__error__required")

  private def viewmodel(mode: Mode, index: Index, srn: Option[String]): Retrieval[PayeViewModel] =
    Retrieval {
      implicit request =>
        CompanyDetailsId(index).retrieve.right.map {
          details =>
            PayeViewModel(
              postCall = routes.CompanyPayeController.onSubmit(mode, index, srn),
              title = Message("messages__companyPaye__title"),
              heading = Message("messages__companyPaye__heading", details.companyName),
              hint = Some(Message("messages__common__paye_hint")),
              subHeading = None
            )
        }
    }

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(mode, index, srn).retrieve.right.map {
        vm =>
          get(CompanyPayeId(index), form, vm)
      }
  }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(mode, index, srn).retrieve.right.map {
        vm =>
          post(CompanyPayeId(index), mode, form, vm)
      }
  }
}
