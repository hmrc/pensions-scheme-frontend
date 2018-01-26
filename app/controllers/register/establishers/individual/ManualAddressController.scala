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
import identifiers.register.establishers.individual.{AddressResultsId, EstablisherDetailsId}
import models.addresslookup.Address
import models.register.CountryOptions
import models.requests.DataRequest
import models.{Index, Mode}
import play.api.mvc.{Action, AnyContent, Result}
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
                                                  formProvider: ManualAddressFormProvider,
                                                  countryOptions: CountryOptions) extends FrontendController with I18nSupport {

  val form: Form[Address] = formProvider()

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveEstablisherName(index) {
        establisherName =>
          val result = request.userAnswers.get(AddressResultsId(index)) match {
            case None => Ok(manualAddress(appConfig, form, mode, index, countryOptions.options, establisherName))
            case Some(value) => Ok(manualAddress(appConfig, form.fill(value), mode, index, countryOptions.options, establisherName))
          }
          Future.successful(result)
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveEstablisherName(index) {
        establisherName =>
          form.bindFromRequest().fold(
            (formWithErrors: Form[_]) =>
              Future.successful(BadRequest(manualAddress(appConfig, formWithErrors, mode, index, countryOptions.options, establisherName))),
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

  private def retrieveEstablisherName(index:Int)(block: String => Future[Result])
                                     (implicit request: DataRequest[AnyContent]): Future[Result] = {
    request.userAnswers.get(EstablisherDetailsId(index)) match {
      case Some(value) =>
        block(value.establisherName)
      case _ =>
        Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
    }
  }
}
