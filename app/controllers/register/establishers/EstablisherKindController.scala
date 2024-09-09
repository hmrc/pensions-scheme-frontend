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

package controllers.register.establishers

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import forms.register.establishers.EstablisherKindFormProvider
import identifiers.register.establishers.{EstablisherKindId, IsEstablisherNewId}
import javax.inject.Inject
import models.{Index, Mode}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.Establishers
import utils.{Enumerable, UserAnswers}
import views.html.register.establishers.establisherKind

import scala.concurrent.{ExecutionContext, Future}
import models.SchemeReferenceNumber

class EstablisherKindController @Inject()(
                                           appConfig: FrontendAppConfig,
                                           override val messagesApi: MessagesApi,
                                           val userAnswersService: UserAnswersService,
                                           @Establishers navigator: Navigator,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           allowAccess: AllowAccessActionProvider,
                                           requireData: DataRequiredAction,
                                           formProvider: EstablisherKindFormProvider,
                                           val controllerComponents: MessagesControllerComponents,
                                           val view: establisherKind
                                         )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController with Retrievals with I18nSupport with Enumerable.Implicits {

  private val form = formProvider()
  private val postCall = routes.EstablisherKindController.onSubmit _

  def onPageLoad(mode: Mode, index: Index, srn: Option[SchemeReferenceNumber]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        val formWithData = request.userAnswers.get(EstablisherKindId(index)).fold(form)(form.fill)
        Future.successful(Ok(view(formWithData, srn, index, existingSchemeName, postCall(mode, index, srn))))
    }

  def onSubmit(mode: Mode, index: Index, srn: Option[SchemeReferenceNumber]): Action[AnyContent] = (authenticate() andThen getData
  (mode, srn) andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, srn, index, existingSchemeName, postCall(mode, index,
            srn)))),
        value =>

          request.userAnswers.upsert(IsEstablisherNewId(index))(value = true) {
            _.upsert(EstablisherKindId(index))(value) { answers =>
              userAnswersService.upsert(mode, srn, answers.json).map {
                json =>
                  Redirect(navigator.nextPage(EstablisherKindId(index), mode, UserAnswers(json), srn))
              }
            }
          }
      )
  }

}
