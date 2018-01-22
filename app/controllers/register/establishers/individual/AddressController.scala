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

import play.api.data.{Form, FormError}
import play.api.i18n.{I18nSupport, MessagesApi}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import connectors.{AddressLookupConnector, DataCacheConnector}
import controllers.actions._
import config.FrontendAppConfig
import forms.register.establishers.individual.AddressFormProvider
import identifiers.register.establishers.individual.AddressId
import models.addresslookup.AddressRecord
import models.Mode
import play.api.mvc.{Action, AnyContent}
import utils.{Enumerable, MapFormats, Navigator, UserAnswers}
import views.html.register.establishers.individual.address
import scala.concurrent.Future

class AddressController @Inject()(
                                   appConfig: FrontendAppConfig,
                                   override val messagesApi: MessagesApi,
                                   dataCacheConnector: DataCacheConnector,
                                   addressLookupConnector: AddressLookupConnector,
                                   navigator: Navigator,
                                   authenticate: AuthAction,
                                   getData: DataRetrievalAction,
                                   requireData: DataRequiredAction,
                                   formProvider: AddressFormProvider) extends FrontendController with I18nSupport
  with Enumerable.Implicits with MapFormats {

  val form = formProvider()

  def formWithError(messageKey: String): Form[String] = {
    form.withError("value", messageKey)
  }

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      Future.successful(Ok(address(appConfig, form, mode)))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(address(appConfig, formWithErrors, mode))),
        (value) =>
          addressLookupConnector.addressLookupByPostCode(value).flatMap {
            case None =>
              Future.successful(BadRequest(address(appConfig, formWithError("messages__error__postcode_invalid"), mode)))

            case Some(Nil) =>
              Future.successful(BadRequest(address(appConfig, formWithError("messages__error__postcode_no_results"), mode)))

            case Some(addressList) =>
              dataCacheConnector.save[List[AddressRecord]](request.externalId, AddressId.toString, addressList).map {
                cacheMap =>
                  Redirect(navigator.nextPage(AddressId, mode)(new UserAnswers(cacheMap)))
              }
          }
      )
  }
}
