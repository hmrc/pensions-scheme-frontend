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
import connectors.DataCacheConnector
import controllers.Retrievals
import controllers.actions._
import forms.register.SecuredBenefitsFormProvider
import identifiers.register.{SchemeDetailsId, SecuredBenefitsId}
import javax.inject.Inject
import models.Mode
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.Register
import utils.{Navigator, UserAnswers}
import views.html.register.securedBenefits

import scala.concurrent.Future

class SecuredBenefitsController @Inject()(appConfig: FrontendAppConfig,
                                          override val messagesApi: MessagesApi,
                                          dataCacheConnector: DataCacheConnector,
                                          @Register navigator: Navigator,
                                          authenticate: AuthAction,
                                          getData: DataRetrievalAction,
                                          requireData: DataRequiredAction,
                                          formProvider: SecuredBenefitsFormProvider
                                         ) extends FrontendController with I18nSupport with Retrievals {

  private val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      SchemeDetailsId.retrieve.right.map { schemeDetails =>
        val preparedForm = request.userAnswers.get(SecuredBenefitsId) match {
          case None => form
          case Some(value) => form.fill(value)
        }
        Future.successful(Ok(securedBenefits(appConfig, preparedForm, mode, schemeDetails.schemeName)))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          SchemeDetailsId.retrieve.right.map { schemeDetails =>
            Future.successful(BadRequest(securedBenefits(appConfig, formWithErrors, mode, schemeDetails.schemeName)))
          },
        value =>
          dataCacheConnector.save(request.externalId, SecuredBenefitsId, value).map(cacheMap =>
            Redirect(navigator.nextPage(SecuredBenefitsId, mode, UserAnswers(cacheMap))))
      )
  }
}
