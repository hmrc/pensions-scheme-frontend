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

import audit.AuditService
import config.FrontendAppConfig
import controllers.actions._
import controllers.address.ManualAddressController
import controllers.register.trustees.individual.routes._
import forms.address.AddressFormProvider
import identifiers.register.trustees.individual._
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

class TrusteePreviousAddressController @Inject()(
                                                  override val appConfig: FrontendAppConfig,
                                                  override val messagesApi: MessagesApi,
                                                  val userAnswersService: UserAnswersService,
                                                  override val navigator: Navigator,
                                                  authenticate: AuthAction,
                                                  getData: DataRetrievalAction,
                                                  allowAccess: AllowAccessActionProvider,
                                                  requireData: DataRequiredAction,
                                                  formProvider: AddressFormProvider,
                                                  val countryOptions: CountryOptions,
                                                  val auditService: AuditService
                                                )(implicit val ec: ExecutionContext) extends ManualAddressController with I18nSupport {

  private[controllers] val postCall = TrusteePreviousAddressController.onSubmit _

  protected val form: Form[Address] = formProvider()

  private def viewmodel(index: Int, mode: Mode, srn: Option[String], name: String): ManualAddressViewModel =
    ManualAddressViewModel(
      postCall(mode, Index(index), srn),
      countryOptions.options,
      title = Message("messages__common__confirmPreviousAddress__h1",Message("messages__theIndividual")),
      heading = Message("messages__common__confirmPreviousAddress__h1", name),
      srn = srn
    )

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        trusteeName(index).retrieve.right.map {
          name =>
            get(
              id = TrusteePreviousAddressId(index),
              selectedId = TrusteePreviousAddressListId(index),
              viewModel = viewmodel(index, mode, srn, name)
            )
        }
    }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      trusteeName(index).retrieve.right.map {
        name =>
          post(
            id = TrusteePreviousAddressId(index),
            selectedId = TrusteePreviousAddressListId(index),
            viewModel = viewmodel(index, mode, srn, name),
            mode = mode,
            context = s"Trustee Individual Previous Address: $name",
            postCodeLookupIdForCleanup = IndividualPreviousAddressPostCodeLookupId(index)
          )
      }
  }

  val trusteeName: Index => Retrieval[String] = (trusteeIndex: Index) => Retrieval {
    implicit request =>
        TrusteeNameId(trusteeIndex).retrieve.right.map(_.fullName)
  }
}
