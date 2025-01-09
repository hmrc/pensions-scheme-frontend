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

import audit.AuditService
import config.FrontendAppConfig
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.ManualAddressController
import controllers.register.trustees.individual.routes.TrusteeAddressController
import forms.address.AddressFormProvider
import identifiers.register.trustees.individual.{IndividualAddressListId, IndividualPostCodeLookupId, TrusteeAddressId, TrusteeNameId}
import javax.inject.Inject
import models.address.Address
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

class TrusteeAddressController @Inject()(
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

  val trusteeName: Index => Retrieval[String] = (trusteeIndex: Index) => Retrieval {
    implicit request =>
      TrusteeNameId(trusteeIndex).retrieve.map(_.fullName)
  }
  protected val form: Form[Address] = formProvider()
  private[controllers] val postCall = TrusteeAddressController.onSubmit _

  def onPageLoad(mode: Mode, index: Index, srn: Option[SchemeReferenceNumber]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        trusteeName(index).retrieve.map {
          name =>
            get(TrusteeAddressId(index), IndividualAddressListId(index), viewmodel(index, mode, srn, name))
        }
    }

  private def viewmodel(index: Int, mode: Mode, srn: Option[SchemeReferenceNumber], name: String): ManualAddressViewModel =
    ManualAddressViewModel(
      postCall(mode, Index(index), srn),
      countryOptions.options,
      title = Message("messages__common__confirmAddress__h1", Message("messages__theTrustee")),
      heading = Message("messages__common__confirmAddress__h1", name),
      srn = srn
    )

  def onSubmit(mode: Mode, index: Index, srn: Option[SchemeReferenceNumber]): Action[AnyContent] = (authenticate() andThen getData
  (mode, srn) andThen requireData).async {
    implicit request =>
      trusteeName(index).retrieve.map {
        name =>
          post(TrusteeAddressId(index), IndividualAddressListId(index), viewmodel(index, mode, srn, name), mode,
            context(name),
            IndividualPostCodeLookupId(index))
      }
  }

  private def context(fullName: String): String = s"Trustee Individual Address: $fullName"

}
