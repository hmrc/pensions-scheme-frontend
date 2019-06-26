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
import controllers.HasPAYEController
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import forms.HasPAYEFormProvider
import identifiers.register.establishers.company.{CompanyDetailsId, HasCompanyNumberId, HasCompanyPAYEId}
import models.{Index, Mode}
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, Call}
import services.UserAnswersService
import utils.Navigator
import utils.annotations.EstablishersCompany
import viewmodels.{CommonFormWithHintViewModel, Message}

class HasCompanyPAYEController @Inject()(override val appConfig: FrontendAppConfig,
                                         override val messagesApi: MessagesApi,
                                         override val userAnswersService: UserAnswersService,
                                         @EstablishersCompany override val navigator: Navigator,
                                         authenticate: AuthAction,
                                         allowAccess: AllowAccessActionProvider,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: HasPAYEFormProvider
                                        ) extends HasPAYEController {

  private def postCall(mode: Mode, srn: Option[String], index: Int): Call = controllers.register.establishers.company.routes.HasCompanyPAYEController.onSubmit(mode, srn, index)


  private def viewModel(mode: Mode, index: Index, srn: Option[String], companyName: String): CommonFormWithHintViewModel =
    CommonFormWithHintViewModel(
      postCall = controllers.register.establishers.company.routes.HasCompanyPAYEController.onSubmit(mode, srn, index),
      title = Message("messages__companyPayeRef__title"),
      heading = Message("messages__companyPayeRef__h1", companyName),
      hint = Message("messages__companyPayeRef__p1"),
      srn = srn
    )

  private def form(companyName: String) = formProvider("messages__companyPayeRef__error__required", companyName)

  def onPageLoad(mode: Mode, srn: Option[String] = None, index: Index): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        CompanyDetailsId(index).retrieve.right.map {
          details =>
            get(HasCompanyPAYEId(index), form(details.companyName), viewModel(mode, index, srn, details.companyName))
        }
    }

  def onSubmit(mode: Mode, srn: Option[String] = None, index: Index): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        CompanyDetailsId(index).retrieve.right.map {
          details =>
            post(HasCompanyPAYEId(index), mode, form(details.companyName), viewModel(mode, index, srn, details.companyName))
        }
    }


  //  def onPageLoad(mode: Mode, srn: Option[String], index: Int): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requireData).async {
  //    implicit request =>
  //      retrieveCompanyName(index) {
  //        companyName =>
  //          val preparedForm = request.userAnswers.get(HasCompanyPAYEId(index)).fold(form)(v => form.fill(v))
  //          Future.successful(Ok(hasCompanyPAYE(appConfig, preparedForm, companyName, postCall(mode, srn, index), existingSchemeName)))
  //      }
  //  }
  //
  //  def onSubmit(mode: Mode, srn: Option[String], index: Int): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requireData).async {
  //    implicit request =>
  //      retrieveCompanyName(index) { companyName =>
  //        form.bindFromRequest().fold(
  //          (formWithErrors: Form[_]) =>
  //            Future.successful(BadRequest(hasCompanyPAYE(appConfig, formWithErrors, companyName, postCall(mode, srn, index), existingSchemeName))),
  //          value => {
  //            userAnswersService.save(mode, srn, HasCompanyPAYEId(index), value).map { cacheMap =>
  //              Redirect(navigator.nextPage(HasCompanyPAYEId(index), mode, UserAnswers(cacheMap), srn))
  //            }
  //          }
  //        )
  //      }
  //  }

}
