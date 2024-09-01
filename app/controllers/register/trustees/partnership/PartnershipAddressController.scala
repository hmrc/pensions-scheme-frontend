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

package controllers.register.trustees.partnership

import audit.AuditService
import config.FrontendAppConfig
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.ManualAddressController
import forms.address.AddressFormProvider
import identifiers.register.trustees.partnership.{PartnershipAddressId, PartnershipAddressListId, PartnershipDetailsId, PartnershipPostcodeLookupId}
import javax.inject.Inject
import models.address.Address
import models.requests.DataRequest
import models.{Index, Mode}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import utils.CountryOptions
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.manualAddress

import scala.concurrent.ExecutionContext
import models.SchemeReferenceNumber
class PartnershipAddressController @Inject()(
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
                                              val controllerComponents: MessagesControllerComponents,
                                              val view: manualAddress
                                            )(implicit val ec: ExecutionContext) extends ManualAddressController with
  I18nSupport {

  protected val form: Form[Address] = formProvider()
  private[controllers] val postCall = routes.PartnershipAddressController.onSubmit _
  private[controllers] val title: Message = "messages__common__confirmAddress__h1"
  private[controllers] val heading: Message = "messages__common__confirmAddress__h1"
  private[controllers] val hint: Message = "messages__trusteePartnershipAddress__lede"

  def onPageLoad(mode: Mode, index: Index, srn: SchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData() andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        PartnershipDetailsId(index).retrieve.map {
          details =>
            get(PartnershipAddressId(index), PartnershipAddressListId(index), viewmodel(index, mode, srn, details.name))
        }
    }

  private def viewmodel(index: Int, mode: Mode, srn: SchemeReferenceNumber, name: String
                       )(implicit request: DataRequest[AnyContent]): ManualAddressViewModel =
    ManualAddressViewModel(
      postCall(mode, Index(index), srn),
      countryOptions.options,
      title = Message(title, Message("messages__thePartnership")),
      heading = Message(heading, name),
      srn = srn
    )

  def onSubmit(mode: Mode, index: Index, srn: SchemeReferenceNumber): Action[AnyContent] = (authenticate() andThen getData
  () andThen requireData).async {
    implicit request =>
      PartnershipDetailsId(index).retrieve.map {
        details =>
          val context = s"Trustee Partnership Address: ${details.name}"
          post(PartnershipAddressId(index), PartnershipAddressListId(index),
            viewmodel(index, mode, srn, details.name), mode, context,
            PartnershipPostcodeLookupId(index)
          )
      }
  }
}
