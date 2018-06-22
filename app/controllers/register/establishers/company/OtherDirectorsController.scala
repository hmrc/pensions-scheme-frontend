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

package controllers.register.establishers.company

import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.Retrievals
import controllers.actions._
import forms.register.establishers.company.OtherDirectorsFormProvider
import identifiers.register.establishers.company.OtherDirectorsId
import javax.inject.Inject
import models.{Index, Mode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.EstablishersCompany
import utils.{Navigator, UserAnswers}
import views.html.register.establishers.company.otherDirectors

import scala.concurrent.Future

class OtherDirectorsController @Inject()(
                                          appConfig: FrontendAppConfig,
                                          override val messagesApi: MessagesApi,
                                          dataCacheConnector: DataCacheConnector,
                                          @EstablishersCompany navigator: Navigator,
                                          authenticate: AuthAction,
                                          getData: DataRetrievalAction,
                                          requireData: DataRequiredAction,
                                          formProvider: OtherDirectorsFormProvider
                                        ) extends FrontendController with Retrievals with I18nSupport {

  private val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode, establisherIndex: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveCompanyName(establisherIndex) { companyName =>
        val redirectResult = request.userAnswers.get(OtherDirectorsId(establisherIndex)) match {
          case None => Ok(otherDirectors(appConfig, form, mode, establisherIndex, companyName))
          case Some(value) => Ok(otherDirectors(appConfig, form.fill(value), mode, establisherIndex, companyName))
        }
        Future.successful(redirectResult)
      }

  }

  def onSubmit(mode: Mode, establisherIndex: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveCompanyName(establisherIndex) {
        companyName =>
          form.bindFromRequest().fold(
            (formWithErrors: Form[_]) =>
              Future.successful(BadRequest(otherDirectors(appConfig, formWithErrors, mode, establisherIndex, companyName))),
            value =>
              dataCacheConnector.save(request.externalId, OtherDirectorsId(establisherIndex), value).map(cacheMap =>
                Redirect(navigator.nextPage(OtherDirectorsId(establisherIndex), mode, UserAnswers(cacheMap))))
          )
      }
  }
}
