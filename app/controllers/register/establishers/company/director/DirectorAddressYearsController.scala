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

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import controllers.address.AddressYearsController
import forms.address.AddressYearsFormProvider
import identifiers.register.establishers.company.director.{DirectorAddressYearsId, DirectorNameId}
import javax.inject.Inject
import models.requests.DataRequest
import models.{Index, Mode}
import navigators.Navigator
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import utils.annotations.EstablishersCompanyDirector
import viewmodels.Message
import viewmodels.address.AddressYearsViewModel
import views.html.address.addressYears

import scala.concurrent.ExecutionContext

class DirectorAddressYearsController @Inject()(val appConfig: FrontendAppConfig,
                                               val userAnswersService: UserAnswersService,
                                               @EstablishersCompanyDirector val navigator: Navigator,
                                               override val messagesApi: MessagesApi,
                                               authenticate: AuthAction,
                                               getData: DataRetrievalAction,
                                               allowAccess: AllowAccessActionProvider,
                                               requireData: DataRequiredAction,
                                               val controllerComponents: MessagesControllerComponents,
                                               val view: addressYears
                                              )(implicit val ec: ExecutionContext) extends AddressYearsController
  with Retrievals {

  private val directorName = (establisherIndex: Index, directorIndex: Index) => Retrieval {
    implicit request =>
      DirectorNameId(establisherIndex, directorIndex).retrieve.right.map(_.fullName)
  }

  def onPageLoad(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        directorName(establisherIndex, directorIndex).retrieve.right.map { name =>
          get(DirectorAddressYearsId(establisherIndex, directorIndex), form(name),
            viewModel(mode, establisherIndex, directorIndex, name, srn))
        }
    }

  def onSubmit(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        directorName(establisherIndex, directorIndex).retrieve.right.map { name =>
          post(
            DirectorAddressYearsId(establisherIndex, directorIndex),
            mode,
            form(name),
            viewModel(mode, establisherIndex, directorIndex, name, srn)
          )
        }
    }

  private def form(directorName: String)(implicit request: DataRequest[AnyContent]) =
    new AddressYearsFormProvider()(Message("messages__director_address_years__form_error", directorName))

  private def viewModel(mode: Mode, establisherIndex: Index, directorIndex: Index, directorName: String,
                        srn: Option[String])
                       (implicit request: DataRequest[AnyContent]) =
    AddressYearsViewModel(
      postCall = routes.DirectorAddressYearsController.onSubmit(mode, establisherIndex, directorIndex, srn),
      title = Message("messages__director_address_years__title", Message("messages__common__address_years__director")),
      heading = Message("messages__director_address_years__heading", directorName),
      legend = Message("messages__director_address_years__heading", directorName),
      subHeading = Some(Message(directorName)),
      srn = srn
    )
}
