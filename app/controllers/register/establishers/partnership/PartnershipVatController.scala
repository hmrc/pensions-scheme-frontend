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
import controllers.VatController
import controllers.actions._
import forms.VatFormProvider
import identifiers.register.establishers.partnership.{PartnershipDetailsId, PartnershipVatId}
import javax.inject.Inject
import models.{Index, Mode}
import navigators.Navigator
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import utils.annotations.EstablisherPartnership
import viewmodels.{Message, EnterVATViewModel}

import scala.concurrent.ExecutionContext


class PartnershipVatController @Inject()(
                                          override val appConfig: FrontendAppConfig,
                                          override val messagesApi: MessagesApi,
                                          val userAnswersService: UserAnswersService,
                                          override val navigator: Navigator,
                                          authenticate: AuthAction,
                                          getData: DataRetrievalAction,
                                          allowAccess: AllowAccessActionProvider,
                                          requireData: DataRequiredAction,
                                          formProvider: VatFormProvider
                                        )(implicit val ec: ExecutionContext) extends VatController {

  private def viewmodel(mode: Mode, index: Index, srn: Option[String]): Retrieval[EnterVATViewModel] =
    Retrieval {
      implicit request =>
        PartnershipDetailsId(index).retrieve.right.map {
          details =>
            EnterVATViewModel(
              postCall = routes.PartnershipVatController.onSubmit(mode, index, srn),
              title = Message("messages__partnershipVat__title"),
              heading = Message("messages__partnershipVat__heading"),
              hint = Message("messages__common__vat__hint"),
              subHeading = Some(details.name),
              srn = srn
            )
        }
    }

  private val form = formProvider()

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
    implicit request =>
      viewmodel(mode, index, srn).retrieve.right.map {
        vm =>
          get(PartnershipVatId(index), form, vm)
      }
  }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      viewmodel(mode, index, srn).retrieve.right.map {
        vm =>
          post(PartnershipVatId(index), mode, form, vm)
      }
  }
}
