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

package controllers.register.establishers.individual

import config.FrontendAppConfig
import connectors.AddressLookupConnector
import controllers.actions._
import controllers.address.{PostcodeLookupController => GenericPostcodeLookupController}
import forms.address.PostCodeLookupFormProvider
import identifiers.register.establishers.individual.{EstablisherNameId, PreviousPostCodeLookupId}

import javax.inject.Inject
import models.{Index, Mode, OptionalSchemeReferenceNumber, SchemeReferenceNumber}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel
import views.html.address.postcodeLookup

import scala.concurrent.ExecutionContext

class PreviousAddressPostCodeLookupController @Inject()(
                                                         override val appConfig: FrontendAppConfig,
                                                         override val messagesApi: MessagesApi,
                                                         val userAnswersService: UserAnswersService,
                                                         override val addressLookupConnector: AddressLookupConnector,
                                                         val navigator: Navigator,
                                                         authenticate: AuthAction,
                                                         getData: DataRetrievalAction,
                                                         allowAccess: AllowAccessActionProvider,
                                                         requireData: DataRequiredAction,
                                                         formProvider: PostCodeLookupFormProvider,
                                                         val view: postcodeLookup,
                                                         val controllerComponents: MessagesControllerComponents
                                                       )(implicit val ec: ExecutionContext) extends
  GenericPostcodeLookupController {

  protected val form: Form[String] = formProvider()

  def onPageLoad(mode: Mode, index: Index, srn: OptionalSchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        viewmodel(index, mode, srn).retrieve map get
    }

  def onSubmit(mode: Mode, index: Index, srn: OptionalSchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        viewmodel(index, mode, srn).retrieve.map {
          vm =>
            post(PreviousPostCodeLookupId(index), vm, mode)
        }
    }

  private def viewmodel(index: Int, mode: Mode, srn: OptionalSchemeReferenceNumber): Retrieval[PostcodeLookupViewModel] =
    Retrieval {
      implicit request =>
        EstablisherNameId(index).retrieve.map {
          details =>
            PostcodeLookupViewModel(
              routes.PreviousAddressPostCodeLookupController.onSubmit(mode, index, srn),
              routes.PreviousAddressController.onPageLoad(mode, index, srn),
              title = Message("messages__establisher_individual_previous_address__heading", Message
              ("messages__theIndividual")),
              heading = Message("messages__establisher_individual_previous_address__heading", details.fullName),
              subHeading = Some(details.fullName),
              srn = srn
            )
        }
    }
}
