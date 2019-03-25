/*
 * Copyright 2019 HM Revenue & Customs
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

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions._
import forms.register.trustees.AddTrusteeFormProvider
import identifiers.register.trustees.AddTrusteeId
import javax.inject.Inject
import models.Mode
import play.api.Logger
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.JsResultException
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{Navigator}
import utils.annotations.Trustees
import views.html.register.trustees.addTrustee

import scala.concurrent.{ExecutionContext, Future}

class AddTrusteeController @Inject()(
                                      appConfig: FrontendAppConfig,
                                      override val messagesApi: MessagesApi,
                                      dataCacheConnector: UserAnswersCacheConnector,
                                      @Trustees navigator: Navigator,
                                      authenticate: AuthAction,
                                      getData: DataRetrievalAction,
                                      requireData: DataRequiredAction,
                                      formProvider: AddTrusteeFormProvider
                                    )(implicit val ec: ExecutionContext) extends FrontendController with I18nSupport with Retrievals {

  private val form = formProvider()

  def onPageLoad(mode: Mode, srn: Option[String] = None): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      val trustees = request.userAnswers.allTrusteesAfterDelete
      val submitUrl = controllers.register.trustees.routes.AddTrusteeController.onPageLoad(mode, srn)
      Future.successful(Ok(addTrustee(appConfig, form, mode, trustees, existingSchemeName, submitUrl)))
  }

  def onSubmit(mode: Mode, srn: Option[String] = None): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>

      val trustees = request.userAnswers.allTrusteesAfterDelete

      if (trustees.isEmpty || trustees.lengthCompare(appConfig.maxTrustees) >= 0)
        Future.successful(Redirect(navigator.nextPage(AddTrusteeId, mode, request.userAnswers)))
      else {
        form.bindFromRequest().fold(
          (formWithErrors: Form[_]) => {
            val submitUrl = controllers.register.trustees.routes.AddTrusteeController.onPageLoad(mode, srn)
            Future.successful(BadRequest(addTrustee(appConfig, formWithErrors, mode, trustees, existingSchemeName, submitUrl)))
          },
          value =>
            request.userAnswers.set(AddTrusteeId)(value).fold(
              errors => {
                Logger.error("Unable to set user answer", JsResultException(errors))
                Future.successful(InternalServerError)
              },
              userAnswers =>
                Future.successful(Redirect(navigator.nextPage(AddTrusteeId, mode, userAnswers)))
            )
        )
      }
  }
}
