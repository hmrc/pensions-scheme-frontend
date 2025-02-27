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

import audit.AuditService
import config.FrontendAppConfig
import controllers.actions._
import controllers.address.ManualAddressController
import forms.address.AddressFormProvider
import identifiers.register.establishers.individual.{AddressId, AddressListId, EstablisherNameId, PostCodeLookupId}
import models.address.Address
import models.requests.DataRequest
import models.{Index, Mode, OptionalSchemeReferenceNumber}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import utils.CountryOptions
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.manualAddress

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class AddressController @Inject()(
                                   val appConfig: FrontendAppConfig,
                                   override val messagesApi: MessagesApi,
                                   val userAnswersService: UserAnswersService,
                                   val navigator: Navigator,
                                   authenticate: AuthAction,
                                   getData: DataRetrievalAction,
                                   allowAccess: AllowAccessActionProvider,
                                   requireData: DataRequiredAction,
                                   val formProvider: AddressFormProvider,
                                   val countryOptions: CountryOptions,
                                   val auditService: AuditService,
                                   val view: manualAddress,
                                   val controllerComponents: MessagesControllerComponents
                                 )(implicit val ec: ExecutionContext) extends ManualAddressController with I18nSupport {

  protected val form: Form[Address] = formProvider()
  private[controllers] val postCall = routes.AddressController.onSubmit _
  private[controllers] val heading: Message = "messages__common__confirmAddress__h1"
  private[controllers] val hint: Message = "messages__establisher_individual_address_lede"

  def onPageLoad(mode: Mode, index: Index, srn: OptionalSchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        EstablisherNameId(index).retrieve.map {
          details =>
            get(AddressId(index), AddressListId(index), viewmodel(index, mode, srn, details.fullName))
        }
    }

  def onSubmit(mode: Mode, index: Index, srn: OptionalSchemeReferenceNumber): Action[AnyContent] = (authenticate() andThen getData
  (mode, srn) andThen requireData).async {
    implicit request =>
      EstablisherNameId(index).retrieve.map {
        details =>
          val context = s"Establisher Individual Address: ${details.fullName}"
          post(AddressId(index), AddressListId(index),
            viewmodel(index, mode, srn, details.fullName), mode, context,
            PostCodeLookupId(index)
          )
      }
  }

  private def viewmodel(index: Int, mode: Mode, srn: OptionalSchemeReferenceNumber, name: String)
                       (implicit request: DataRequest[AnyContent]): ManualAddressViewModel =
    ManualAddressViewModel(
      postCall(mode, Index(index), srn),
      countryOptions.options,
      title = Message(heading, Message("messages__theIndividual")),
      heading = Message(heading, name),
      srn = srn
    )
}
