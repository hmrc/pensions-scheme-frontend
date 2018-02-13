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
import connectors.{AddressLookupConnector, DataCacheConnector}
import controllers.actions._
import config.FrontendAppConfig
import forms.register.establishers.individual.PostCodeLookupFormProvider
import identifiers.register.establishers.individual.{EstablisherDetailsId, PreviousPostCodeLookupId}
import models.addresslookup.Address
import models.requests.DataRequest
import models.{Index, Mode}
import play.api.mvc.{Action, AnyContent, Result}
import utils.annotations.EstablishersIndividual
import utils.{Enumerable, Navigator, UserAnswers}
import views.html.register.establishers.individual.previousPostCodeLookup

import scala.concurrent.Future

class PreviousAddressPostCodeLookupController @Inject()(
                                                         appConfig: FrontendAppConfig,
                                                         override val messagesApi: MessagesApi,
                                                         dataCacheConnector: DataCacheConnector,
                                                         addressLookupConnector: AddressLookupConnector,
                                                         @EstablishersIndividual navigator: Navigator,
                                                         authenticate: AuthAction,
                                                         getData: DataRetrievalAction,
                                                         requireData: DataRequiredAction,
                                                         formProvider: PostCodeLookupFormProvider
                                      ) extends FrontendController with I18nSupport with Enumerable.Implicits{

  private val form = formProvider()

  def formWithError(messageKey: String): Form[String] = {
    form.withError("value", s"messages__error__postcode_$messageKey")
  }

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveEstablisherName(index) {
        establisherName =>
          Future.successful(Ok(previousPostCodeLookup(appConfig, form, mode, index, establisherName)))
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveEstablisherName(index) {
        establisherName =>
          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(previousPostCodeLookup(appConfig, formWithErrors, mode, index, establisherName))),
            value =>
              addressLookupConnector.addressLookupByPostCode(value).flatMap {
                case None =>
                  Future.successful(BadRequest(previousPostCodeLookup(appConfig, formWithError("invalid"), mode, index, establisherName)))

                case Some(Nil) =>
                  Future.successful(BadRequest(previousPostCodeLookup(appConfig, formWithError("no_results"), mode, index, establisherName)))

                case Some(addressSeq) =>
                  dataCacheConnector.save(
                    request.externalId,
                    PreviousPostCodeLookupId(index),
                    addressSeq.map(_.address)
                  ).map {
                    json =>
                      Redirect(navigator.nextPage(PreviousPostCodeLookupId(index), mode)(new UserAnswers(json)))
                  }

              }
          )

      }
  }

  private def retrieveEstablisherName(index: Int)(block: String => Future[Result])
                                     (implicit request: DataRequest[AnyContent]): Future[Result] = {
    request.userAnswers.get(EstablisherDetailsId(index)) match {
      case Some(value) =>
        block(value.establisherName)
      case _ =>
        Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
    }
  }
}
