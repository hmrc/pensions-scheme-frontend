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

package controllers.register.establishers.individual

import audit.AuditService
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.actions._
import controllers.address.ManualAddressController
import forms.address.AddressFormProvider
import identifiers.register.establishers.individual.{AddressId, AddressListId, EstablisherDetailsId, PostCodeLookupId}
import javax.inject.Inject
import models.address.Address
import models.{Index, Mode}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import utils.annotations.EstablishersIndividual
import utils.CountryOptions
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel

import scala.concurrent.ExecutionContext

class AddressController @Inject()(
                                   val appConfig: FrontendAppConfig,
                                   val messagesApi: MessagesApi,
                                   val userAnswersService: UserAnswersService,
                                   @EstablishersIndividual val navigator: Navigator,
                                   authenticate: AuthAction,
                                   getData: DataRetrievalAction,
                                   allowAccess: AllowAccessActionProvider,
                                   requireData: DataRequiredAction,
                                   val formProvider: AddressFormProvider,
                                   val countryOptions: CountryOptions,
                                   val auditService: AuditService
                                 )(implicit val ec: ExecutionContext) extends ManualAddressController with I18nSupport {

  private[controllers] val postCall = routes.AddressController.onSubmit _
  private[controllers] val title: Message = "messages__establisher_individual_address__title"
  private[controllers] val heading: Message = "messages__common__confirmAddress__h1"
  private[controllers] val hint: Message = "messages__establisher_individual_address_lede"

  protected val form: Form[Address] = formProvider()

  private def viewmodel(index: Int, mode: Mode, srn: Option[String]): Retrieval[ManualAddressViewModel] =
    Retrieval {
      implicit request =>
        EstablisherDetailsId(index).retrieve.right.map {
          details =>
            ManualAddressViewModel(
              postCall(mode, Index(index), srn),
              countryOptions.options,
              title = Message(title),
              heading = Message(heading,details.fullName),
              hint = Some(Message(hint)),
              secondaryHeader = Some(details.fullName),
              srn = srn
            )
        }
    }

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
    implicit request =>
      viewmodel(index, mode, srn).retrieve.right.map {
        vm =>
          get(AddressId(index), AddressListId(index), vm)
      }
  }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      viewmodel(index, mode, srn).retrieve.right.map {
        vm =>
          post(AddressId(index), AddressListId(index), vm, mode, context(vm),
            PostCodeLookupId(index)
          )
      }
  }

  def onClick(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      clear(AddressId(index), AddressListId(index), mode, srn, routes.AddressController.onPageLoad(mode, index, srn))
  }

  private def context(viewModel: ManualAddressViewModel): String = {
    viewModel.secondaryHeader match {
      case Some(name) => s"Establisher Individual Address: $name"
      case _ => "Establisher Individual Address"
    }
  }

}
