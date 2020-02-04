/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers.register.establishers.company.director

import audit.AuditService
import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import controllers.address.ManualAddressController
import forms.address.AddressFormProvider
import identifiers.register.establishers.company.director._
import javax.inject.Inject
import models.address.Address
import models.{Index, Mode}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import utils.CountryOptions
import utils.annotations.EstablishersCompanyDirector
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel

import scala.concurrent.ExecutionContext

class DirectorPreviousAddressController @Inject()(
                                                   val appConfig: FrontendAppConfig,
                                                   override val messagesApi: MessagesApi,
                                                   val userAnswersService: UserAnswersService,
                                                   @EstablishersCompanyDirector val navigator: Navigator,
                                                   authenticate: AuthAction,
                                                   getData: DataRetrievalAction,
                                                   allowAccess: AllowAccessActionProvider,
                                                   requireData: DataRequiredAction,
                                                   val formProvider: AddressFormProvider,
                                                   val countryOptions: CountryOptions,
                                                   val auditService: AuditService
                                                 )(implicit val ec: ExecutionContext) extends ManualAddressController with I18nSupport with Retrievals {

  protected val form: Form[Address] = formProvider()
  private[controllers] val postCall = routes.DirectorPreviousAddressController.onSubmit _
  private[controllers] val title: Message = "messages__common__confirmPreviousAddress__h1"
  private[controllers] val heading: Message = "messages__common__confirmPreviousAddress__h1"

  def onPageLoad(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        directorName(establisherIndex, directorIndex).retrieve.right.map {
          name =>
            get(DirectorPreviousAddressId(establisherIndex, directorIndex), DirectorPreviousAddressListId(establisherIndex, directorIndex),
              viewmodel(mode, establisherIndex, directorIndex, srn, name))
        }
    }

  private def viewmodel(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: Option[String], name: String): ManualAddressViewModel =
    ManualAddressViewModel(
      postCall(mode, establisherIndex, directorIndex, srn),
      countryOptions.options,
      title = Message(title, Message("messages__common__address_years__director")),
      heading = Message(heading, name),
      srn = srn
    )

  private val directorName = (establisherIndex: Index, directorIndex: Index) => Retrieval {
    implicit request =>
        DirectorNameId(establisherIndex, directorIndex).retrieve.right.map(_.fullName)
  }

  def onSubmit(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        directorName(establisherIndex, directorIndex).retrieve.right.map {
          name =>
            val context = s"Company Director Previous Address: $name"
            post(
              DirectorPreviousAddressId(establisherIndex, directorIndex),
              DirectorPreviousAddressListId(establisherIndex, directorIndex),
              viewmodel(mode, establisherIndex, directorIndex, srn, name),
              mode,
              context,
              DirectorPreviousAddressPostcodeLookupId(establisherIndex, directorIndex)
            )
        }
    }

}
