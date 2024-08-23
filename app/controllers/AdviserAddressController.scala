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

package controllers

import audit.AuditService
import config.FrontendAppConfig
import controllers.actions._
import controllers.address.ManualAddressController
import controllers.routes._
import forms.address.AddressFormProvider
import identifiers.{AdviserAddressId, AdviserAddressListId, AdviserAddressPostCodeLookupId, AdviserNameId}

import javax.inject.Inject
import models.{Mode, SchemeReferenceNumber}
import models.address.Address
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import utils.CountryOptions
import utils.annotations.WorkingKnowledge
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.manualAddress

import scala.concurrent.ExecutionContext

class AdviserAddressController @Inject()(
                                          val appConfig: FrontendAppConfig,
                                          override val messagesApi: MessagesApi,
                                          val userAnswersService: UserAnswersService,
                                          @WorkingKnowledge val navigator: Navigator,
                                          authenticate: AuthAction,
                                          getData: DataRetrievalAction,
                                          requireData: DataRequiredAction,
                                          val formProvider: AddressFormProvider,
                                          val countryOptions: CountryOptions,
                                          val auditService: AuditService,
                                          val controllerComponents: MessagesControllerComponents,
                                          val view: manualAddress
                                        )(implicit val ec: ExecutionContext) extends ManualAddressController with
  I18nSupport {

  protected val form: Form[Address] = formProvider()
  private[controllers] val postCall = AdviserAddressController.onSubmit _
  private[controllers] val title: Message = "messages__confirmAdviserAddress__title"
  private[controllers] val secondary: Message = "messages__adviserAddress__secondary"
  private[controllers] val hint = None

  def onPageLoad(mode: Mode, srn: SchemeReferenceNumber): Action[AnyContent] = (authenticate() andThen getData(srn=srn) andThen requireData).async {
    implicit request =>
      AdviserNameId.retrieve.map { adviserName =>
        get(AdviserAddressId, AdviserAddressListId, viewmodel(mode, adviserName, srn))
      }
  }

  private def viewmodel(mode: Mode, adviserName: String, srn: SchemeReferenceNumber): ManualAddressViewModel =
    ManualAddressViewModel(
      postCall(mode, srn),
      countryOptions.options,
      title = title,
      heading = heading(adviserName),
      srn
    )

  private[controllers] def heading(adviserName: String): Message =
    Message("messages__common__confirmAddress__h1", adviserName)

  def onSubmit(mode: Mode, srn: SchemeReferenceNumber): Action[AnyContent] = (authenticate() andThen getData(srn=srn) andThen requireData).async {
    implicit request =>
      AdviserNameId.retrieve.map { adviserName =>
        post(AdviserAddressId, AdviserAddressListId, viewmodel(mode, adviserName, srn), mode, "Adviser Address",
          AdviserAddressPostCodeLookupId)
      }
  }
}
