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

package controllers.register.establishers.individual

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import controllers.address.{AddressYearsController => GenericAddressYearController}
import forms.address.AddressYearsFormProvider
import identifiers.register.establishers.individual.{AddressYearsId, EstablisherNameId}
import javax.inject.Inject
import models.requests.DataRequest
import models.{Index, Mode}
import navigators.Navigator
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import viewmodels.Message
import viewmodels.address.AddressYearsViewModel
import views.html.address.addressYears

import scala.concurrent.ExecutionContext

class AddressYearsController @Inject()(
                                        override val appConfig: FrontendAppConfig,
                                        val userAnswersService: UserAnswersService,
                                        val navigator: Navigator,
                                        override val messagesApi: MessagesApi,
                                        authenticate: AuthAction,
                                        getData: DataRetrievalAction,
                                        allowAccess: AllowAccessActionProvider,
                                        requireData: DataRequiredAction,
                                        val view: addressYears,
                                        val controllerComponents: MessagesControllerComponents
                                      )(implicit val ec: ExecutionContext) extends GenericAddressYearController with
  Retrievals {

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        EstablisherNameId(index).retrieve.right.map { establisherName =>
          get(AddressYearsId(index), form(establisherName.fullName), viewModel(mode, index, establisherName.fullName,
            srn))
        }
    }

  private def form(establisherName: String)(implicit request: DataRequest[AnyContent]) =
    new AddressYearsFormProvider()(Message("messages__establisher_address_years__formError", establisherName))

  private def viewModel(mode: Mode, index: Index, establisherName: String, srn: Option[String])
                       (implicit request: DataRequest[AnyContent]) = AddressYearsViewModel(
    postCall = routes.AddressYearsController.onSubmit(mode, index, srn),
    title = Message("messages__establisher_address_years__title", Message("messages__theIndividual").resolve),
    heading = Message("messages__establisher_address_years__title", establisherName),
    legend = Message("messages__establisher_address_years__title", establisherName),
    subHeading = Some(Message(establisherName)),
    srn = srn
  )

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData
  (mode, srn) andThen requireData).async {
    implicit request =>
      EstablisherNameId(index).retrieve.right.map { establisherName =>
        post(AddressYearsId(index), mode, form(establisherName.fullName), viewModel(mode, index, establisherName
          .fullName, srn))
      }
  }
}

