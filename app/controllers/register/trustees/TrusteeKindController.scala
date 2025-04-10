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

package controllers.register.trustees

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import forms.register.trustees.TrusteeKindFormProvider
import identifiers.register.trustees.{IsTrusteeNewId, TrusteeKindId}
import models.{Index, Mode, OptionalSchemeReferenceNumber}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.Trustees
import utils.{Enumerable, UserAnswers}
import views.html.register.trustees.trusteeKind

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TrusteeKindController @Inject()(
                                       appConfig: FrontendAppConfig,
                                       override val messagesApi: MessagesApi,
                                       userAnswersService: UserAnswersService,
                                       @Trustees navigator: Navigator,
                                       authenticate: AuthAction,
                                       getData: DataRetrievalAction,
                                       allowAccess: AllowAccessActionProvider,
                                       requireData: DataRequiredAction,
                                       formProvider: TrusteeKindFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       val view: trusteeKind
                                     )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController with Retrievals with I18nSupport with Enumerable.Implicits {

  private val form = formProvider()

  def onPageLoad(mode: Mode, index: Index, srn: OptionalSchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        val preparedForm = request.userAnswers.get(TrusteeKindId(index)) match {
          case None => form
          case Some(value) => form.fill(value)
        }
        val submitUrl = controllers.register.trustees.routes.TrusteeKindController.onSubmit(mode, index, srn)
        Future.successful(Ok(view(preparedForm, mode, index, existingSchemeName, submitUrl, srn)))
    }

  def onSubmit(mode: Mode, index: Index, srn: OptionalSchemeReferenceNumber): Action[AnyContent] = (authenticate() andThen getData
  (mode, srn) andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[?]) => {
          val submitUrl = controllers.register.trustees.routes.TrusteeKindController.onSubmit(mode, index, srn)
          Future.successful(BadRequest(view(formWithErrors, mode, index, existingSchemeName, submitUrl, srn)))
        },
        value => {

          request.userAnswers.upsert(IsTrusteeNewId(index))(value = true) {
            _.upsert(TrusteeKindId(index))(value) { answers =>
              userAnswersService.upsert(mode, srn, answers.json).map {
                json =>
                  Redirect(navigator.nextPage(TrusteeKindId(index), mode, UserAnswers(json), srn))
              }
            }
          }

        }
      )
  }
}
