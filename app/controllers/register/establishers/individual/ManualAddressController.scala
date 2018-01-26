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

import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import connectors.DataCacheConnector
import controllers.actions._
import config.FrontendAppConfig
import forms.register.establishers.individual.ManualAddressFormProvider
import identifiers.register.establishers.individual.{AddressResultsId, ManualAddressId}
import models.addresslookup.Address
import models.{Index, Mode}
import models.register.establishers.individual.ManualAddress
import play.api.mvc.{Action, AnyContent}
import utils.{Navigator, UserAnswers}
import views.html.register.establishers.individual.manualAddress

import scala.concurrent.Future

class ManualAddressController @Inject()(appConfig: FrontendAppConfig,
                                                  override val messagesApi: MessagesApi,
                                                  dataCacheConnector: DataCacheConnector,
                                                  navigator: Navigator,
                                                  authenticate: AuthAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  formProvider: ManualAddressFormProvider) extends FrontendController with I18nSupport {

  val form: Form[Address] = formProvider()

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(AddressResultsId(index)) match {
        case None => form
        case Some(value) => form.fill(value)
      }
      Ok(manualAddress(appConfig, preparedForm, mode, index))
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(manualAddress(appConfig, formWithErrors, mode, index))),
        (value) =>
          dataCacheConnector.save(
            request.externalId,
            AddressResultsId(index),
            value
          ).map {
            json =>
              Redirect(navigator.nextPage(AddressResultsId(index), mode)(new UserAnswers(json)))
          }
      )
  }
}
