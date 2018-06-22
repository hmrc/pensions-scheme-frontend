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

package controllers.register.trustees.individual

import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.Retrievals
import controllers.actions._
import forms.register.trustees.individual.UniqueTaxReferenceFormProvider
import identifiers.register.trustees.individual.{TrusteeDetailsId, UniqueTaxReferenceId}
import javax.inject.Inject
import models.{Index, Mode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.TrusteesIndividual
import utils.{Enumerable, Navigator, UserAnswers}
import views.html.register.trustees.individual.uniqueTaxReference

import scala.concurrent.Future

class UniqueTaxReferenceController @Inject()(
                                              appConfig: FrontendAppConfig,
                                              override val messagesApi: MessagesApi,
                                              dataCacheConnector: DataCacheConnector,
                                              @TrusteesIndividual navigator: Navigator,
                                              authenticate: AuthAction,
                                              getData: DataRetrievalAction,
                                              requireData: DataRequiredAction,
                                              formProvider: UniqueTaxReferenceFormProvider
                                     ) extends FrontendController with Retrievals with I18nSupport with Enumerable.Implicits {

  private val form = formProvider()

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      TrusteeDetailsId(index).retrieve.right.flatMap { details =>
          UniqueTaxReferenceId(index).retrieve.right.map { value =>
            Future.successful(Ok(uniqueTaxReference(appConfig, form.fill(value), mode, index, details.fullName)))
          }.left.map { _ =>
            Future.successful(Ok(uniqueTaxReference(appConfig, form, mode, index, details.fullName)))
          }
        }
  }

  def onSubmit(mode: Mode, index: Index) = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      TrusteeDetailsId(index).retrieve.right.map { details =>
        form.bindFromRequest().fold(
          (formWithErrors: Form[_]) =>
            Future.successful(BadRequest(uniqueTaxReference(appConfig, formWithErrors, mode, index, details.fullName))),
          (value) =>
            dataCacheConnector.save(request.externalId, UniqueTaxReferenceId(index), value).map(cacheMap =>
              Redirect(navigator.nextPage(UniqueTaxReferenceId(index), mode, new UserAnswers(cacheMap))))
        )
      }
  }
}
