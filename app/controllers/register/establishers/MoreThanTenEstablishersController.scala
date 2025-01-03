/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.register.establishers

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import forms.register.establishers.MoreThanTenEstablishersFormProvider
import identifiers.register.establishers.MoreThanTenEstablishersId
import models.Mode
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.UserAnswers
import utils.annotations.Establishers
import views.html.register.establishers.moreThanTenEstablishers

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MoreThanTenEstablishersController @Inject()(
                                                   appConfig: FrontendAppConfig,
                                                   override val messagesApi: MessagesApi,
                                                   userAnswersService: UserAnswersService,
                                                   @Establishers navigator: Navigator,
                                                   authenticate: AuthAction,
                                                   getData: DataRetrievalAction,
                                                   allowAccess: AllowAccessActionProvider,
                                                   requireData: DataRequiredAction,
                                                   formProvider: MoreThanTenEstablishersFormProvider,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   val view: moreThanTenEstablishers
                                                 )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController with Retrievals with I18nSupport {

  private val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode, srn: Option[String]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        val submitUrl = controllers.register.establishers.routes.MoreThanTenEstablishersController.onSubmit(mode, srn)
        val updatedForm = request.userAnswers.get(MoreThanTenEstablishersId).fold(form)(form.fill)
        Future.successful(Ok(view(updatedForm, mode, ???, existingSchemeName, submitUrl, srn)))
    }

  def onSubmit(mode: Mode, srn: Option[String]): Action[AnyContent] = (authenticate() andThen getData(mode, srn)
    andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) => {
          val submitUrl = controllers.register.establishers.routes.MoreThanTenEstablishersController.onSubmit(mode, srn)
          Future.successful(BadRequest(view(formWithErrors, mode, ???, existingSchemeName, submitUrl, srn)))
        },
        value =>
          userAnswersService.save(mode, srn, MoreThanTenEstablishersId, value).map(cacheMap =>
            Redirect(navigator.nextPage(MoreThanTenEstablishersId, mode, UserAnswers(cacheMap), srn)))
      )
  }
}
