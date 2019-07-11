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

package controllers.register.establishers.company.director

import config.FrontendAppConfig
import connectors.AddressLookupConnector
import controllers.actions._
import controllers.address.PostcodeLookupController
import forms.address.PostCodeLookupFormProvider
import identifiers.register.establishers.company.director.{DirectorDetailsId, DirectorPreviousAddressPostcodeLookupId}
import javax.inject.Inject
import models.{Index, Mode}
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import utils.Navigator
import utils.annotations.EstablishersCompanyDirector
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel

import scala.concurrent.ExecutionContext

class DirectorPreviousAddressPostcodeLookupController @Inject()(
                                                                 override val appConfig: FrontendAppConfig,
                                                                 override val messagesApi: MessagesApi,
                                                                 val userAnswersService: UserAnswersService,
                                                                 override val addressLookupConnector: AddressLookupConnector,
                                                                 @EstablishersCompanyDirector override val navigator: Navigator,
                                                                 authenticate: AuthAction,
                                                                 getData: DataRetrievalAction,
                                                                 allowAccess: AllowAccessActionProvider,
                                                                 requireData: DataRequiredAction,
                                                                 formProvider: PostCodeLookupFormProvider
                                                               )(implicit val ec: ExecutionContext) extends PostcodeLookupController {

  protected val form: Form[String] = formProvider()

  def onPageLoad(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        viewmodel(mode, establisherIndex, directorIndex, srn).retrieve.right.map(
          vm =>
            get(vm)
        )
    }

  private def viewmodel(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: Option[String]) = Retrieval {
    implicit request =>
      DirectorDetailsId(establisherIndex, directorIndex).retrieve.right.map(
        details => PostcodeLookupViewModel(
          routes.DirectorPreviousAddressPostcodeLookupController.onSubmit(mode, establisherIndex, directorIndex, srn),
          routes.DirectorPreviousAddressController.onPageLoad(mode, establisherIndex, directorIndex, srn),
          Message("messages__directorPreviousAddressPostcodeLookup__title"),
          Message("messages__previousAddressPostcodeLookup__heading", details.fullName),
          Some(details.fullName),
          enterPostcode = Message("messages__common__postcode_lookup__manual_link"),
          srn = srn
        )
      )
  }

  def onSubmit(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      viewmodel(mode, establisherIndex, directorIndex, srn).retrieve.right.map(
        vm =>
          post(DirectorPreviousAddressPostcodeLookupId(establisherIndex, directorIndex), vm, mode)
      )
  }
}
