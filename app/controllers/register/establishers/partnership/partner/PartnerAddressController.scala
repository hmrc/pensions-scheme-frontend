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

package controllers.register.establishers.partnership.partner

import audit.AuditService
import config.FrontendAppConfig
import controllers.actions._
import controllers.address.ManualAddressController
import controllers.register.establishers.partnership.partner.routes._
import forms.address.AddressFormProvider
import identifiers.register.establishers.partnership.partner.{PartnerAddressId, PartnerAddressListId, PartnerAddressPostcodeLookupId, PartnerDetailsId}
import javax.inject.Inject
import models.address.Address
import models.{Index, Mode}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import utils.CountryOptions
import utils.annotations.EstablishersPartner
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel

import scala.concurrent.ExecutionContext

class PartnerAddressController @Inject()(
                                          val appConfig: FrontendAppConfig,
                                          val messagesApi: MessagesApi,
                                          val userAnswersService: UserAnswersService,
                                          @EstablishersPartner val navigator: Navigator,
                                          authenticate: AuthAction,
                                          getData: DataRetrievalAction,
                                          allowAccess: AllowAccessActionProvider,
                                          requireData: DataRequiredAction,
                                          val formProvider: AddressFormProvider,
                                          val countryOptions: CountryOptions,
                                          val auditService: AuditService
                                        )(implicit val ec: ExecutionContext) extends ManualAddressController with I18nSupport {

  private[controllers] val postCall = PartnerAddressController.onSubmit _
  private[controllers] val title: Message = "messages__partnerAddress__title"
  private[controllers] val heading: Message = "messages__common__confirmAddress__h1"
  private[controllers] val hint: Message = "messages__partnerAddress__lede"

  protected val form: Form[Address] = formProvider()

  def onPageLoad(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        PartnerDetailsId(establisherIndex, partnerIndex).retrieve.right.map {
          details =>
            get(PartnerAddressId(establisherIndex, partnerIndex),
              PartnerAddressListId(establisherIndex, partnerIndex),
              viewmodel(establisherIndex, partnerIndex, mode, srn, details.fullName))
        }
    }

  def onSubmit(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        PartnerDetailsId(establisherIndex, partnerIndex).retrieve.right.map {
          details =>
            val context = s"Company Partner Address: ${details.fullName}"
            post(
              PartnerAddressId(establisherIndex, partnerIndex),
              PartnerAddressListId(establisherIndex, partnerIndex),
              viewmodel(establisherIndex, partnerIndex, mode, srn, details.fullName),
              mode,
              context,
              PartnerAddressPostcodeLookupId(establisherIndex, partnerIndex)
            )
        }
    }

  private def viewmodel(establisherIndex: Int, partnerIndex: Int, mode: Mode, srn: Option[String], name: String): ManualAddressViewModel =
    ManualAddressViewModel(
      postCall(mode, Index(establisherIndex), Index(partnerIndex), srn),
      countryOptions.options,
      title = Message(title),
      heading = Message(heading, name),
      srn = srn
    )

}
