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

package controllers.register.establishers.company.director

import audit.AuditService
import config.FrontendAppConfig
import controllers.actions._
import controllers.address.ManualAddressController
import controllers.register.establishers.company.director.routes._
import forms.address.AddressFormProvider
import identifiers.register.establishers.company.director._

import javax.inject.Inject
import models.address.Address
import models.requests.DataRequest
import models.{Index, Mode, SchemeReferenceNumber}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import utils.CountryOptions
import utils.annotations.EstablishersCompanyDirector
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.manualAddress

import scala.concurrent.ExecutionContext

class DirectorAddressController @Inject()(
                                           val appConfig: FrontendAppConfig,
                                           val userAnswersService: UserAnswersService,
                                           @EstablishersCompanyDirector val navigator: Navigator,
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
  private[controllers] val postCall = DirectorAddressController.onSubmit _
  private[controllers] val title: Message = "messages__common__confirmAddress__h1"
  private[controllers] val heading: Message = "messages__common__confirmAddress__h1"
  private[controllers] val hint: Message = "messages__directorAddressPostcodeLookup__lede"
  private val directorName = (establisherIndex: Index, directorIndex: Index) => Retrieval {
    implicit request =>
      DirectorNameId(establisherIndex, directorIndex).retrieve.map(_.fullName)
  }

  def onPageLoad(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: SchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        directorName(establisherIndex, directorIndex).retrieve.map {
          name =>
            get(DirectorAddressId(establisherIndex, directorIndex), DirectorAddressListId(establisherIndex,
              directorIndex),
              viewmodel(establisherIndex, directorIndex, mode, srn, name))
        }
    }

  def onSubmit(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: SchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        directorName(establisherIndex, directorIndex).retrieve.map {
          name =>
            post(
              DirectorAddressId(establisherIndex, directorIndex),
              DirectorAddressListId(establisherIndex, directorIndex),
              viewmodel(establisherIndex, directorIndex, mode, srn, name),
              mode,
              context = s"Company Director Address: $name",
              DirectorAddressPostcodeLookupId(establisherIndex, directorIndex)
            )
        }
    }

  private def viewmodel(establisherIndex: Int, directorIndex: Int, mode: Mode, srn: SchemeReferenceNumber, name: String)
                       (implicit request: DataRequest[AnyContent]): ManualAddressViewModel =
    ManualAddressViewModel(
      postCall(mode, Index(establisherIndex), Index(directorIndex), srn),
      countryOptions.options,
      title = Message(title, Message("messages__theDirector")),
      heading = Message(heading, name),
      srn = srn
    )

}
