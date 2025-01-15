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

package controllers.register.establishers.partnership.partner

import java.time.LocalDate
import config.FrontendAppConfig
import controllers.actions._
import controllers.dateOfBirth.DateOfBirthController
import forms.DOBFormProvider
import identifiers.register.establishers.partnership.partner.{PartnerDOBId, PartnerNameId}

import javax.inject.Inject
import models.requests.DataRequest
import models.{Index, Mode, OptionalSchemeReferenceNumber, SchemeReferenceNumber}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.UserAnswersService
import viewmodels.Message
import viewmodels.dateOfBirth.DateOfBirthViewModel
import views.html.register.DOB

import scala.concurrent.ExecutionContext

class PartnerDOBController @Inject()(
                                      val appConfig: FrontendAppConfig,
                                      override val messagesApi: MessagesApi,
                                      val userAnswersService: UserAnswersService,
                                      val navigator: Navigator,
                                      authenticate: AuthAction,
                                      getData: DataRetrievalAction,
                                      allowAccess: AllowAccessActionProvider,
                                      requireData: DataRequiredAction,
                                      formProvider: DOBFormProvider,
                                      val controllerComponents: MessagesControllerComponents,
                                      val view: DOB
                                    )(implicit val ec: ExecutionContext) extends DateOfBirthController {

  val form: Form[LocalDate] = formProvider()

  def onPageLoad(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: OptionalSchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        get(
          PartnerDOBId(establisherIndex, partnerIndex),
          PartnerNameId(establisherIndex, partnerIndex),
          viewModel(mode, establisherIndex, partnerIndex, srn, Message("messages__thePartner")),
          mode
        )
    }

  def onSubmit(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: OptionalSchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        post(
          PartnerDOBId(establisherIndex, partnerIndex),
          PartnerNameId(establisherIndex, partnerIndex),
          viewModel(mode, establisherIndex, partnerIndex, srn, Message("messages__thePartner")),
          mode
        )
    }

  private def viewModel(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: OptionalSchemeReferenceNumber, token: Message)
                       (implicit request: DataRequest[AnyContent]): DateOfBirthViewModel = {
    DateOfBirthViewModel(
      postCall = postCall(mode, establisherIndex, partnerIndex, srn),
      srn = srn,
      token = token
    )
  }

  private def postCall: (Mode, Index, Index, OptionalSchemeReferenceNumber) => Call = routes.PartnerDOBController.onSubmit
}
