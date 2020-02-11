/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.actions._
import forms.OccupationalPensionSchemeFormProvider
import identifiers.OccupationalPensionSchemeId
import javax.inject.Inject
import models.Mode
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.UserAnswers
import utils.annotations.AboutBenefitsAndInsurance
import views.html.occupationalPensionScheme

import scala.concurrent.{ExecutionContext, Future}

class OccupationalPensionSchemeController @Inject()(appConfig: FrontendAppConfig,
                                                    override val messagesApi: MessagesApi,
                                                    dataCacheConnector: UserAnswersCacheConnector,
                                                    @AboutBenefitsAndInsurance navigator: Navigator,
                                                    authenticate: AuthAction,
                                                    getData: DataRetrievalAction,
                                                    requireData: DataRequiredAction,
                                                    formProvider: OccupationalPensionSchemeFormProvider,
                                                     val controllerComponents: MessagesControllerComponents,
                                                     val view: occupationalPensionScheme
                                                    )(implicit val executionContext: ExecutionContext) extends FrontendBaseController with I18nSupport with Retrievals {

  private val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData() andThen requireData).async {
    implicit request =>
      val preparedForm = request.userAnswers.get(OccupationalPensionSchemeId) match {
        case None => form
        case Some(value) => form.fill(value)
      }
      Future.successful(Ok(view(preparedForm, mode, existingSchemeName)))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData() andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, mode, existingSchemeName))),
        value =>
          dataCacheConnector.save(request.externalId, OccupationalPensionSchemeId, value).map(cacheMap =>
            Redirect(navigator.nextPage(OccupationalPensionSchemeId, mode, UserAnswers(cacheMap))))
      )
  }
}
