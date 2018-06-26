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
import connectors.DataCacheConnector
import controllers.actions._
import forms.ContactDetailsFormProvider
import identifiers.register.trustees.company.{CompanyContactDetailsId, CompanyDetailsId}
import javax.inject.Inject
import models.{Index, Mode}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils._
import utils.annotations.TrusteesCompany
import viewmodels.{ContactDetailsViewModel, Message}

class CompanyContactDetailsController @Inject()(
                                                 @TrusteesCompany override val navigator: Navigator,
                                                 override val appConfig: FrontendAppConfig,
                                                 override val messagesApi: MessagesApi,
                                                 override val cacheConnector: DataCacheConnector,
                                                 authenticate: AuthAction,
                                                 getData: DataRetrievalAction,
                                                 requireData: DataRequiredAction,
                                                 formProvider: ContactDetailsFormProvider
                                               ) extends controllers.ContactDetailsController {

  private val form = formProvider()

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      CompanyDetailsId(index).retrieve.right.map {
        companyDetails =>
          get(CompanyContactDetailsId(index), form, viewmodel(mode, index, companyDetails.companyName))
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      CompanyDetailsId(index).retrieve.right.map {
        companyDetails =>
          post(CompanyContactDetailsId(index), mode, form, viewmodel(mode, index, companyDetails.companyName))
      }
  }

  private def viewmodel(mode: Mode, index: Index, companyName: String) = ContactDetailsViewModel(
    postCall = routes.CompanyContactDetailsController.onSubmit(mode, index),
    title = Message("messages__trustee_company_contact_details__title"),
    heading = Message("messages__trustee_company_contact_details__heading"),
    body = Message("messages__contact_details__body"),
    subHeading = Some(companyName)
  )
}
