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

package controllers.register.establishers.company.director

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import controllers.address.ConfirmPreviousAddressController
import identifiers.register.establishers.company.director._

import javax.inject.Inject
import models.{Index, Mode, SchemeReferenceNumber}
import navigators.Navigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import utils.CountryOptions
import utils.annotations.EstablishersCompanyDirector
import viewmodels.Message
import viewmodels.address.ConfirmAddressViewModel
import views.html.address.confirmPreviousAddress

import scala.concurrent.ExecutionContext

class DirectorConfirmPreviousAddressController @Inject()(val appConfig: FrontendAppConfig,
                                                         override val messagesApi: MessagesApi,
                                                         val userAnswersService: UserAnswersService,
                                                         @EstablishersCompanyDirector val navigator: Navigator,
                                                         authenticate: AuthAction,
                                                         allowAccess: AllowAccessActionProvider,
                                                         getData: DataRetrievalAction,
                                                         requireData: DataRequiredAction,
                                                         val countryOptions: CountryOptions,
                                                         val view: confirmPreviousAddress,
                                                         val controllerComponents: MessagesControllerComponents
                                                        )(implicit val ec: ExecutionContext) extends
  ConfirmPreviousAddressController with Retrievals with I18nSupport {

  val directorName = (establisherIndex: Index, directorIndex: Index) => Retrieval {
    implicit request =>
      DirectorNameId(establisherIndex, directorIndex).retrieve.map(_.fullName)
  }
  private[controllers] val postCall = routes.DirectorConfirmPreviousAddressController.onSubmit _
  private[controllers] val title: Message = "messages__confirmPreviousAddress__title"
  private[controllers] val heading: Message = "messages__confirmPreviousAddress__heading"

  def onPageLoad(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: SchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData() andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        viewmodel(mode, establisherIndex, directorIndex, srn).retrieve.map { vm =>
          get(DirectorConfirmPreviousAddressId(establisherIndex, directorIndex), vm)
        }
    }

  def onSubmit(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: SchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData() andThen requireData).async {
      implicit request =>
        viewmodel(mode, establisherIndex, directorIndex, srn).retrieve.map { vm =>
          post(DirectorConfirmPreviousAddressId(establisherIndex, directorIndex), DirectorPreviousAddressId
          (establisherIndex, directorIndex), vm, mode)
        }
    }

  private def viewmodel(mode: Mode, establisherIndex: Int, directorIndex: Int, srn: SchemeReferenceNumber) =
    Retrieval(
      implicit request =>
        (directorName(establisherIndex, directorIndex) and ExistingCurrentAddressId(establisherIndex, directorIndex))
          .retrieve.map {
          case name ~ address =>
            ConfirmAddressViewModel(
              postCall(establisherIndex, directorIndex, srn),
              title = Message(title),
              heading = Message(heading, name),
              hint = None,
              address = address,
              name = name,
              srn = srn
            )
        }
    )


}
