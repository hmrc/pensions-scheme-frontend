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

import javax.inject.Inject

import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import connectors.DataCacheConnector
import controllers.actions._
import config.FrontendAppConfig
import forms.register.establishers.company.CompanyUniqueTaxReferenceFormProvider
import identifiers.register.establishers.company.CompanyUniqueTaxReferenceId
import models.{Index, Mode, UniqueTaxReference}
import play.api.mvc.{Action, AnyContent}
import utils.{Enumerable, MapFormats, Navigator, UserAnswers}
import views.html.register.establishers.company.companyUniqueTaxReference
import scala.concurrent.Future
import scala.util.{Failure, Success}

class CompanyUniqueTaxReferenceController @Inject()(
                                        appConfig: FrontendAppConfig,
                                        override val messagesApi: MessagesApi,
                                        dataCacheConnector: DataCacheConnector,
                                        navigator: Navigator,
                                        authenticate: AuthAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: CompanyUniqueTaxReferenceFormProvider) extends FrontendController with I18nSupport
  with Enumerable.Implicits with MapFormats {

  val form: Form[UniqueTaxReference] = formProvider()

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async{
    implicit request =>
      val redirectResult = request.userAnswers.companyUniqueTaxReference(index) match {
        case Success(None) => Ok(companyUniqueTaxReference(appConfig, form, mode, index))
        case Success(Some(value)) => Ok(companyUniqueTaxReference(appConfig, form.fill(value), mode, index))
        case Failure(_) => Redirect(controllers.routes.SessionExpiredController.onPageLoad())
      }
      Future.successful(redirectResult)
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(companyUniqueTaxReference(appConfig, formWithErrors, mode, index))),
        (value) =>
          dataCacheConnector.save[UniqueTaxReference](request.externalId,
            CompanyUniqueTaxReferenceId.toString, value).map(cacheMap =>
            Redirect(navigator.nextPage(CompanyUniqueTaxReferenceId, mode)(new UserAnswers(cacheMap))))
      )
  }
}
