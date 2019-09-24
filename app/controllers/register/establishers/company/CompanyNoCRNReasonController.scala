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
import controllers.ReasonController
import controllers.actions._
import forms.register.NoCompanyNumberFormProvider
import identifiers.register.establishers.company.{CompanyDetailsId, CompanyNoCRNReasonId}
import javax.inject.Inject
import models.{Index, Mode}
import navigators.Navigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import utils.annotations.EstablishersCompany
import viewmodels.{Message, ReasonViewModel}

import scala.concurrent.ExecutionContext

class CompanyNoCRNReasonController @Inject()(override val appConfig: FrontendAppConfig,
                                          override val messagesApi: MessagesApi,
                                          override val userAnswersService: UserAnswersService,
                                          @EstablishersCompany val navigator: Navigator,
                                          authenticate: AuthAction,
                                          getData: DataRetrievalAction,
                                          allowAccess: AllowAccessActionProvider,
                                          requireData: DataRequiredAction,
                                          formProvider: NoCompanyNumberFormProvider
                                         )(implicit val ec: ExecutionContext) extends ReasonController with I18nSupport {

  protected def form(name: String) = formProvider(name)

  private def viewModel(mode: Mode, index: Index, srn: Option[String], companyName: String): ReasonViewModel = {
    ReasonViewModel(
      postCall = routes.CompanyNoCRNReasonController.onSubmit(mode, srn, index),
      title = Message("messages__noCompanyNumber__establisher__title"),
      heading = Message("messages__noCompanyNumber__establisher__heading", companyName),
      srn = srn
    )
  }

  def onPageLoad(mode: Mode, srn: Option[String], index: Index): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        CompanyDetailsId(index).retrieve.right.map { details =>
          val companyName = details.companyName
          get(CompanyNoCRNReasonId(index), viewModel(mode, index, srn, companyName), form(companyName))
        }
    }

  def onSubmit(mode: Mode, srn: Option[String], index: Index): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        CompanyDetailsId(index).retrieve.right.map { details =>
          val companyName = details.companyName
          post(CompanyNoCRNReasonId(index), mode, viewModel(mode, index, srn, companyName), form(companyName))
        }
    }

}
