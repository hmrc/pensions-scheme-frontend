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

package controllers.register.establishers.company.director

import javax.inject.Inject

import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import connectors.DataCacheConnector
import controllers.actions._
import config.FrontendAppConfig
import controllers.Retrievals
import forms.register.establishers.company.director.DirectorDetailsFormProvider
import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.establishers.company.director.DirectorDetailsId
import models.register.establishers.company.director.DirectorDetails
import models.{Index, Mode}
import play.api.mvc.{Action, AnyContent}
import utils.annotations.EstablishersCompanyDirector
import utils.{Navigator, UserAnswers}
import views.html.register.establishers.company.director.directorDetails

import scala.concurrent.Future

class DirectorDetailsController @Inject() (
                                        appConfig: FrontendAppConfig,
                                        override val messagesApi: MessagesApi,
                                        dataCacheConnector: DataCacheConnector,
                                        @EstablishersCompanyDirector navigator: Navigator,
                                        authenticate: AuthAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: DirectorDetailsFormProvider
                                      ) extends FrontendController with Retrievals with I18nSupport {

  private val form = formProvider()

  def onPageLoad(mode: Mode,establisherIndex:Index, directorIndex:Index): Action[AnyContent] =
    (authenticate andThen getData andThen requireData).async {
      implicit request =>
        CompanyDetailsId(establisherIndex).retrieve.right.map { companyDetails =>
          val preparedForm = request.userAnswers.get[DirectorDetails](DirectorDetailsId(establisherIndex, directorIndex)) match {
            case None => form
            case Some(value) => form.fill(value)
          }
          Future.successful(Ok(directorDetails(appConfig, preparedForm, mode, establisherIndex, directorIndex, companyDetails.companyName)))
        }
    }

  def onSubmit(mode: Mode,establisherIndex:Index,directorIndex:Index): Action[AnyContent] =
    (authenticate andThen getData andThen requireData).async {
      implicit request =>
        CompanyDetailsId(establisherIndex).retrieve.right.map { companyDetails =>
          form.bindFromRequest().fold(
            (formWithErrors: Form[_]) =>
              Future.successful(BadRequest(directorDetails(appConfig, formWithErrors, mode, establisherIndex, directorIndex,companyDetails.companyName)))
            ,
            (value) =>
              dataCacheConnector.save(request.externalId, DirectorDetailsId(establisherIndex, directorIndex), value).map(cacheMap =>
                Redirect(navigator.nextPage(DirectorDetailsId(establisherIndex, directorIndex), mode)(new UserAnswers(cacheMap))))
          )
        }
    }
}

