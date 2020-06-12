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

package controllers.register.trustees.partnership

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.AddressYearsController
import forms.address.AddressYearsFormProvider
import identifiers.register.trustees.partnership.{PartnershipAddressYearsId, PartnershipDetailsId}
import models.requests.DataRequest
import models.{AddressYears, Index, Mode}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import viewmodels.Message
import viewmodels.address.AddressYearsViewModel
import views.html.address.addressYears

import scala.concurrent.ExecutionContext

class PartnershipAddressYearsController @Inject()(
                                                   override val appConfig: FrontendAppConfig,
                                                   val userAnswersService: UserAnswersService,
                                                   override val navigator: Navigator,
                                                   override val messagesApi: MessagesApi,
                                                   authenticate: AuthAction,
                                                   getData: DataRetrievalAction,
                                                   allowAccess: AllowAccessActionProvider,
                                                   requireData: DataRequiredAction,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   val view: addressYears
                                                 )(implicit val ec: ExecutionContext) extends AddressYearsController
  with Retrievals {

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        PartnershipDetailsId(index).retrieve.right.map { partnershipDetails =>
          get(PartnershipAddressYearsId(index), form(partnershipDetails.name), viewModel(mode, index,
            partnershipDetails.name, srn))
        }
    }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData
  (mode, srn) andThen requireData).async {
    implicit request =>
      PartnershipDetailsId(index).retrieve.right.map { partnershipDetails =>
        post(PartnershipAddressYearsId(index), mode, form(partnershipDetails.name), viewModel(mode, index,
          partnershipDetails.name, srn))
      }
  }

  private def form(partnershipName: String)(implicit request: DataRequest[AnyContent]): Form[AddressYears] =
    new AddressYearsFormProvider()(Message("messages__partnershipAddressYears__error", partnershipName))

  private def viewModel(mode: Mode, index: Index, partnershipName: String, srn: Option[String]) = AddressYearsViewModel(
    postCall = routes.PartnershipAddressYearsController.onSubmit(mode, index, srn),
    title = Message("messages__partnershipAddressYears__title", Message("messages__thePartnership")),
    heading = Message("messages__trusteeAddressYears__heading", partnershipName),
    legend = Message("messages__trusteeAddressYears__heading", partnershipName),
    subHeading = Some(Message(partnershipName)),
    srn = srn
  )

}
