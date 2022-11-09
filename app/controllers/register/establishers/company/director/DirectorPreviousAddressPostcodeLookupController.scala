/*
 * Copyright 2022 HM Revenue & Customs
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
import identifiers.register.establishers.company.director.{DirectorNameId, DirectorPreviousAddressPostcodeLookupId}
import javax.inject.Inject
import models.{Index, Mode}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import utils.annotations.EstablishersCompanyDirector
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel
import views.html.address.postcodeLookup

import scala.concurrent.ExecutionContext

class DirectorPreviousAddressPostcodeLookupController @Inject()(
                                                                 override val appConfig: FrontendAppConfig,
                                                                 override val messagesApi: MessagesApi,
                                                                 val userAnswersService: UserAnswersService,
                                                                 override val
                                                                 addressLookupConnector: AddressLookupConnector,
                                                                 @EstablishersCompanyDirector override val
                                                                 navigator: Navigator,
                                                                 authenticate: AuthAction,
                                                                 getData: DataRetrievalAction,
                                                                 allowAccess: AllowAccessActionProvider,
                                                                 requireData: DataRequiredAction,
                                                                 formProvider: PostCodeLookupFormProvider,
                                                                 val view: postcodeLookup,
                                                                 val controllerComponents: MessagesControllerComponents
                                                               )(implicit val ec: ExecutionContext) extends
  PostcodeLookupController {

  protected val form: Form[String] = formProvider()
  private val directorName = (establisherIndex: Index, directorIndex: Index) => Retrieval {
    implicit request =>
      DirectorNameId(establisherIndex, directorIndex).retrieve.map(_.fullName)
  }

  def onPageLoad(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        viewmodel(mode, establisherIndex, directorIndex, srn).retrieve.map(get)
    }

  private def viewmodel(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: Option[String]) = Retrieval {
    implicit request =>
      directorName(establisherIndex, directorIndex).retrieve.map(
        name => PostcodeLookupViewModel(
          routes.DirectorPreviousAddressPostcodeLookupController.onSubmit(mode, establisherIndex, directorIndex, srn),
          routes.DirectorPreviousAddressController.onPageLoad(mode, establisherIndex, directorIndex, srn),
          Message("messages__directorPreviousAddressPostcodeLookup__title"),
          Message("messages__previousAddressPostcodeLookup__heading", name),
          None,
          srn = srn
        )
      )
  }

  def onSubmit(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        viewmodel(mode, establisherIndex, directorIndex, srn).retrieve.map(
          vm =>
            post(DirectorPreviousAddressPostcodeLookupId(establisherIndex, directorIndex), vm, mode)
        )
    }
}
