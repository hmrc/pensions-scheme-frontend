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

package controllers.register.establishers.company

import config.FrontendAppConfig
import controllers.UTRController
import controllers.actions._
import forms.UTRFormProvider
import identifiers.register.establishers.company.{CompanyDetailsId, CompanyEnterUTRId}
import javax.inject.Inject
import models.{Index, Mode, ReferenceValue}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import utils.annotations.EstablishersCompany
import viewmodels.{Message, UTRViewModel}
import views.html.utr

import scala.concurrent.ExecutionContext

class CompanyEnterUTRController @Inject()(
                                           override val appConfig: FrontendAppConfig,
                                           override val messagesApi: MessagesApi,
                                           override val userAnswersService: UserAnswersService,
                                           @EstablishersCompany override val navigator: Navigator,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           allowAccess: AllowAccessActionProvider,
                                           requireData: DataRequiredAction,
                                           formProvider: UTRFormProvider,
                                           val view: utr,
                                           val controllerComponents: MessagesControllerComponents
                                         )(implicit val ec: ExecutionContext) extends UTRController {

  def onPageLoad(mode: Mode, srn: Option[String], index: Index): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        CompanyDetailsId(index).retrieve.map { details =>
          val companyName = details.companyName
          get(CompanyEnterUTRId(index), viewModel(mode, index, srn, companyName), form)
        }
    }

  def onSubmit(mode: Mode, srn: Option[String], index: Index): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        CompanyDetailsId(index).retrieve.map { details =>
          val companyName = details.companyName
          post(CompanyEnterUTRId(index), mode, viewModel(mode, index, srn, companyName), form)
        }
    }

  private def form: Form[ReferenceValue] = formProvider()

  private def viewModel(mode: Mode, index: Index, srn: Option[String], companyName: String): UTRViewModel = {
    UTRViewModel(
      postCall = routes.CompanyEnterUTRController.onSubmit(mode, srn, index),
      title = Message("messages__enterUTR", Message("messages__theCompany")),
      heading = Message("messages__enterUTR", companyName),
      hint = Message("messages_utr__hint"),
      srn = srn
    )
  }
}
