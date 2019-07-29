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
import controllers.actions._
import forms.ContactDetailsFormProvider
import identifiers.register.establishers.partnership.{PartnershipContactDetailsId, PartnershipDetailsId}
import javax.inject.Inject
import models.{Index, Mode}
import navigators.Navigator
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import utils._
import utils.annotations.EstablisherPartnership
import viewmodels.{ContactDetailsViewModel, Message}

import scala.concurrent.ExecutionContext

class PartnershipContactDetailsController @Inject()(
                                                     @EstablisherPartnership override val navigator: Navigator,
                                                     override val appConfig: FrontendAppConfig,
                                                     override val messagesApi: MessagesApi,
                                                     val userAnswersService: UserAnswersService,
                                                     authenticate: AuthAction,
                                                     getData: DataRetrievalAction,
                                                     allowAccess: AllowAccessActionProvider,
                                                     requireData: DataRequiredAction,
                                                     formProvider: ContactDetailsFormProvider
                                                   )(implicit val ec: ExecutionContext) extends controllers.ContactDetailsController {

  private val form = formProvider()

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        PartnershipDetailsId(index).retrieve.right.map {
          partnershipDetails =>
            get(PartnershipContactDetailsId(index), form, viewmodel(mode, index, partnershipDetails.name, srn))
        }
    }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        PartnershipDetailsId(index).retrieve.right.map {
          partnershipDetails =>
            post(PartnershipContactDetailsId(index), mode, form, viewmodel(mode, index, partnershipDetails.name, srn))
        }
    }

  private def viewmodel(mode: Mode, index: Index, partnershipName: String, srn: Option[String]) = ContactDetailsViewModel(
    postCall = routes.PartnershipContactDetailsController.onSubmit(mode, index, srn),
    title = Message("messages__partnership_contact_details__title"),
    heading = Message("messages__partnership_contact_details__heading"),
    body = Message("messages__partnership_contact_details__body"),
    subHeading = Some(partnershipName),
    srn = srn
  )
}
