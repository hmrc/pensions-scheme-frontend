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

package controllers.register.establishers.individual

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import controllers.address.ConfirmPreviousAddressController
import identifiers.register.establishers.ExistingCurrentAddressId
import identifiers.register.establishers.individual.{EstablisherNameId, IndividualConfirmPreviousAddressId, PreviousAddressId}
import models.{Index, Mode, OptionalSchemeReferenceNumber}
import navigators.Navigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import utils.CountryOptions
import viewmodels.Message
import viewmodels.address.ConfirmAddressViewModel
import views.html.address.confirmPreviousAddress

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class IndividualConfirmPreviousAddressController @Inject()(val appConfig: FrontendAppConfig,
                                                           override val messagesApi: MessagesApi,
                                                           val userAnswersService: UserAnswersService,
                                                           val navigator: Navigator,
                                                           authenticate: AuthAction,
                                                           allowAccess: AllowAccessActionProvider,
                                                           getData: DataRetrievalAction,
                                                           requireData: DataRequiredAction,
                                                           val countryOptions: CountryOptions,
                                                           val view: confirmPreviousAddress,
                                                           val controllerComponents: MessagesControllerComponents
                                                          )(implicit val ec: ExecutionContext) extends
  ConfirmPreviousAddressController with Retrievals with I18nSupport {

  private[controllers] val postCall = routes.IndividualConfirmPreviousAddressController.onSubmit
  private[controllers] val heading: Message = "messages__confirmPreviousAddress__heading"

  def onPageLoad(mode: Mode, index: Index, srn: OptionalSchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        viewmodel(mode, index, srn).retrieve.map { vm =>
          get(IndividualConfirmPreviousAddressId(index), vm)
        }
    }

  def onSubmit(mode: Mode, index: Index, srn: OptionalSchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        viewmodel(mode, index, srn).retrieve.map { vm =>
          post(IndividualConfirmPreviousAddressId(index), PreviousAddressId(index), vm, mode)
        }
    }

  private def viewmodel(mode: Mode, index: Int, srn: OptionalSchemeReferenceNumber) =
    Retrieval(
      implicit request =>
        EstablisherNameId(index).and(ExistingCurrentAddressId(index)).retrieve.map {
          case details ~ address =>
            ConfirmAddressViewModel(
              postCall(index, srn),
              title = Message(heading, Message("messages__theIndividual")),
              heading = Message(heading, details.fullName),
              hint = None,
              address = address,
              name = details.fullName,
              srn = srn
            )
        }
    )


}
