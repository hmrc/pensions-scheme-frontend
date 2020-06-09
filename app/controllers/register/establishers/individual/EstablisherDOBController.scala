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

package controllers.register.establishers.individual

import config.FrontendAppConfig
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.dateOfBirth.DateOfBirthController
import forms.DOBFormProvider
import identifiers.register.establishers.individual.{EstablisherDOBId, EstablisherNameId}
import javax.inject.Inject
import models.requests.DataRequest
import models.{Index, Mode}
import navigators.Navigator
import java.time.LocalDate
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.UserAnswersService
import viewmodels.Message
import viewmodels.dateOfBirth.DateOfBirthViewModel
import views.html.register.DOB

import scala.concurrent.ExecutionContext

class EstablisherDOBController @Inject()(val appConfig: FrontendAppConfig,
                                         override val messagesApi: MessagesApi,
                                         val userAnswersService: UserAnswersService,
                                         val navigator: Navigator,
                                         authenticate: AuthAction,
                                         getData: DataRetrievalAction,
                                         allowAccess: AllowAccessActionProvider,
                                         requireData: DataRequiredAction,
                                         formProvider: DOBFormProvider,
                                         val view: DOB,
                                         val controllerComponents: MessagesControllerComponents
                                        )(implicit val ec: ExecutionContext) extends DateOfBirthController {

  val form: Form[LocalDate] = formProvider()

  private def postCall: (Mode, Index, Option[String]) => Call = routes.EstablisherDOBController.onSubmit

  private def viewModel(mode: Mode, index: Index, srn: Option[String], token: String)
                       (implicit request: DataRequest[AnyContent]): DateOfBirthViewModel = {
    DateOfBirthViewModel(
      postCall = postCall(mode, index, srn),
      srn = srn,
      token = token
    )
  }

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        get(EstablisherDOBId(index), EstablisherNameId(index), viewModel(mode, index, srn, Message("messages__theIndividual")), mode)
    }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        post(EstablisherDOBId(index), EstablisherNameId(index), viewModel(mode, index, srn, Message("messages__theIndividual")), mode)
    }
}
