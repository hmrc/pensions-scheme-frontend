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
import controllers.Retrievals
import controllers.actions._
import forms.register.establishers.individual.UniqueTaxReferenceFormProvider
import identifiers.register.establishers.individual.UniqueTaxReferenceId
import javax.inject.Inject
import models.{Index, Mode, UniqueTaxReference}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.EstablishersIndividual
import utils.{Enumerable, UserAnswers}
import views.html.register.establishers.individual._

import scala.concurrent.{ExecutionContext, Future}

class UniqueTaxReferenceController @Inject()(
                                              appConfig: FrontendAppConfig,
                                              override val messagesApi: MessagesApi,
                                              userAnswersService: UserAnswersService,
                                              navigator: Navigator,
                                              authenticate: AuthAction,
                                              getData: DataRetrievalAction,
                                              allowAccess: AllowAccessActionProvider,
                                              requireData: DataRequiredAction,
                                              formProvider: UniqueTaxReferenceFormProvider)(
                                              implicit val ec: ExecutionContext) extends FrontendController with Retrievals with I18nSupport with Enumerable.Implicits {

  private val form: Form[UniqueTaxReference] = formProvider()

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        retrieveEstablisherName(index) { _ =>
          val preparedForm = request.userAnswers.get(UniqueTaxReferenceId(index)).fold(form)(form.fill)
          val submitUrl = controllers.register.establishers.individual.routes.UniqueTaxReferenceController.onSubmit(mode, index, srn)
          Future.successful(Ok(uniqueTaxReference(appConfig, preparedForm, mode, index, existingSchemeName, submitUrl, srn)))
        }
    }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      retrieveEstablisherName(index) {
        establisherName =>
          form.bindFromRequest().fold(
            (formWithErrors: Form[_]) => {
              val submitUrl = controllers.register.establishers.individual.routes.UniqueTaxReferenceController.onSubmit(mode, index, srn)
              Future.successful(BadRequest(uniqueTaxReference(appConfig, formWithErrors, mode, index, existingSchemeName, submitUrl, srn)))
            },
            (value) =>
              userAnswersService.save(
                mode,
                srn,
                UniqueTaxReferenceId(index),
                value
              ).map {
                json =>
                  Redirect(navigator.nextPage(UniqueTaxReferenceId(index), mode, new UserAnswers(json), srn))
              }
          )
      }
  }

}
