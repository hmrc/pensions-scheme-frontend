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

package controllers.register.trustees.individual

import config.FrontendAppConfig
import controllers.NinoController
import controllers.actions._
import forms.NINOFormProvider
import identifiers.register.trustees.individual.{TrusteeEnterNINOId, TrusteeNameId}
import models.requests.DataRequest
import models.{Index, Mode, OptionalSchemeReferenceNumber}
import navigators.Navigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.UserAnswersService
import viewmodels.{Message, NinoViewModel}
import views.html.nino

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TrusteeEnterNINOController @Inject()(
                                            val appConfig: FrontendAppConfig,
                                            override val messagesApi: MessagesApi,
                                            val userAnswersService: UserAnswersService,
                                            val navigator: Navigator,
                                            authenticate: AuthAction,
                                            getData: DataRetrievalAction,
                                            allowAccess: AllowAccessActionProvider,
                                            requireData: DataRequiredAction,
                                            val formProvider: NINOFormProvider,
                                            val controllerComponents: MessagesControllerComponents,
                                            val view: nino
                                          )(implicit val ec: ExecutionContext) extends NinoController with I18nSupport {

  private[controllers] val postCall = controllers.register.trustees.individual.routes.TrusteeEnterNINOController
    .onSubmit _

  def onPageLoad(mode: Mode, index: Index, srn: OptionalSchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        val fullNameOption: Either[Future[Result], String] =
          TrusteeNameId(index).retrieve.map(_.fullName)

        fullNameOption.map {
          fullName =>
            get(TrusteeEnterNINOId(index), formProvider(fullName), viewmodel(fullName, index, mode, srn))
        }
    }

  def onSubmit(mode: Mode, index: Index, srn: OptionalSchemeReferenceNumber): Action[AnyContent] = (authenticate() andThen getData
  (mode, srn) andThen requireData).async {
    implicit request =>
      val fullNameOption: Either[Future[Result], String] =
        TrusteeNameId(index).retrieve.map(_.fullName)

      fullNameOption.map {
        fullName =>
          post(TrusteeEnterNINOId(index), mode, formProvider(fullName), viewmodel(fullName, index, mode, srn))
      }
  }

  private def viewmodel(fullName: String, index: Index, mode: Mode, srn: OptionalSchemeReferenceNumber
                       )(implicit request: DataRequest[AnyContent]): NinoViewModel =
    NinoViewModel(
      postCall(mode, Index(index), srn),
      title = Message("messages__enterNINO", Message("messages__theIndividual")),
      heading = Message("messages__enterNINO", fullName),
      hint = Message("messages__common__nino_hint"),
      srn = srn
    )

}
