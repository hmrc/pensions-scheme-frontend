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

package controllers.register.establishers.company.director

import audit.AuditService
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.actions._
import controllers.address.ManualAddressController
import controllers.register.establishers.company.director.routes._
import forms.address.AddressFormProvider
import identifiers.register.establishers.company.director.{DirectorAddressId, DirectorAddressListId, DirectorAddressPostcodeLookupId, DirectorDetailsId}
import javax.inject.Inject
import models.address.Address
import models.{Index, Mode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import utils.annotations.EstablishersCompanyDirector
import utils.{CountryOptions, Navigator}
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel

import scala.concurrent.ExecutionContext

class DirectorAddressController @Inject()(
                                           val appConfig: FrontendAppConfig,
                                           val messagesApi: MessagesApi,
                                           val userAnswersService: UserAnswersService,
                                           @EstablishersCompanyDirector val navigator: Navigator,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           allowAccess: AllowAccessActionProvider,
                                           requireData: DataRequiredAction,
                                           val formProvider: AddressFormProvider,
                                           val countryOptions: CountryOptions,
                                           val auditService: AuditService
                                         )(implicit val ec: ExecutionContext) extends ManualAddressController with I18nSupport {

  private[controllers] val postCall = DirectorAddressController.onSubmit _
  private[controllers] val title: Message = "messages__directorAddressPostcodeLookup__title"
  private[controllers] val heading: Message = "messages__directorAddressPostcodeLookup__heading"
  private[controllers] val hint: Message = "messages__directorAddressPostcodeLookup__lede"

  protected val form: Form[Address] = formProvider()

  def onPageLoad(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
    implicit request =>
      viewmodel(establisherIndex, directorIndex, mode, srn).retrieve.right.map {
        vm =>
          get(DirectorAddressId(establisherIndex, directorIndex), DirectorAddressListId(establisherIndex, directorIndex), vm)
      }
  }

  def onSubmit(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      viewmodel(establisherIndex, directorIndex, mode, srn).retrieve.right.map {
        vm =>
          post(
            DirectorAddressId(establisherIndex, directorIndex),
            DirectorAddressListId(establisherIndex, directorIndex),
            vm,
            mode,
            context(vm),
            DirectorAddressPostcodeLookupId(establisherIndex, directorIndex)
          )
      }
  }

  private def viewmodel(establisherIndex: Int, directorIndex: Int, mode: Mode, srn: Option[String]): Retrieval[ManualAddressViewModel] =
    Retrieval {
      implicit request =>
        DirectorDetailsId(establisherIndex, directorIndex).retrieve.right.map {
          details =>
            ManualAddressViewModel(
              postCall(mode, Index(establisherIndex), Index(directorIndex), srn),
              countryOptions.options,
              title = Message(title),
              heading = Message(heading),
              hint = Some(Message(hint)),
              secondaryHeader = Some(details.fullName),
              srn = srn
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
