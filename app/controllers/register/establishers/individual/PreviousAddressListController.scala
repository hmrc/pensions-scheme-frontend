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

package controllers.register.establishers.individual

import javax.inject.Inject

import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.Retrievals
import controllers.actions._
import forms.address.AddressListFormProvider
import identifiers.register.establishers.individual._
import models.{Index, Mode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.EstablishersIndividual
import utils.{Enumerable, MapFormats, Navigator, UserAnswers}
import views.html.register.establishers.individual.previousAddressList

import scala.concurrent.Future

class PreviousAddressListController @Inject()(
                                               appConfig: FrontendAppConfig,
                                               override val messagesApi: MessagesApi,
                                               dataCacheConnector: DataCacheConnector,
                                               @EstablishersIndividual navigator: Navigator,
                                               authenticate: AuthAction,
                                               getData: DataRetrievalAction,
                                               requireData: DataRequiredAction,
                                               formProvider: AddressListFormProvider
                                     ) extends FrontendController with Retrievals with I18nSupport with Enumerable.Implicits with MapFormats{


  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveEstablisherName(index) {
        establisherName =>
          val result = request.userAnswers.get(PreviousPostCodeLookupId(index)) match {
            case None =>
              Redirect(controllers.register.establishers.individual.routes.PreviousAddressPostCodeLookupController.onPageLoad(mode, index))
            case Some(previousAddresses) =>
              Ok(previousAddressList(appConfig, formProvider(previousAddresses), mode, index, previousAddresses, establisherName))
          }
          Future.successful(result)
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveEstablisherName(index) {
        establisherName =>
          request.userAnswers.get(PreviousPostCodeLookupId(index)) match {
            case None =>
              Future.successful(Redirect(controllers.register.establishers.individual.routes.PreviousAddressPostCodeLookupController.onPageLoad(mode, index)))

            case Some(previousAddresses) =>
              formProvider(previousAddresses).bindFromRequest().fold(
                formWithErrors =>
                  Future.successful(BadRequest(previousAddressList(appConfig, formWithErrors, mode, index, previousAddresses, establisherName))),
                id =>
                  dataCacheConnector.save(
                    request.externalId,
                    PreviousAddressId(index),
                    previousAddresses(id).copy(country = "GB")
                  ).map {
                    json =>
                      Redirect(navigator.nextPage(PreviousAddressListId(index), mode)(new UserAnswers(json)))
                  }
              )
          }
      }
  }

}
