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
import models.{Index, Mode}
import forms.register.establishers.company.director.CompanyDirectorContactDetailsFormProvider
import identifiers.register.establishers.company.director.CompanyDirectorContactDetailsId
import models.register.establishers.company.director.CompanyDirectorContactDetails
import play.api.mvc.{Action, AnyContent}
import utils.{Enumerable, MapFormats, Navigator, UserAnswers}
import views.html.register.establishers.company.director.companyDirectorContactDetails

import scala.concurrent.Future

class CompanyDirectorContactDetailsController @Inject()(appConfig: FrontendAppConfig,
                                                        override val messagesApi: MessagesApi,
                                                        dataCacheConnector: DataCacheConnector,
                                                        navigator: Navigator,
                                                        authenticate: AuthAction,
                                                        getData: DataRetrievalAction,
                                                        requireData: DataRequiredAction,
                                                        formProvider: CompanyDirectorContactDetailsFormProvider
                                                       ) extends FrontendController with Retrievals with I18nSupport with Enumerable.Implicits with MapFormats {

  val form: Form[CompanyDirectorContactDetails] = formProvider()

  def onPageLoad(mode: Mode, establisherIndex: Index, directorIndex: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
        val redirectResult = request.userAnswers.get(CompanyDirectorContactDetailsId(establisherIndex, directorIndex)) match {
          case None => Ok(companyDirectorContactDetails(appConfig, form, mode, establisherIndex, directorIndex))
          case Some(value) => Ok(companyDirectorContactDetails(appConfig, form.fill(value), mode, establisherIndex, directorIndex))
        }
        Future.successful(redirectResult)
      }


  def onSubmit(mode: Mode, establisherIndex: Index, directorIndex: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
        form.bindFromRequest().fold(
          (formWithErrors: Form[_]) =>
            Future.successful(BadRequest(companyDirectorContactDetails(appConfig, formWithErrors, mode, establisherIndex, directorIndex))),
          (value) =>
            dataCacheConnector.save(request.externalId, CompanyDirectorContactDetailsId(establisherIndex, directorIndex), value).map(cacheMap =>
              Redirect(navigator.nextPage(CompanyDirectorContactDetailsId(establisherIndex, directorIndex), mode)(new UserAnswers(cacheMap))))
        )
      }
  }
