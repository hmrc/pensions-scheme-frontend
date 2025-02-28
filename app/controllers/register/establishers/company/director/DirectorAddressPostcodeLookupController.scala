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

import config.FrontendAppConfig
import connectors.AddressLookupConnector
import controllers.actions._
import controllers.address.PostcodeLookupController
import forms.address.PostCodeLookupFormProvider
import identifiers.register.establishers.company.director.{DirectorAddressPostcodeLookupId, DirectorNameId}
import models.{Index, Mode, OptionalSchemeReferenceNumber}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import utils.annotations.EstablishersCompanyDirector
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel
import views.html.address.postcodeLookup

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class DirectorAddressPostcodeLookupController @Inject()(
                                                         override val appConfig: FrontendAppConfig,
                                                         override val messagesApi: MessagesApi,
                                                         val userAnswersService: UserAnswersService,
                                                         override val addressLookupConnector: AddressLookupConnector,
                                                         @EstablishersCompanyDirector override val navigator: Navigator,
                                                         authenticate: AuthAction,
                                                         getData: DataRetrievalAction,
                                                         allowAccess: AllowAccessActionProvider,
                                                         requireData: DataRequiredAction,
                                                         formProvider: PostCodeLookupFormProvider,
                                                         val view: postcodeLookup,
                                                         val controllerComponents: MessagesControllerComponents
                                                       )(implicit val ec: ExecutionContext) extends
  PostcodeLookupController {

  val directorName = (establisherIndex: Index, directorIndex: Index) => Retrieval {
    implicit request =>
      DirectorNameId(establisherIndex, directorIndex).retrieve.map(_.fullName)
  }
  protected val form: Form[String] = formProvider()

  def onPageLoad(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: OptionalSchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        viewmodel(establisherIndex, directorIndex, mode, srn).retrieve map get
    }

  def onSubmit(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: OptionalSchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        viewmodel(establisherIndex, directorIndex, mode, srn).retrieve.map(
          vm =>
            post(DirectorAddressPostcodeLookupId(establisherIndex, directorIndex), vm, mode)
        )
    }

  private def viewmodel(establisherIndex: Index, directorIndex: Index, mode: Mode, srn: OptionalSchemeReferenceNumber)
  : Retrieval[PostcodeLookupViewModel] =
    Retrieval(
      implicit request =>
        directorName(establisherIndex, directorIndex).retrieve.map {
          name =>
            PostcodeLookupViewModel(
              postCall = routes.DirectorAddressPostcodeLookupController.onSubmit(mode, establisherIndex,
                directorIndex, srn),
              manualInputCall = routes.DirectorAddressController.onPageLoad(mode, establisherIndex, directorIndex, srn),
              title = Message("messages__directorCompanyAddressPostcodeLookup__title"),
              heading = Message("messages__addressPostcodeLookup__heading", name),
              subHeading = None,
              srn = srn
            )
        }
    )

}
