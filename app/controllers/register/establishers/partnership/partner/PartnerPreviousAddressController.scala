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
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.ManualAddressController
import forms.address.AddressFormProvider
import identifiers.register.establishers.partnership.partner._
import javax.inject.Inject
import models.address.Address
import models.{Index, Mode}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import utils.CountryOptions
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel

import scala.concurrent.ExecutionContext

class PartnerPreviousAddressController @Inject()(
                                                  val appConfig: FrontendAppConfig,
                                                  val messagesApi: MessagesApi,
                                                  val userAnswersService: UserAnswersService,
                                                  val navigator: Navigator,
                                                  authenticate: AuthAction,
                                                  getData: DataRetrievalAction,
                                                  allowAccess: AllowAccessActionProvider,
                                                  requireData: DataRequiredAction,
                                                  val formProvider: AddressFormProvider,
                                                  val countryOptions: CountryOptions,
                                                  val auditService: AuditService
                                                )(implicit val ec: ExecutionContext) extends ManualAddressController with I18nSupport with Retrievals {

  protected val form: Form[Address] = formProvider()
  private[controllers] val postCall = routes.PartnerPreviousAddressController.onSubmit _
  private[controllers] val title: Message = "messages__partnerPreviousAddress__title"
  private[controllers] val heading: Message = "messages__common__confirmPreviousAddress__h1"

  def onPageLoad(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        PartnerNameId(establisherIndex, partnerIndex).retrieve.right.map {
          partner =>
            get(PartnerPreviousAddressId(establisherIndex, partnerIndex),
              PartnerPreviousAddressListId(establisherIndex, partnerIndex),
              viewmodel(mode, establisherIndex, partnerIndex, srn, partner.fullName))
        }
    }

  def onSubmit(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        PartnerNameId(establisherIndex, partnerIndex).retrieve.right.map {
          partner =>
            val context = s"Partnership Partner Previous Address: ${partner.fullName}"
            post(
              PartnerPreviousAddressId(establisherIndex, partnerIndex),
              PartnerPreviousAddressListId(establisherIndex, partnerIndex),
              viewmodel(mode, establisherIndex, partnerIndex, srn, partner.fullName),
              mode,
              context,
              PartnerPreviousAddressPostcodeLookupId(establisherIndex, partnerIndex)
            )
        }
    }

  private def viewmodel(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: Option[String], name: String): ManualAddressViewModel =
    ManualAddressViewModel(
      postCall(mode, establisherIndex, partnerIndex, srn),
      countryOptions.options,
      title = Message(title),
      heading = Message(heading, name),
      srn = srn
    )

}
