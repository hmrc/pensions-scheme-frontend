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

package controllers.register.trustees.partnership

import config.FrontendAppConfig
import connectors.AddressLookupConnector
import controllers.actions._
import controllers.address.PostcodeLookupController
import forms.address.PostCodeLookupFormProvider
import identifiers.register.trustees.partnership.{PartnershipDetailsId, PartnershipPreviousAddressPostcodeLookupId}
import javax.inject.Inject
import models.{Index, Mode}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import utils.annotations.TrusteesPartnership
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel

import scala.concurrent.ExecutionContext

class PartnershipPreviousAddressPostcodeLookupController @Inject()(
                                                                    override val appConfig: FrontendAppConfig,
                                                                    override val messagesApi: MessagesApi,
                                                                    val userAnswersService: UserAnswersService,
                                                                    override val addressLookupConnector: AddressLookupConnector,
                                                                    @TrusteesPartnership override val navigator: Navigator,
                                                                    authenticate: AuthAction,
                                                                    getData: DataRetrievalAction,
                                                                    allowAccess: AllowAccessActionProvider,
                                                                    requireData: DataRequiredAction,
                                                                    formProvider: PostCodeLookupFormProvider
                                                                  )(implicit val ec: ExecutionContext) extends PostcodeLookupController {

  private val title: Message = "messages__partnershipPreviousAddressPostcodeLookup__title"
  private val heading: Message = "messages__partnershipPreviousAddressPostcodeLookup__title"

  protected val form: Form[String] = formProvider()

  private def viewmodel(index: Int, mode: Mode, srn: Option[String]): Retrieval[PostcodeLookupViewModel] =
    Retrieval {
      implicit request =>
        PartnershipDetailsId(index).retrieve.right.map {
          details =>
            PostcodeLookupViewModel(
              routes.PartnershipPreviousAddressPostcodeLookupController.onSubmit(mode, index, srn),
              routes.PartnershipPreviousAddressController.onPageLoad(mode, index, srn),
              title = Message(title),
              heading = Message(heading),
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
            post(PartnershipPreviousAddressPostcodeLookupId(index), vm, mode)
        }
    }
}
