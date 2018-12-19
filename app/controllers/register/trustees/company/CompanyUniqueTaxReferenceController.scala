/*
 * Copyright 2018 HM Revenue & Customs
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
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.TrusteesCompany
import utils.{Enumerable, Navigator, UserAnswers}
import views.html.register.trustees.company.companyUniqueTaxReference

import scala.concurrent.{ExecutionContext, Future}

class CompanyUniqueTaxReferenceController @Inject()(
                                                     appConfig: FrontendAppConfig,
                                                     override val messagesApi: MessagesApi,
                                                     dataCacheConnector: UserAnswersCacheConnector,
                                                     @TrusteesCompany navigator: Navigator,
                                                     authenticate: AuthAction,
                                                     getData: DataRetrievalAction,
                                                     requireData: DataRequiredAction,
                                                     formProvider: CompanyUniqueTaxReferenceFormProvider
                                                   ) (implicit val ec: ExecutionContext) extends FrontendController with Retrievals with I18nSupport with Enumerable.Implicits {

  private val form = formProvider()

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      CompanyDetailsId(index).retrieve.right.flatMap { companyDetails =>
        CompanyUniqueTaxReferenceId(index).retrieve.right.map { value =>
          Future.successful(Ok(companyUniqueTaxReference(appConfig, form.fill(value), mode, index)))
        }.left.map { _ =>
          Future.successful(Ok(companyUniqueTaxReference(appConfig, form, mode, index)))
        }
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      CompanyDetailsId(index).retrieve.right.map {
        companyDetails =>
          form.bindFromRequest().fold(
            (formWithErrors: Form[_]) =>
              Future.successful(BadRequest(companyUniqueTaxReference(appConfig, formWithErrors, mode, index))),
            (value) =>
              dataCacheConnector.save(
                request.externalId,
                CompanyUniqueTaxReferenceId(index),
                value
              ).map {
                json =>
                  Redirect(navigator.nextPage(CompanyUniqueTaxReferenceId(index), mode, UserAnswers(json)))
              }
          )
      }
  }
}
