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

package controllers.register.trustees.company

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions._
import forms.CompanyRegistrationNumberFormProvider
import identifiers.register.trustees.company.{CompanyDetailsId, CompanyRegistrationNumberId}
import javax.inject.Inject
import models.{Index, Mode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.TrusteesCompany
import utils.{Navigator, UserAnswers}
import views.html.register.trustees.company.companyRegistrationNumber

import scala.concurrent.{ExecutionContext, Future}

class CompanyRegistrationNumberController @Inject()(
                                                     appConfig: FrontendAppConfig,
                                                     override val messagesApi: MessagesApi,
                                                     userAnswersService: UserAnswersService,
                                                     @TrusteesCompany navigator: Navigator,
                                                     authenticate: AuthAction,
                                                     getData: DataRetrievalAction,
                                                     allowAccess: AllowAccessActionProvider,
                                                     requireData: DataRequiredAction,
                                                     formProvider: CompanyRegistrationNumberFormProvider
                                                   ) (implicit val ec: ExecutionContext) extends FrontendController with I18nSupport with Retrievals {

  private val form = formProvider()

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
    implicit request =>
      CompanyDetailsId(index).retrieve.right.map { _ =>
        val submitUrl = controllers.register.trustees.company.routes.CompanyRegistrationNumberController.onSubmit(mode, index, srn)
        val updatedForm = request.userAnswers.get(CompanyRegistrationNumberId(index)).fold(form)(form.fill)
        Future.successful(Ok(companyRegistrationNumber(appConfig, updatedForm, mode, index, existingSchemeName, submitUrl)))
      }
  }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      CompanyDetailsId(index).retrieve.right.map { _ =>
        form.bindFromRequest().fold(
          (formWithErrors: Form[_]) => {
            val submitUrl = controllers.register.trustees.company.routes.CompanyRegistrationNumberController.onSubmit(mode, index, srn)
            Future.successful(BadRequest(companyRegistrationNumber(appConfig, formWithErrors, mode, index, existingSchemeName, submitUrl)))
          },
          (value) =>
            userAnswersService.save(mode, srn, CompanyRegistrationNumberId(index), value).map(cacheMap =>
              Redirect(navigator.nextPage(CompanyRegistrationNumberId(index), mode, UserAnswers(cacheMap), srn)))
        )
      }
  }
}
