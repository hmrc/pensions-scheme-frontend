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
import forms.register.establishers.individual.AddressResultsFormProvider
import identifiers.register.establishers.individual.AddressResultsId
import models.addresslookup.{Address, AddressRecord}
import models.requests.DataRequest
import utils.{Enumerable, MapFormats, Navigator, UserAnswers}
import views.html.register.establishers.individual.addressResults
import models.{Index, Mode}
import play.api.mvc.{Action, AnyContent, Result}

import scala.concurrent.Future
import scala.util.Success

class AddressResultsController @Inject()(
                                        appConfig: FrontendAppConfig,
                                        override val messagesApi: MessagesApi,
                                        dataCacheConnector: DataCacheConnector,
                                        navigator: Navigator,
                                        authenticate: AuthAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: AddressResultsFormProvider) extends FrontendController with I18nSupport
  with Enumerable.Implicits with MapFormats{

  val form = formProvider()

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveEstablisherName(index) {
        establisherName =>
          val result = request.userAnswers.address match {
            case None => Redirect(controllers.register.establishers.individual.routes.AddressController.onPageLoad(mode, index))
            case Some(value) => Ok(addressResults(appConfig, form, mode, index, value, establisherName))
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
              Future.successful(BadRequest(addressResults(appConfig, formWithErrors, mode, index,
                request.userAnswers.address.getOrElse(Seq.empty), establisherName))),
            (value) =>
              dataCacheConnector.save[Address](request.externalId, AddressResultsId.toString, value).map(cacheMap =>
                Redirect(navigator.nextPage(AddressResultsId, mode)(new UserAnswers(cacheMap))))
          )
      }
  }

  private def retrieveEstablisherName(index: Int)(block: String => Future[Result])
                                     (implicit request: DataRequest[AnyContent]): Future[Result] = {
    request.userAnswers.establisherDetails(index) match {
      case Success(Some(value)) => block(value.establisherName)
      case _ => Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
    }
  }
}
