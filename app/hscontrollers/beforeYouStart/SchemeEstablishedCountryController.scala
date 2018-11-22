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

package hscontrollers.beforeYouStart

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions._
import forms.register.SchemeEstablishedCountryFormProvider
import identifiers.register.{SchemeDetailsId, SchemeEstablishedCountryId}
import javax.inject.Inject
import models.Mode
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.BeforeYouStart
import utils.{CountryOptions, Navigator, UserAnswers}
import views.html.hs.beforeYouStart.schemeEstablishedCountry

import scala.concurrent.Future

class SchemeEstablishedCountryController @Inject()(appConfig: FrontendAppConfig,
                                                   override val messagesApi: MessagesApi,
                                                   dataCacheConnector: UserAnswersCacheConnector,
                                                   @BeforeYouStart navigator: Navigator,
                                                   authenticate: AuthAction,
                                                   getData: DataRetrievalAction,
                                                   requireData: DataRequiredAction,
                                                   formProvider: SchemeEstablishedCountryFormProvider,
                                                   countryOptions: CountryOptions) extends FrontendController with I18nSupport with Retrievals {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      SchemeDetailsId.retrieve.right.map { schemeDetails =>
        val preparedForm = request.userAnswers.get(SchemeEstablishedCountryId) match {
          case None => form
          case Some(value) => form.fill(value)
        }
        Future.successful(Ok(schemeEstablishedCountry(appConfig, preparedForm, mode, schemeDetails.schemeName, countryOptions.options)))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          SchemeDetailsId.retrieve.right.map { schemeDetails =>
            Future.successful(BadRequest(schemeEstablishedCountry(appConfig, formWithErrors, mode, schemeDetails.schemeName, countryOptions.options)))
          },
        value =>
          dataCacheConnector.save(request.externalId, SchemeEstablishedCountryId, value).map(cacheMap =>
            Redirect(navigator.nextPage(SchemeEstablishedCountryId, mode, UserAnswers(cacheMap)))
          )
      )
  }
}
