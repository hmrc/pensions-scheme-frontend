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

package controllers.register.trustees.company

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import controllers.address.ConfirmPreviousAddressController
import identifiers.register.trustees.company.{CompanyConfirmPreviousAddressId, CompanyDetailsId, CompanyPreviousAddressId}
import identifiers.register.trustees.ExistingCurrentAddressId
import javax.inject.Inject
import models.{Index, Mode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import utils.annotations.TrusteesCompany
import utils.{CountryOptions, Navigator}
import viewmodels.Message
import viewmodels.address.ConfirmAddressViewModel

class CompanyConfirmPreviousAddressController @Inject()(val appConfig: FrontendAppConfig,
                                                           val messagesApi: MessagesApi,
                                                           val userAnswersService: UserAnswersService,
                                                           @TrusteesCompany val navigator: Navigator,
                                                           authenticate: AuthAction,
                                                           allowAccess: AllowAccessActionProvider,
                                                           getData: DataRetrievalAction,
                                                           requireData: DataRequiredAction,
                                                           val countryOptions: CountryOptions
                                                          ) extends ConfirmPreviousAddressController with Retrievals with I18nSupport {

  private[controllers] val postCall = routes.CompanyConfirmPreviousAddressController.onSubmit _
  private[controllers] val title: Message = "messages__confirmPreviousAddress__title"
  private[controllers] val heading: Message = "messages__confirmPreviousAddress__heading"

  private def viewmodel(mode: Mode, index: Int, srn: Option[String]) =
    Retrieval(
      implicit request =>
        (CompanyDetailsId(index) and ExistingCurrentAddressId(index)).retrieve.right.map {
          case details ~ address =>
            ConfirmAddressViewModel(
              postCall(index, srn),
              title = Message(title),
              heading = Message(heading, details.companyName),
              hint = None,
              address = address,
              name = details.companyName,
              srn = srn
            )
        }
    )

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        viewmodel(mode, index, srn).retrieve.right.map { vm =>
          get(CompanyConfirmPreviousAddressId(index), vm)
        }
    }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        viewmodel(mode, index, srn).retrieve.right.map { vm =>
          post(CompanyConfirmPreviousAddressId(index), CompanyPreviousAddressId(index), vm, mode)
        }
    }
}