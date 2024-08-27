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

package controllers.register.establishers.partnership

import config.FrontendAppConfig
import connectors.AddressLookupConnector
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.PostcodeLookupController
import forms.address.PostCodeLookupFormProvider
import identifiers.register.establishers.partnership.{PartnershipDetailsId, PartnershipPostcodeLookupId}

import javax.inject.Inject
import models.{Index, Mode, SchemeReferenceNumber}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel
import views.html.address.postcodeLookup

import scala.concurrent.ExecutionContext

class PartnershipPostcodeLookupController @Inject()(
                                                     override val appConfig: FrontendAppConfig,
                                                     override val messagesApi: MessagesApi,
                                                     val userAnswersService: UserAnswersService,
                                                     override val addressLookupConnector: AddressLookupConnector,
                                                     override val navigator: Navigator,
                                                     authenticate: AuthAction,
                                                     getData: DataRetrievalAction,
                                                     allowAccess: AllowAccessActionProvider,
                                                     requireData: DataRequiredAction,
                                                     formProvider: PostCodeLookupFormProvider,
                                                     val controllerComponents: MessagesControllerComponents,
                                                     val view: postcodeLookup
                                                   )(implicit val ec: ExecutionContext) extends
  PostcodeLookupController {

  protected val form: Form[String] = formProvider()

  def onPageLoad(mode: Mode, index: Index, srn: SchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        viewmodel(index, mode, srn).retrieve map get
    }

  private def viewmodel(index: Int, mode: Mode, srn: SchemeReferenceNumber): Retrieval[PostcodeLookupViewModel] =
    Retrieval {
      implicit request =>
        PartnershipDetailsId(index).retrieve.map {
          details =>
            PostcodeLookupViewModel(
              routes.PartnershipPostcodeLookupController.onSubmit(mode, index, srn),
              routes.PartnershipAddressController.onPageLoad(mode, index, srn),
              title = Message("messages__partnershipPostcodeLookup__heading", Message("messages__thePartnership")),
              heading = Message("messages__partnershipPostcodeLookup__heading", details.name),
              subHeading = Some(details.name),
              srn = srn
            )
        }
    }

  def onSubmit(mode: Mode, index: Index, srn: SchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        viewmodel(index, mode, srn).retrieve.map {
          vm =>
            post(PartnershipPostcodeLookupId(index), vm, mode)
        }
    }
}
