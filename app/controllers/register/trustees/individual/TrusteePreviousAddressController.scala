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
import utils.annotations.TrusteesIndividual
import utils.CountryOptions
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel

import scala.concurrent.ExecutionContext

class TrusteePreviousAddressController @Inject()(
                                                  override val appConfig: FrontendAppConfig,
                                                  override val messagesApi: MessagesApi,
                                                  val userAnswersService: UserAnswersService,
                                                  @TrusteesIndividual override val navigator: Navigator,
                                                  authenticate: AuthAction,
                                                  getData: DataRetrievalAction,
                                                  allowAccess: AllowAccessActionProvider,
                                                  requireData: DataRequiredAction,
                                                  formProvider: AddressFormProvider,
                                                  val countryOptions: CountryOptions,
                                                  val auditService: AuditService
                                                )(implicit val ec: ExecutionContext) extends ManualAddressController with I18nSupport {

  private[controllers] val postCall = TrusteePreviousAddressController.onSubmit _
  private[controllers] val title: Message = "messages__trustee_individual_previous_address__title"
  private[controllers] val heading: Message = "messages__trustee_individual_previous_address__heading"

  protected val form: Form[Address] = formProvider()

  private def viewmodel(index: Int, mode: Mode, srn: Option[String]): Retrieval[ManualAddressViewModel] =
    Retrieval {
      implicit request =>
        TrusteeDetailsId(index).retrieve.right.map {
          details =>
            ManualAddressViewModel(
              postCall(mode, Index(index), srn),
              countryOptions.options,
              title = Message(title),
              heading = Message(heading),
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
          get(TrusteePreviousAddressId(index), TrusteePreviousAddressListId(index), vm)
      }
  }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      viewmodel(index, mode, srn).retrieve.right.map {
        vm =>
          post(TrusteePreviousAddressId(index), TrusteePreviousAddressListId(index), vm, mode, context(vm),
            IndividualPreviousAddressPostCodeLookupId(index)
          )
      }
  }

  private def context(viewModel: ManualAddressViewModel): String = {
    viewModel.secondaryHeader match {
      case Some(fullName) => s"Trustee Individual Previous Address: $fullName"
      case _ => "Trustee Individual Previous Address"
    }
  }

}
