/*
 * Copyright 2024 HM Revenue & Customs
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

import connectors.UserAnswersCacheConnector
import controllers.actions._
import forms.register.trustees.HaveAnyTrusteesFormProvider
import identifiers.{HaveAnyTrusteesId, SchemeNameId}
import models.Mode
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.UserAnswers
import utils.annotations.BeforeYouStart
import views.html.haveAnyTrustees

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class HaveAnyTrusteesController @Inject()(
                                           override val messagesApi: MessagesApi,
                                           dataCacheConnector: UserAnswersCacheConnector,
                                           @BeforeYouStart navigator: Navigator,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           formProvider: HaveAnyTrusteesFormProvider,
                                           val controllerComponents: MessagesControllerComponents,
                                           val view: haveAnyTrustees
                                         )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController with I18nSupport with Retrievals {

  private val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate() andThen getData() andThen requireData).async {
    implicit request =>
      SchemeNameId.retrieve.map { schemeName =>
        val preparedForm = request.userAnswers.get(HaveAnyTrusteesId) match {
          case None => form
          case Some(value) => form.fill(value)
        }
        Future.successful(Ok(view(preparedForm, mode, schemeName)))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate() andThen getData() andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[?]) =>
          SchemeNameId.retrieve.map { schemeName =>
            Future.successful(BadRequest(view(formWithErrors, mode, schemeName)))
          },
        value =>
          dataCacheConnector.save(request.externalId, HaveAnyTrusteesId, value).map(cacheMap =>
            Redirect(navigator.nextPage(HaveAnyTrusteesId, mode, UserAnswers(cacheMap))))
      )
  }
}
