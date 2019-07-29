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
import forms.register.trustees.company.CompanyUniqueTaxReferenceFormProvider
import identifiers.register.trustees.company._
import javax.inject.Inject
import models.{Index, Mode}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.TrusteesCompany
import utils.{Enumerable, UserAnswers}
import views.html.register.trustees.company.companyUniqueTaxReference

import scala.concurrent.{ExecutionContext, Future}

class CompanyUniqueTaxReferenceController @Inject()(
                                                     appConfig: FrontendAppConfig,
                                                     override val messagesApi: MessagesApi,
                                                     userAnswersService: UserAnswersService,
                                                     @TrusteesCompany navigator: Navigator,
                                                     authenticate: AuthAction,
                                                     getData: DataRetrievalAction,
                                                     allowAccess: AllowAccessActionProvider,
                                                     requireData: DataRequiredAction,
                                                     formProvider: CompanyUniqueTaxReferenceFormProvider
                                                   )(implicit val ec: ExecutionContext) extends FrontendController with Retrievals with I18nSupport with Enumerable.Implicits {

  private val form = formProvider()

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
    implicit request =>
      CompanyDetailsId(index).retrieve.right.map { companyDetails =>
        val submitUrl = controllers.register.trustees.company.routes.CompanyUniqueTaxReferenceController.onSubmit(mode, index, srn)
        val updatedForm = request.userAnswers.get(CompanyUniqueTaxReferenceId(index)).fold(form)(form.fill)
        Future.successful(Ok(companyUniqueTaxReference(appConfig, updatedForm, mode, index, existingSchemeName, submitUrl, srn)))
      }
  }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      CompanyDetailsId(index).retrieve.right.map {
        companyDetails =>
          form.bindFromRequest().fold(
            (formWithErrors: Form[_]) => {
              val submitUrl = controllers.register.trustees.company.routes.CompanyUniqueTaxReferenceController.onSubmit(mode, index, srn)
              Future.successful(BadRequest(companyUniqueTaxReference(appConfig, formWithErrors, mode, index, existingSchemeName, submitUrl, srn)))
            },
            value =>
              userAnswersService.save(
                mode,
                srn,
                CompanyUniqueTaxReferenceId(index),
                value
              ).map {
                json =>
                  Redirect(navigator.nextPage(CompanyUniqueTaxReferenceId(index), mode, UserAnswers(json), srn))
              }
          )
      }
  }
}
