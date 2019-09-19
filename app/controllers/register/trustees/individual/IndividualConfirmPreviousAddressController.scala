/*
 * Copyright 2019 HM Revenue & Customs
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

import config.{FeatureSwitchManagementService, FrontendAppConfig}
import controllers.Retrievals
import controllers.actions._
import controllers.address.ConfirmPreviousAddressController
import identifiers.register.trustees.ExistingCurrentAddressId
import identifiers.register.trustees.individual.{IndividualConfirmPreviousAddressId, TrusteeDetailsId, TrusteeNameId, TrusteePreviousAddressId}
import javax.inject.Inject
import models.{Index, Mode}
import navigators.Navigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import utils.{CountryOptions, Toggles}
import viewmodels.Message
import viewmodels.address.ConfirmAddressViewModel

import scala.concurrent.ExecutionContext

class IndividualConfirmPreviousAddressController @Inject()(val appConfig: FrontendAppConfig,
                                                           val messagesApi: MessagesApi,
                                                           val userAnswersService: UserAnswersService,
                                                           val navigator: Navigator,
                                                           authenticate: AuthAction,
                                                           allowAccess: AllowAccessActionProvider,
                                                           getData: DataRetrievalAction,
                                                           requireData: DataRequiredAction,
                                                           val countryOptions: CountryOptions,
                                                           fs: FeatureSwitchManagementService
                                                          )(implicit val ec: ExecutionContext) extends ConfirmPreviousAddressController with Retrievals with I18nSupport {

  private[controllers] val postCall = routes.IndividualConfirmPreviousAddressController.onSubmit _
  private[controllers] val title: Message = "messages__confirmPreviousAddress__title"
  private[controllers] val heading: Message = "messages__confirmPreviousAddress__heading"

  private def viewmodel(trusteeName: String, mode: Mode, index: Int, srn: Option[String]) = {
    Retrieval(
      implicit request =>
        ExistingCurrentAddressId(index).retrieve.right.map { address =>
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

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        trusteeName(index).retrieve.right.flatMap { trusteeName =>
          viewmodel(trusteeName, mode, index, srn).retrieve.right.map { vm =>
            get(IndividualConfirmPreviousAddressId(index), vm)
          }
        }
    }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        trusteeName(index).retrieve.right.flatMap { trusteeName =>
          viewmodel(trusteeName, mode, index, srn).retrieve.right.map { vm =>
            post(IndividualConfirmPreviousAddressId(index), TrusteePreviousAddressId(index), vm, mode)
          }
        }
    }

  val trusteeName: Index => Retrieval[String] = (trusteeIndex: Index) => Retrieval {
    implicit request =>
        TrusteeNameId(trusteeIndex).retrieve.right.map(_.fullName)
  }
}
