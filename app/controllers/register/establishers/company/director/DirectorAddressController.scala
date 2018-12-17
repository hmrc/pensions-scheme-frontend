/*
 * Copyright 2018 HM Revenue & Customs
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

import audit.AuditService
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.actions._
import controllers.address.ManualAddressController
import controllers.register.establishers.company.director.routes._
import forms.address.AddressFormProvider
import identifiers.register.establishers.company.director.{DirectorAddressId, DirectorAddressListId, DirectorDetailsId}
import javax.inject.Inject
import models.address.Address
import models.{Index, Mode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import utils.annotations.EstablishersCompanyDirector
import utils.{CountryOptions, Navigator}
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel

import scala.concurrent.ExecutionContext

class DirectorAddressController @Inject()(
                                           val appConfig: FrontendAppConfig,
                                           val messagesApi: MessagesApi,
                                           val dataCacheConnector: UserAnswersCacheConnector,
                                           @EstablishersCompanyDirector val navigator: Navigator,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           val formProvider: AddressFormProvider,
                                           val countryOptions: CountryOptions,
                                           val auditService: AuditService
                                         ) (implicit val ec: ExecutionContext) extends ManualAddressController with I18nSupport {

  private[controllers] val postCall = DirectorAddressController.onSubmit _
  private[controllers] val title: Message = "messages__directorAddressPostcodeLookup__title"
  private[controllers] val heading: Message = "messages__directorAddressPostcodeLookup__heading"
  private[controllers] val hint: Message = "messages__directorAddressPostcodeLookup__lede"

  protected val form: Form[Address] = formProvider()

  def onPageLoad(mode: Mode, establisherIndex: Index, directorIndex: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(establisherIndex, directorIndex, mode).retrieve.right.map {
        vm =>
          get(DirectorAddressId(establisherIndex, directorIndex), DirectorAddressListId(establisherIndex, directorIndex), vm)
      }
  }

  def onSubmit(mode: Mode, establisherIndex: Index, directorIndex: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(establisherIndex, directorIndex, mode).retrieve.right.map {
        vm =>
          post(
            DirectorAddressId(establisherIndex, directorIndex),
            DirectorAddressListId(establisherIndex, directorIndex),
            vm,
            mode,
            context(vm)
          )
      }
  }

  private def viewmodel(establisherIndex: Int, directorIndex: Int, mode: Mode): Retrieval[ManualAddressViewModel] =
    Retrieval {
      implicit request =>
        DirectorDetailsId(establisherIndex, directorIndex).retrieve.right.map {
          details =>
            ManualAddressViewModel(
              postCall(mode, Index(establisherIndex), Index(directorIndex)),
              countryOptions.options,
              title = Message(title),
              heading = Message(heading),
              hint = Some(Message(hint)),
              secondaryHeader = Some(details.fullName)
            )
        }
    }

  private def context(viewModel: ManualAddressViewModel): String = {
    viewModel.secondaryHeader match {
      case Some(name) => s"Company Director Address: $name"
      case _ => "Company Director Address"
    }
  }

}
