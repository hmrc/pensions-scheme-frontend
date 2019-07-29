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
import controllers.actions._
import forms.ContactDetailsFormProvider
import identifiers.register.establishers.company.CompanyContactDetailsId
import javax.inject.Inject
import models.{Index, Mode}
import navigators.Navigator
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import utils._
import utils.annotations.EstablishersCompany
import viewmodels.{ContactDetailsViewModel, Message}

import scala.concurrent.ExecutionContext

class CompanyContactDetailsController @Inject()(
                                                 @EstablishersCompany override val navigator: Navigator,
                                                 override val appConfig: FrontendAppConfig,
                                                 override val messagesApi: MessagesApi,
                                                 val userAnswersService: UserAnswersService,
                                                 authenticate: AuthAction,
                                                 getData: DataRetrievalAction,
                                                 allowAccess: AllowAccessActionProvider,
                                                 requireData: DataRequiredAction,
                                                 formProvider: ContactDetailsFormProvider
                                               )(implicit val ec: ExecutionContext) extends controllers.ContactDetailsController {

  private val form = formProvider()

  def onPageLoad(mode: Mode, srn: Option[String], index: Index): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        retrieveCompanyName(index) {
          companyName =>
            get(CompanyContactDetailsId(index), form, viewmodel(mode, srn, index, companyName))
        }
    }

  def onSubmit(mode: Mode, srn: Option[String], index: Index): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      retrieveCompanyName(index) {
        companyName =>
          post(CompanyContactDetailsId(index), mode, form, viewmodel(mode, srn, index, companyName))
      }
  }

  private def viewmodel(mode: Mode, srn: Option[String], index: Index, companyName: String) = ContactDetailsViewModel(
    postCall = routes.CompanyContactDetailsController.onSubmit(mode, srn, index),
    title = Message("messages__establisher_company_contact_details__title"),
    heading = Message("messages__establisher_company_contact_details__heading"),
    body = Message("messages__contact_details__body"),
    subHeading = Some(companyName),
    srn = srn
  )
}
