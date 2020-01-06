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

package controllers.register.trustees.partnership

import config.FrontendAppConfig
import connectors.AddressLookupConnector
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.PostcodeLookupController
import forms.address.PostCodeLookupFormProvider
import identifiers.register.trustees.partnership.{PartnershipDetailsId, PartnershipPostcodeLookupId}
import javax.inject.Inject
import models.{Index, Mode}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel

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
                                                     formProvider: PostCodeLookupFormProvider
                                                   )(implicit val ec: ExecutionContext) extends PostcodeLookupController {

  private val title: Message = "messages__partnershipPostcodeLookup__title"
  private val hint: Message = "messages__trusteePartnershipPostcodeLookup__hint"

  protected val form: Form[String] = formProvider()

  private def viewmodel(index: Int, mode: Mode, srn: Option[String]): Retrieval[PostcodeLookupViewModel] =
    Retrieval {
      implicit request =>
        PartnershipDetailsId(index).retrieve.right.map {
          details =>
            PostcodeLookupViewModel(
              routes.PartnershipPostcodeLookupController.onSubmit(mode, index, srn),
              routes.PartnershipAddressController.onPageLoad(mode, index, srn),
              title = Message(title),
              heading = Message("messages__partnershipPostcodeLookup__heading", details.name),
              subHeading = Some(details.name),
              srn = srn
            )
        }
    }

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        viewmodel(index, mode, srn).retrieve.right map get
    }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        viewmodel(index, mode, srn).retrieve.right.map {
          vm =>
            post(PartnershipPostcodeLookupId(index), vm, mode)
        }
    }
}
