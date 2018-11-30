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

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions._
import forms.register.OccupationalPensionSchemeFormProvider
import identifiers.register.{OccupationalPensionSchemeId, SchemeDetailsId}
import javax.inject.Inject
import models.Mode
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.Register
import utils.{Navigator, UserAnswers}
import views.html.register.occupationalPensionScheme

import scala.concurrent.Future

class OccupationalPensionSchemeController @Inject()(appConfig: FrontendAppConfig,
                                                    override val messagesApi: MessagesApi,
                                                    dataCacheConnector: UserAnswersCacheConnector,
                                                    @Register navigator: Navigator,
                                                    authenticate: AuthAction,
                                                    getData: DataRetrievalAction,
                                                    requireData: DataRequiredAction,
                                                    formProvider: OccupationalPensionSchemeFormProvider
                                                   ) extends FrontendController with I18nSupport with Retrievals {

  private val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      SchemeDetailsId.retrieve.right.map { schemeDetails =>
        val preparedForm = request.userAnswers.get(OccupationalPensionSchemeId) match {
          case None => form
          case Some(value) => form.fill(value)
        }
        Future.successful(Ok(occupationalPensionScheme(appConfig, preparedForm, mode)))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          SchemeDetailsId.retrieve.right.map { schemeDetails =>
            Future.successful(BadRequest(occupationalPensionScheme(appConfig, formWithErrors, mode)))
          },
        value =>
          dataCacheConnector.save(request.externalId, OccupationalPensionSchemeId, value).map(cacheMap =>
            Redirect(navigator.nextPage(OccupationalPensionSchemeId, mode, UserAnswers(cacheMap))))
      )
  }
}
