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

package controllers.register.establishers.company

import audit.AuditService
import config.FrontendAppConfig
import controllers.actions._
import controllers.address.ManualAddressController
import forms.address.AddressFormProvider
import identifiers.register.establishers.company.{CompanyDetailsId, CompanyPreviousAddressId, CompanyPreviousAddressListId, CompanyPreviousAddressPostcodeLookupId}
import javax.inject.Inject
import models.address.Address
import models.{Index, Mode}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import utils.annotations.EstablishersCompany
import utils.CountryOptions
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel

import scala.concurrent.ExecutionContext

class CompanyPreviousAddressController @Inject()(
                                                  val appConfig: FrontendAppConfig,
                                                  val messagesApi: MessagesApi,
                                                  val userAnswersService: UserAnswersService,
                                                  @EstablishersCompany val navigator: Navigator,
                                                  authenticate: AuthAction,
                                                  getData: DataRetrievalAction,
                                                  allowAccess: AllowAccessActionProvider,
                                                  requireData: DataRequiredAction,
                                                  formProvider: AddressFormProvider,
                                                  val countryOptions: CountryOptions,
                                                  val auditService: AuditService
                                                )(implicit val ec: ExecutionContext) extends ManualAddressController with I18nSupport {

  protected val form: Form[Address] = formProvider()
  private[controllers] val postCall = routes.CompanyPreviousAddressController.onSubmit _
  private[controllers] val title: Message = "messages__common__confirmPreviousAddress__h1"
  private[controllers] val heading: Message = "messages__common__confirmPreviousAddress__h1"
  private[controllers] val hint: Message = "messages__companyAddress__lede"

  def onPageLoad(mode: Mode, srn: Option[String], index: Index): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        CompanyDetailsId(index).retrieve.right.map {
          details =>
            get(CompanyPreviousAddressId(index), CompanyPreviousAddressListId(index), viewmodel(index, srn, mode, details.companyName))
        }
    }

  def onSubmit(mode: Mode, srn: Option[String], index: Index): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      CompanyDetailsId(index).retrieve.right.map {
        details =>
          val context = s"Establisher Company Previous Address: ${details.companyName}"
          post(CompanyPreviousAddressId(index), CompanyPreviousAddressListId(index),
            viewmodel(index, srn, mode, details.companyName), mode, context, CompanyPreviousAddressPostcodeLookupId(index))
      }
  }

  private def viewmodel(index: Int, srn: Option[String], mode: Mode, name: String): ManualAddressViewModel =
    ManualAddressViewModel(
      postCall(mode, srn, Index(index)),
      countryOptions.options,
      title = Message(title, Message("messages__theEstablisher")),
      heading = Message(heading, name),
      srn = srn
    )

}
