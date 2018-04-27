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
import connectors.{DataCacheConnector, PSANameCacheConnector}
import controllers.actions._
import forms.register.SchemeDetailsFormProvider
import identifiers.register.SchemeDetailsId
import models.Mode
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.Register
import utils.{NameMatchingFactory, Navigator, UserAnswers}
import views.html.register.schemeDetails

import scala.concurrent.Future

class SchemeDetailsController @Inject()(appConfig: FrontendAppConfig,
                                        override val messagesApi: MessagesApi,
                                        dataCacheConnector: DataCacheConnector,
                                        @Register navigator: Navigator,
                                        authenticate: AuthAction,
                                        getData: DataRetrievalAction,
                                        formProvider: SchemeDetailsFormProvider,
                                        nameMatchingFactory: NameMatchingFactory) extends FrontendController with I18nSupport {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData) {
    implicit request =>
      val preparedForm = request.userAnswers.flatMap(_.get(SchemeDetailsId)) match {
        case None => form
        case Some(value) => form.fill(value)
      }
      Ok(schemeDetails(appConfig, preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(schemeDetails(appConfig, formWithErrors, mode))),
        (value) =>
          nameMatchingFactory.nameMatching(value.schemeName) flatMap { x =>
            dataCacheConnector.save(request.externalId, SchemeDetailsId, value).map(cacheMap =>
              Redirect(navigator.nextPage(SchemeDetailsId, mode)(UserAnswers(cacheMap)))
            )
          }
      )
  }
}
