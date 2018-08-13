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

package controllers.register.establishers.company

import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.Retrievals
import controllers.actions._
import forms.CompanyRegistrationNumberFormProvider
import identifiers.register.establishers.company.CompanyRegistrationNumberId
import javax.inject.Inject
import models.{Index, Mode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils._
import utils.annotations.EstablishersCompany
import views.html.register.establishers.company.companyRegistrationNumber

import scala.concurrent.Future

class CompanyRegistrationNumberController @Inject()(
                                                     appConfig: FrontendAppConfig,
                                                     override val messagesApi: MessagesApi,
                                                     dataCacheConnector: DataCacheConnector,
                                                     @EstablishersCompany navigator: Navigator,
                                                     authenticate: AuthAction,
                                                     getData: DataRetrievalAction,
                                                     requireData: DataRequiredAction,
                                                     formProvider: CompanyRegistrationNumberFormProvider
                                                   ) extends FrontendController with Retrievals with I18nSupport with Enumerable.Implicits with MapFormats {

  val form = formProvider()

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveCompanyName(index) {
        companyName =>
          val redirectResult = request.userAnswers.get(CompanyRegistrationNumberId(index)) match {
            case None =>
              Ok(companyRegistrationNumber(appConfig, form, mode, index, companyName))
            case Some(value) =>
              Ok(companyRegistrationNumber(appConfig, form.fill(value), mode, index, companyName))
          }

          Future.successful(redirectResult)

      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveCompanyName(index) {
        companyName =>
          form.bindFromRequest().fold(
            (formWithErrors: Form[_]) =>
              Future.successful(BadRequest(companyRegistrationNumber(appConfig, formWithErrors, mode, index, companyName))),
            value =>
              dataCacheConnector.save(request.externalId, CompanyRegistrationNumberId(index), value).map(cacheMap =>
                Redirect(navigator.nextPage(CompanyRegistrationNumberId(index), mode, UserAnswers(cacheMap))))
          )
      }
  }

}
