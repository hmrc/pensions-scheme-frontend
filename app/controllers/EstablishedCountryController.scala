/*
 * Copyright 2022 HM Revenue & Customs
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
import forms.EstablishedCountryFormProvider
import identifiers.{EstablishedCountryId, SchemeNameId}
import javax.inject.Inject
import models.Mode
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.BeforeYouStart
import utils.{CountryOptions, UserAnswers}
import views.html.establishedCountry

import scala.concurrent.{ExecutionContext, Future}

class EstablishedCountryController @Inject()(appConfig: FrontendAppConfig,
                                             override val messagesApi: MessagesApi,
                                             dataCacheConnector: UserAnswersCacheConnector,
                                             @BeforeYouStart navigator: Navigator,
                                             authenticate: AuthAction,
                                             getData: DataRetrievalAction,
                                             requireData: DataRequiredAction,
                                             formProvider: EstablishedCountryFormProvider,
                                             countryOptions: CountryOptions,
                                             val controllerComponents: MessagesControllerComponents,
                                             val view: establishedCountry
                                            )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController with I18nSupport with Retrievals {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate() andThen getData() andThen requireData).async {
    implicit request =>
      SchemeNameId.retrieve.right.map { schemeName =>
        val preparedForm = request.userAnswers.get(EstablishedCountryId) match {
          case None => form
          case Some(value) => form.fill(value)
        }
        Future.successful(Ok(view(preparedForm, mode, countryOptions.options, schemeName)))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate() andThen getData() andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          SchemeNameId.retrieve.right.map { schemeName =>
            Future.successful(BadRequest(view(formWithErrors, mode, countryOptions.options, schemeName)))
          },
        value =>
          dataCacheConnector.save(request.externalId, EstablishedCountryId, value).map(cacheMap =>
            Redirect(navigator.nextPage(EstablishedCountryId, mode, UserAnswers(cacheMap)))
          )
      )
  }
}
