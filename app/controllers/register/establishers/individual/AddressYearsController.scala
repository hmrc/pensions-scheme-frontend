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
import forms.register.establishers.individual.AddressYearsFormProvider
import identifiers.register.establishers.individual.AddressYearsId
import models.{Index, Mode, NormalMode}
import play.api.libs.json._
import models.register.establishers.individual.AddressYears
import models.{Index, Mode}
import play.api.mvc.{Action, AnyContent}
import utils._
import views.html.register.establishers.individual.addressYears

import scala.concurrent.Future
import scala.util.{Failure, Success}

class AddressYearsController @Inject()(
                                        appConfig: FrontendAppConfig,
                                        override val messagesApi: MessagesApi,
                                        dataCacheConnector: DataCacheConnector,
                                        navigator: Navigator,
                                        authenticate: AuthAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: AddressYearsFormProvider
                                      ) extends FrontendController with I18nSupport with Enumerable.Implicits with MapFormats {

  private def key(index: Int): JsPath = __ \ "establishers" \ index \ AddressYearsId

  private val form = formProvider()

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>
      request.userAnswers.addressYears(index) match {
        case None =>
          Ok(addressYears(appConfig, form, mode, index))
        case Some(value) =>
          Ok(addressYears(appConfig, form.fill(value), mode, index))
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(addressYears(appConfig, formWithErrors, mode, index))),
        (value) =>
          dataCacheConnector.save[AddressYears](request.externalId, key(index), value).map(cacheMap =>
            Redirect(navigator.nextPage(AddressYearsId, mode)(new UserAnswers(cacheMap))))
      )
  }
}
