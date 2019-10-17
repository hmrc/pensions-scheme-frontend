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

package controllers.register.establishers.individual

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions._
import forms.register.establishers.individual.EstablisherNinoFormProvider
import identifiers.register.establishers.individual.EstablisherNinoId
import javax.inject.Inject
import models.{Index, Mode, Nino}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.EstablishersIndividual
import utils.{Enumerable, UserAnswers}
import views.html.register.establishers.individual.establisherNino

import scala.concurrent.{ExecutionContext, Future}

class EstablisherNinoController @Inject()(
                                           appConfig: FrontendAppConfig,
                                           override val messagesApi: MessagesApi,
                                           userAnswersService: UserAnswersService,
                                           navigator: Navigator,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           allowAccess: AllowAccessActionProvider,
                                           requireData: DataRequiredAction,
                                           formProvider: EstablisherNinoFormProvider
                                         )(implicit val ec: ExecutionContext) extends FrontendController with Retrievals with I18nSupport with Enumerable.Implicits {

  private val form: Form[Nino] = formProvider()

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
    implicit request =>
      retrieveEstablisherName(index) { _ =>
        val preparedForm = request.userAnswers.get(EstablisherNinoId(index)).fold(form)(form.fill)
        val submitUrl = controllers.register.establishers.individual.routes.EstablisherNinoController.onSubmit(mode, index, srn)
        Future.successful(Ok(establisherNino(appConfig, preparedForm, mode, index, existingSchemeName, submitUrl, srn)))
      }
  }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      retrieveEstablisherName(index) {
        establisherName =>
          form.bindFromRequest().fold(
            (formWithErrors: Form[_]) => {
              val submitUrl = controllers.register.establishers.individual.routes.EstablisherNinoController.onSubmit(mode, index, srn)
              Future.successful(BadRequest(establisherNino(appConfig, formWithErrors, mode, index, existingSchemeName, submitUrl, srn)))
            },
            (value) =>
              userAnswersService.save(
                mode,
                srn,
                EstablisherNinoId(index),
                value
              ).map {
                json =>
                  Redirect(navigator.nextPage(EstablisherNinoId(index), mode, new UserAnswers(json), srn))
              }
          )
      }
  }

}
