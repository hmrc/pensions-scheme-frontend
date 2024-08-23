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

import config.FrontendAppConfig
import connectors.AddressLookupConnector
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.PostcodeLookupController
import forms.address.PostCodeLookupFormProvider
import identifiers.{AdviserAddressPostCodeLookupId, AdviserNameId}

import javax.inject.Inject
import models.{Mode, SchemeReferenceNumber}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import utils.annotations.WorkingKnowledge
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel
import views.html.address.postcodeLookup

import scala.concurrent.ExecutionContext

class AdviserPostCodeLookupController @Inject()(
                                                 override val appConfig: FrontendAppConfig,
                                                 override val messagesApi: MessagesApi,
                                                 val userAnswersService: UserAnswersService,
                                                 override val addressLookupConnector: AddressLookupConnector,
                                                 @WorkingKnowledge override val navigator: Navigator,
                                                 authenticate: AuthAction,
                                                 getData: DataRetrievalAction,
                                                 requireData: DataRequiredAction,
                                                 formProvider: PostCodeLookupFormProvider,
                                                 val controllerComponents: MessagesControllerComponents,
                                                 val view: postcodeLookup
                                               )(implicit val ec: ExecutionContext) extends PostcodeLookupController {

  protected val form: Form[String] = formProvider()

  def onPageLoad(mode: Mode, srn: SchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(srn=srn) andThen requireData).async {
      implicit request =>
        AdviserNameId.retrieve.map { adviserName =>
          get(viewmodel(mode, adviserName, srn))
        }
    }

  def onSubmit(mode: Mode, srn: SchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(srn=srn) andThen requireData).async {
      implicit request =>
        AdviserNameId.retrieve.map { adviserName =>
          post(AdviserAddressPostCodeLookupId, viewmodel(mode, adviserName), mode)
        }
    }

  private def viewmodel(mode: Mode, adviserName: String, srn: SchemeReferenceNumber) =
    PostcodeLookupViewModel(
      routes.AdviserPostCodeLookupController.onSubmit(mode, srn),
      routes.AdviserAddressController.onPageLoad(mode, srn),
      title = Message("messages__adviserPostCodeLookup__heading", Message("messages__theAdviser")),
      heading = Message("messages__adviserPostCodeLookup__heading", adviserName),
      subHeading = Some(Message("messages__adviserPostCodeLookupAddress__secondary"))
    )
}
