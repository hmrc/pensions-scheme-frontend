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
import controllers.Retrievals
import controllers.actions._
import controllers.address.ConfirmPreviousAddressController
import identifiers.register.trustees.ExistingCurrentAddressId
import identifiers.register.trustees.individual.{IndividualConfirmPreviousAddressId, TrusteeNameId, TrusteePreviousAddressId}
import javax.inject.Inject
import models.{Index, Mode}
import navigators.Navigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import utils.CountryOptions
import viewmodels.Message
import viewmodels.address.ConfirmAddressViewModel
import views.html.address.confirmPreviousAddress

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
                                                           val controllerComponents: MessagesControllerComponents,
                                                           val view: confirmPreviousAddress
                                                          )(implicit val ec: ExecutionContext) extends
  ConfirmPreviousAddressController with Retrievals with I18nSupport {

  val trusteeName: Index => Retrieval[String] = (trusteeIndex: Index) => Retrieval {
    implicit request =>
      TrusteeNameId(trusteeIndex).retrieve.map(_.fullName)
  }
  private[controllers] val postCall = routes.IndividualConfirmPreviousAddressController.onSubmit _
  private[controllers] val title: Message = "messages__confirmPreviousAddress__title"
  private[controllers] val heading: Message = "messages__confirmPreviousAddress__heading"

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        trusteeName(index).retrieve.flatMap { trusteeName =>
          viewmodel(trusteeName, mode, index, srn).retrieve.map { vm =>
            get(IndividualConfirmPreviousAddressId(index), vm)
          }
        }
    }

  private def viewmodel(trusteeName: String, mode: Mode, index: Int, srn: Option[String]) = {
    Retrieval(
      implicit request =>
        ExistingCurrentAddressId(index).retrieve.map { address =>
          ConfirmAddressViewModel(
            postCall(index, srn),
            title = Message(title),
            heading = Message(heading, trusteeName),
            hint = None,
            address = address,
            name = trusteeName,
            srn = srn
          )
        }
    )
  }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        trusteeName(index).retrieve.flatMap { trusteeName =>
          viewmodel(trusteeName, mode, index, srn).retrieve.map { vm =>
            post(IndividualConfirmPreviousAddressId(index), TrusteePreviousAddressId(index), vm, mode)
          }
        }
    }
}
