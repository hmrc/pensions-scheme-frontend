/*
 * Copyright 2018 HM Revenue & Customs
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
import connectors.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions._
import controllers.address.AddressYearsController
import forms.address.AddressYearsFormProvider
import identifiers.register.establishers.company.director.{DirectorAddressYearsId, DirectorDetailsId}
import javax.inject.Inject
import models.{Index, Mode}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.EstablishersCompanyDirector
import viewmodels.Message
import viewmodels.address.AddressYearsViewModel

import scala.concurrent.ExecutionContext

class DirectorAddressYearsController @Inject()(
                                                val appConfig: FrontendAppConfig,
                                                val cacheConnector: UserAnswersCacheConnector,
                                                @EstablishersCompanyDirector val navigator: Navigator,
                                                val messagesApi: MessagesApi,
                                                authenticate: AuthAction,
                                                getData: DataRetrievalAction,
                                                requireData: DataRequiredAction
                                              ) (implicit val ec: ExecutionContext) extends AddressYearsController with Retrievals {

  private val form = new AddressYearsFormProvider()(Message("messages__common_error__current_address_years"))

  def onPageLoad(mode: Mode, establisherIndex: Index, directorIndex: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      DirectorDetailsId(establisherIndex, directorIndex).retrieve.right.map { directorDetails =>
        get(DirectorAddressYearsId(establisherIndex, directorIndex), form, viewModel(mode, establisherIndex, directorIndex, directorDetails.fullName))
      }
  }

  def onSubmit(mode: Mode, establisherIndex: Index, directorIndex: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      DirectorDetailsId(establisherIndex, directorIndex).retrieve.right.map { directorDetails =>
        post(
          DirectorAddressYearsId(establisherIndex, directorIndex),
          mode,
          form,
          viewModel(mode, establisherIndex, directorIndex, directorDetails.fullName)
        )
      }
  }

  private def viewModel(mode: Mode, establisherIndex: Index, directorIndex: Index, directorName: String) = AddressYearsViewModel(
    postCall = routes.DirectorAddressYearsController.onSubmit(mode, establisherIndex, directorIndex),
    title = Message("messages__director_address_years__title"),
    heading = Message("messages__director_address_years__heading"),
    legend = Message("messages__director_address_years__heading"),
    subHeading = Some(Message(directorName))
  )
}
