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

package controllers.register.establishers.individual

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions._
import controllers.address.{AddressYearsController => GenericAddressYearController}
import forms.address.AddressYearsFormProvider
import identifiers.register.establishers.individual.{AddressYearsId, EstablisherDetailsId}
import javax.inject.Inject
import models.{Index, Mode}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils._
import utils.annotations.EstablishersIndividual
import viewmodels.Message
import viewmodels.address.AddressYearsViewModel

import scala.concurrent.ExecutionContext

class AddressYearsController @Inject()(
                                        override val appConfig: FrontendAppConfig,
                                        override val cacheConnector: UserAnswersCacheConnector,
                                        @EstablishersIndividual val navigator: Navigator,
                                        override val messagesApi: MessagesApi,
                                        authenticate: AuthAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction
                                      ) extends GenericAddressYearController with Retrievals {

  private val form = new AddressYearsFormProvider()(Message("messages__common_error__current_address_years"))

  def onPageLoad(mode: Mode, index: Index, srn: Option[String] = None): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      EstablisherDetailsId(index).retrieve.right.map { establisherDetails =>
        get(AddressYearsId(index), form, viewModel(mode, index, establisherDetails.fullName, srn))
      }
  }

  def onSubmit(mode: Mode, index: Index, srn: Option[String] = None): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      EstablisherDetailsId(index).retrieve.right.map { establisherDetails =>
        post(AddressYearsId(index), mode, form, viewModel(mode, index, establisherDetails.fullName, srn))
      }
  }

  private def viewModel(mode: Mode, index: Index, establisherName: String, srn: Option[String]) = AddressYearsViewModel(
    postCall = routes.AddressYearsController.onSubmit(mode, index, srn),
    title = Message("messages__establisher_address_years__title"),
    heading = Message("messages__establisher_address_years__title"),
    legend = Message("messages__establisher_address_years__title"),
    subHeading = Some(Message(establisherName))
  )
}

