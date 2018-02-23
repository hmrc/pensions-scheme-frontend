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

package controllers.register

import javax.inject.Inject

import config.FrontendAppConfig
import connectors.{AddressLookupConnector, DataCacheConnector}
import controllers.FrontendBaseController
import controllers.actions._
import forms.register.establishers.individual.PostCodeLookupFormProvider
import identifiers.register.InsurerPostCodeLookupId
import models.Mode
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import utils.{Enumerable, Navigator, UserAnswers}
import views.html.register.insurerPostCodeLookup

import scala.concurrent.Future

class InsurerPostCodeLookupController @Inject()(
                                          appConfig: FrontendAppConfig,
                                          override val messagesApi: MessagesApi,
                                          dataCacheConnector: DataCacheConnector,
                                          addressLookupConnector: AddressLookupConnector,
                                          navigator: Navigator,
                                          authenticate: AuthAction,
                                          getData: DataRetrievalAction,
                                          requireData: DataRequiredAction,
                                          formProvider: PostCodeLookupFormProvider
                                        ) extends FrontendBaseController with I18nSupport with Enumerable.Implicits {

  private val form = formProvider()

  def formWithError(messageKey: String): Form[String] = {
    form.withError("value", s"messages__error__postcode_$messageKey")
  }

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveSchemeName {
        schemeName =>
          Future.successful(Ok(insurerPostCodeLookup(appConfig, form, mode, schemeName)))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveSchemeName {
        schemeName =>
          form.bindFromRequest().fold(
            (formWithErrors: Form[_]) =>
              Future.successful(BadRequest(insurerPostCodeLookup(appConfig, formWithErrors, mode, schemeName))),
            (value) =>
              addressLookupConnector.addressLookupByPostCode(value).flatMap {
                case None =>
                  Future.successful(BadRequest(insurerPostCodeLookup(appConfig, formWithError("invalid"), mode, schemeName)))

                case Some(Nil) =>
                  Future.successful(BadRequest(insurerPostCodeLookup(appConfig, formWithError("no_results"), mode, schemeName)))

                case Some(addressSeq) =>
                  dataCacheConnector.save(
                    request.externalId,
                    InsurerPostCodeLookupId,
                    addressSeq.map(_.address)
                  ).map {
                    json =>
                      Redirect(navigator.nextPage(InsurerPostCodeLookupId, mode)(new UserAnswers(json)))
                  }
              }
          )
      }
  }

}
