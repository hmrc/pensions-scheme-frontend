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

package controllers

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.actions._
import forms.register.trustees.HaveAnyTrusteesFormProvider
import identifiers.HaveAnyTrusteesId
import javax.inject.Inject
import models.Mode
import models.requests.OptionalDataRequest
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.BeforeYouStart
import utils.{IDataFromRequest, Navigator, UserAnswers}
import views.html.haveAnyTrustees

import scala.concurrent.{ExecutionContext, Future}

class HaveAnyTrusteesController @Inject()(
                                           appConfig: FrontendAppConfig,
                                           override val messagesApi: MessagesApi,
                                           dataCacheConnector: UserAnswersCacheConnector,
                                           @BeforeYouStart navigator: Navigator,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           formProvider: HaveAnyTrusteesFormProvider
                                         )(implicit val ec: ExecutionContext) extends FrontendController with IDataFromRequest with I18nSupport with Retrievals {

  private val form: Form[Boolean] = formProvider()

  private def existingSchemeNameOrEmptyString(implicit request:OptionalDataRequest[AnyContent]):String =
    existingSchemeName.getOrElse("")

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData).async {
    implicit request =>
      val preparedForm = request.userAnswers.flatMap(_.get(HaveAnyTrusteesId)) match {
        case None => form
        case Some(value) => form.fill(value)
      }
      Future.successful(Ok(haveAnyTrustees(appConfig, preparedForm, mode, existingSchemeNameOrEmptyString)))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(haveAnyTrustees(appConfig, formWithErrors, mode, existingSchemeNameOrEmptyString))),
        value =>
          dataCacheConnector.save(request.externalId, HaveAnyTrusteesId, value).map(cacheMap =>
            Redirect(navigator.nextPage(HaveAnyTrusteesId, mode, UserAnswers(cacheMap))))
      )
  }
}
