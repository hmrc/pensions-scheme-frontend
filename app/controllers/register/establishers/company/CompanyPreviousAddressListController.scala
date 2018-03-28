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

import javax.inject.Inject

import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.Retrievals
import controllers.actions._
import forms.address.AddressListFormProvider
import identifiers.register.establishers.company.{CompanyPreviousAddressId, CompanyPreviousAddressListId, CompanyPreviousAddressPostcodeLookupId}
import models.{Index, Mode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.EstablishersCompany
import utils.{Enumerable, Navigator, UserAnswers}
import views.html.register.establishers.company.companyPreviousAddressList

import scala.concurrent.Future

class CompanyPreviousAddressListController @Inject()(
                                       appConfig: FrontendAppConfig,
                                       override val messagesApi: MessagesApi,
                                       dataCacheConnector: DataCacheConnector,
                                       @EstablishersCompany navigator: Navigator,
                                       authenticate: AuthAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       formProvider: AddressListFormProvider
                                     ) extends FrontendController with Retrievals with I18nSupport with Enumerable.Implicits {



  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveCompanyName(index) {
        companyName =>
          val redirectResult = request.userAnswers.get(CompanyPreviousAddressPostcodeLookupId(index)) match {
            case Some(addresses) => Ok(companyPreviousAddressList(appConfig, formProvider(addresses), mode, index, companyName, addresses))
            case _ => Redirect(controllers.register.establishers.company.routes.CompanyPreviousAddressPostcodeLookupController.onPageLoad(mode, index))
          }
          Future.successful(redirectResult)
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveCompanyName(index) { companyName =>
        request.userAnswers.get(CompanyPreviousAddressPostcodeLookupId(index)) match {
          case None =>
            Future.successful(Redirect(controllers.register.establishers.company.routes.CompanyPreviousAddressPostcodeLookupController.onPageLoad(mode, index)))
          case Some(addresses) =>
            formProvider(addresses).bindFromRequest().fold(
              (formWithErrors: Form[_]) =>
                Future.successful(BadRequest(companyPreviousAddressList(appConfig, formWithErrors, mode, index, companyName, addresses))),
              (value) =>
                dataCacheConnector.save(request.externalId, CompanyPreviousAddressId(index), addresses(value).copy(country = "GB")).map(cacheMap =>
                  Redirect(navigator.nextPage(CompanyPreviousAddressListId(index), mode)(new UserAnswers(cacheMap))))
            )
        }
      }
  }
}
