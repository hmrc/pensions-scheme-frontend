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

package controllers.register.establishers.partnership

import config.FrontendAppConfig
import connectors.AddressLookupConnector
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.PostcodeLookupController
import forms.address.PostCodeLookupFormProvider
import identifiers.register.establishers.partnership.{PartnershipDetailsId, PartnershipPostcodeLookupId}
import javax.inject.Inject
import models.{Index, Mode}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import utils.annotations.EstablisherPartnership
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel

import scala.concurrent.ExecutionContext

class PartnershipPostcodeLookupController @Inject()(
                                                     override val appConfig: FrontendAppConfig,
                                                     override val messagesApi: MessagesApi,
                                                     val userAnswersService: UserAnswersService,
                                                     override val addressLookupConnector: AddressLookupConnector,
                                                     @EstablisherPartnership override val navigator: Navigator,
                                                     authenticate: AuthAction,
                                                     getData: DataRetrievalAction,
                                                     allowAccess: AllowAccessActionProvider,
                                                     requireData: DataRequiredAction,
                                                     formProvider: PostCodeLookupFormProvider
                                                   )(implicit val ec: ExecutionContext) extends PostcodeLookupController {

  protected val form: Form[String] = formProvider()
  private val title: Message = "messages__partnershipPostcodeLookup__title"
  private val heading: Message = "messages__partnershipPostcodeLookup__heading"
  private val hint: Message = "messages__partnershipPostcodeLookup__hint"

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

  private def viewmodel(index: Int, mode: Mode, srn: Option[String]): Retrieval[PostcodeLookupViewModel] =
    Retrieval {
      implicit request =>
        PartnershipDetailsId(index).retrieve.right.map {
          details =>
            PostcodeLookupViewModel(
              routes.PartnershipPostcodeLookupController.onSubmit(mode, index, srn),
              routes.PartnershipAddressController.onPageLoad(mode, index, srn),
              title = Message(title),
              heading = Message(heading),
              subHeading = Some(details.name),
              srn = srn
            )
        }
    }
}
