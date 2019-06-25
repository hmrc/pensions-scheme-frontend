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
import controllers.Retrievals
import controllers.actions._
import forms.register.establishers.company.NoCompanyNumberFormProvider
import identifiers.register.establishers.company.{CompanyDetailsId, NoCompanyNumberId}
import javax.inject.Inject
import models.{Index, Mode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.EstablishersCompany
import utils.{Navigator, UserAnswers}
import viewmodels.{Message, NoCompanyNumberViewModel}
import views.html.register.establishers.company.noCompanyNumber

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class NoCompanyNumberController @Inject()(appConfig: FrontendAppConfig,
                                          override val messagesApi: MessagesApi,
                                          val userAnswersService: UserAnswersService,
                                          @EstablishersCompany val navigator: Navigator,
                                          authenticate: AuthAction,
                                          getData: DataRetrievalAction,
                                          allowAccess: AllowAccessActionProvider,
                                          requireData: DataRequiredAction,
                                          formProvider: NoCompanyNumberFormProvider) extends FrontendController with Retrievals with I18nSupport {

  protected implicit val ec = play.api.libs.concurrent.Execution.defaultContext

  protected def form(name: String) = formProvider(name)

  private def postCall: (Mode, Option[String], Index) => Call = routes.NoCompanyNumberController.onSubmit _

  private def viewModel(mode: Mode, index: Index, srn: Option[String], companyName: String): NoCompanyNumberViewModel = {
    NoCompanyNumberViewModel(
      title = Message("messages__noCompanyNumber__establisher__title"),
      heading = Message("messages__noCompanyNumber__establisher__heading", companyName)
    )
  }

  def onPageLoad(mode: Mode, srn: Option[String] = None, index: Index): Action[AnyContent] =
      (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
        implicit request =>
          CompanyDetailsId(index).retrieve.right.map { details =>
            val companyName = details.companyName

            val preparedForm =
              request.userAnswers.get(NoCompanyNumberId(index)) match {
                case Some(value) => form(companyName).fill(value)
                case _ => form(companyName)
              }

            val view = noCompanyNumber(appConfig, viewModel(mode, index, srn, companyName), preparedForm, existingSchemeName, postCall(mode, srn, index), srn)

            Future.successful(Ok(view))
          }

  }

  def onSubmit(mode: Mode, srn: Option[String], index: Index): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        CompanyDetailsId(index).retrieve.right.map { details =>
          val companyName = details.companyName
          form(companyName).bindFromRequest().fold(
            (formWithErrors: Form[_]) =>

              Future.successful(BadRequest(
                noCompanyNumber(appConfig, viewModel(mode, index, srn, companyName), formWithErrors,  existingSchemeName, postCall(mode, srn, index), srn))),

            reason => {
              val updatedUserAnswers = request.userAnswers
                .set(NoCompanyNumberId(index))(reason).asOpt.getOrElse(request.userAnswers)
              userAnswersService.upsert(mode, srn, updatedUserAnswers.json).map(cacheMap =>
                Redirect(navigator.nextPage(NoCompanyNumberId(index), mode, UserAnswers(cacheMap), srn)))
            }
          )
        }
    }

}
