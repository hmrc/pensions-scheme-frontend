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

import javax.inject.Inject

import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import connectors.DataCacheConnector
import controllers.actions._
import config.FrontendAppConfig
import controllers.Retrievals
import forms.ContactDetailsFormProvider
import identifiers.register.trustees.company.{CompanyContactDetailsId, CompanyDetailsId}
import models.{ContactDetails, Index, Mode}
import play.api.mvc.{Action, AnyContent}
import utils.{Enumerable, MapFormats, Navigator, UserAnswers}
import views.html.register.trustees.company.companyContactDetails

import scala.concurrent.Future

class CompanyContactDetailsController @Inject() (
                                        appConfig: FrontendAppConfig,
                                        override val messagesApi: MessagesApi,
                                        dataCacheConnector: DataCacheConnector,
                                        navigator: Navigator,
                                        authenticate: AuthAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: ContactDetailsFormProvider
                                      ) extends FrontendController with Retrievals with I18nSupport with Enumerable.Implicits with MapFormats{

  val form: Form[ContactDetails] = formProvider()

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      CompanyDetailsId(index).retrieve.right.flatMap{
        companyDetails =>
          CompanyContactDetailsId(index).retrieve.right.map{ value =>
           Future.successful(Ok(companyContactDetails(appConfig, form.fill(value), mode, index, companyDetails.companyName)))
          }.left.map{ _ =>
           Future.successful(Ok(companyContactDetails(appConfig, form, mode, index, companyDetails.companyName)))
          }
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      CompanyDetailsId(index).retrieve.right.map {
            companyDetails=>
          form.bindFromRequest().fold(
            (formWithErrors: Form[_]) =>
              Future.successful(BadRequest(companyContactDetails(appConfig, formWithErrors, mode, index, companyDetails.companyName))),
            (value) =>
              dataCacheConnector.save(request.externalId, CompanyContactDetailsId(index), value).map(cacheMap =>
              Redirect(navigator.nextPage(CompanyContactDetailsId(index), mode) (new UserAnswers(cacheMap))))
          )
      }
  }

 }
