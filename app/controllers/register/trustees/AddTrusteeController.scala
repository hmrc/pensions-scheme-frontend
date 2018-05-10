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

package controllers.register.trustees

import javax.inject.Inject

import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.Retrievals
import controllers.actions._
import forms.register.trustees.AddTrusteeFormProvider
import identifiers.register.SchemeDetailsId
import identifiers.register.trustees.AddTrusteeId
import models.Mode
import play.api.Logger
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.JsResultException
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.Navigator
import utils.annotations.Trustees
import views.html.register.trustees.addTrustee

import scala.concurrent.Future

class AddTrusteeController @Inject() (
                                        appConfig: FrontendAppConfig,
                                        override val messagesApi: MessagesApi,
                                        dataCacheConnector: DataCacheConnector,
                                        @Trustees navigator: Navigator,
                                        authenticate: AuthAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: AddTrusteeFormProvider
                                      ) extends FrontendController with I18nSupport with Retrievals {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      SchemeDetailsId.retrieve.right.map { schemeDetails =>
        println("\n\n\n\n all trustees before: ")
        val trustees = request.userAnswers.allTrustees
        println("\n\n\n\n all trustees : "+trustees)
        println("mode : "+mode)
        Future.successful(Ok(addTrustee(appConfig, form, mode, schemeDetails.schemeName, trustees)))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>

      val trustees = request.userAnswers.allTrustees

      if (trustees.isEmpty || trustees.lengthCompare(appConfig.maxTrustees) >= 0)
        Future.successful(Redirect(navigator.nextPage(AddTrusteeId, mode)(request.userAnswers)))
      else {
        form.bindFromRequest().fold(
          (formWithErrors: Form[_]) =>
            SchemeDetailsId.retrieve.right.map { schemeDetails =>
              Future.successful(BadRequest(addTrustee(appConfig, formWithErrors, mode, schemeDetails.schemeName, trustees)))
            },
          (value) =>
            request.userAnswers.set(AddTrusteeId)(value).fold(
              errors => {
                Logger.error("Unable to set user answer", JsResultException(errors))
                Future.successful(InternalServerError)
              },
              userAnswers =>
                Future.successful(Redirect(navigator.nextPage(AddTrusteeId, mode)(userAnswers)))
            )
        )
      }
  }
}
