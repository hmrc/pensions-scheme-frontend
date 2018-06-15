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
import forms.ContactDetailsFormProvider
import identifiers.register.trustees.individual.{TrusteeContactDetailsId, TrusteeDetailsId}
import javax.inject.Inject
import models.{Index, Mode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.TrusteesIndividual
import utils.{Navigator2, UserAnswers}
import views.html.register.trustees.individual.trusteeContactDetails

import scala.concurrent.Future

class TrusteeContactDetailsController @Inject() (
                                                  appConfig: FrontendAppConfig,
                                                  override val messagesApi: MessagesApi,
                                                  dataCacheConnector: DataCacheConnector,
                                                  @TrusteesIndividual navigator: Navigator2,
                                                  authenticate: AuthAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  formProvider: ContactDetailsFormProvider
                                      ) extends FrontendController with I18nSupport with Retrievals {

  private val form = formProvider()

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      TrusteeDetailsId(index).retrieve.right.map { trusteeDetails =>
        val preparedForm = request.userAnswers.get(TrusteeContactDetailsId(index)) match {
          case None => form
          case Some(value) => form.fill(value)
        }
        Future.successful(Ok(trusteeContactDetails(appConfig, preparedForm, mode, index, trusteeDetails.fullName)))
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          TrusteeDetailsId(index).retrieve.right.map { trusteeDetails =>
            Future.successful(BadRequest(trusteeContactDetails(appConfig, formWithErrors, mode, index, trusteeDetails.fullName)))
          },
        (value) =>
          dataCacheConnector.save(request.externalId, TrusteeContactDetailsId(index), value).map(cacheMap =>
            Redirect(navigator.nextPage(TrusteeContactDetailsId(index), mode, UserAnswers(cacheMap))))
      )
  }

}
