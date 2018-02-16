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
import controllers.actions._
import forms.register.establishers.individual.AddressListFormProvider
import identifiers.register.establishers.company.{CompanyAddressListId, CompanyDetailsId, CompanyPostCodeLookupId}
import identifiers.register.establishers.individual.AddressId
import models.addresslookup.Address
import models.requests.DataRequest
import models.{Index, Mode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{Enumerable, Navigator, UserAnswers}
import views.html.register.establishers.company.companyAddressList

import scala.concurrent.Future

class CompanyAddressListController @Inject() (
                                               appConfig: FrontendAppConfig,
                                               override val messagesApi: MessagesApi,
                                               dataCacheConnector: DataCacheConnector,
                                               navigator: Navigator,
                                               authenticate: AuthAction,
                                               getData: DataRetrievalAction,
                                               requireData: DataRequiredAction,
                                               formProvider: AddressListFormProvider
                                     ) extends FrontendController with I18nSupport with Enumerable.Implicits {

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieve(mode, index) {
        case (name, addresses) =>
          Future.successful(Ok(
            companyAddressList(appConfig, formProvider(addresses), mode, index, addresses, name)
          ))
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieve(mode, index) {
        case (name, addresses) =>
          formProvider(addresses).bindFromRequest().fold(
            form =>
              Future.successful(BadRequest(
                companyAddressList(appConfig, form, mode, index, addresses, name)
              )),
            id =>
              // TODO point this at the correct identifier
              dataCacheConnector.save(request.externalId, AddressId(index), addresses(id).copy(country = "GB")).map {
                json =>
                  Redirect(navigator.nextPage(CompanyAddressListId(index), mode)(UserAnswers(json)))
              }
          )
      }
  }

  private def retrieve(mode: Mode, index: Int)(f: (String, Seq[Address]) => Future[Result])
                      (implicit request: DataRequest[AnyContent]): Future[Result] = {

    request.userAnswers.get(CompanyDetailsId(index)).map {
      name =>
        request.userAnswers.get(CompanyPostCodeLookupId(index)).map {
          addresses =>
            f(name.companyName, addresses)
        }.getOrElse {
          Future.successful(Redirect(controllers.register.establishers.company.routes.CompanyPostCodeLookupController.onPageLoad(mode, index)))
        }
    }.getOrElse {
      Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
    }
  }
}
