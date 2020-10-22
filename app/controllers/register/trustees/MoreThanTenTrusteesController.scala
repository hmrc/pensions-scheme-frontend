/*
 * Copyright 2020 HM Revenue & Customs
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
import forms.register.trustees.MoreThanTenTrusteesFormProvider
import identifiers.register.trustees.MoreThanTenTrusteesId
import javax.inject.Inject
import models.Mode
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.{FrontendBaseController, FrontendController}
import utils.annotations.Trustees
import utils.UserAnswers
import views.html.register.trustees.moreThanTenTrustees

import scala.concurrent.{ExecutionContext, Future}

class MoreThanTenTrusteesController @Inject()(
                                               appConfig: FrontendAppConfig,
                                               override val messagesApi: MessagesApi,
                                               userAnswersService: UserAnswersService,
                                               @Trustees navigator: Navigator,
                                               authenticate: AuthAction,
                                               getData: DataRetrievalAction,
                                               allowAccess: AllowAccessActionProvider,
                                               requireData: DataRequiredAction,
                                               formProvider: MoreThanTenTrusteesFormProvider,
                                               val controllerComponents: MessagesControllerComponents,
                                               val view: moreThanTenTrustees
                                             )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController with Retrievals with I18nSupport {

  private val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode, srn: Option[String]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        val submitUrl = controllers.register.trustees.routes.MoreThanTenTrusteesController.onSubmit(mode, srn)
        val updatedForm = request.userAnswers.get(MoreThanTenTrusteesId).fold(form)(form.fill)
        Future.successful(Ok(view(updatedForm, mode, existingSchemeName, submitUrl, srn)))
    }

  def onSubmit(mode: Mode, srn: Option[String]): Action[AnyContent] = (authenticate() andThen getData(mode, srn)
    andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) => {
          val submitUrl = controllers.register.trustees.routes.MoreThanTenTrusteesController.onSubmit(mode, srn)
          Future.successful(BadRequest(view(formWithErrors, mode, existingSchemeName, submitUrl, srn)))
        },
        value =>
          userAnswersService.save(mode, srn, MoreThanTenTrusteesId, value).map(cacheMap =>
            Redirect(navigator.nextPage(MoreThanTenTrusteesId, mode, UserAnswers(cacheMap), srn)))
      )
  }
}
