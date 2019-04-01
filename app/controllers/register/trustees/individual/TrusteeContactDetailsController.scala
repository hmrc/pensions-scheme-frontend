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

package controllers.register.trustees.individual

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.actions._
import forms.ContactDetailsFormProvider
import identifiers.register.trustees.individual.{TrusteeContactDetailsId, TrusteeDetailsId}
import javax.inject.Inject
import models.{Index, Mode}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.TrusteesIndividual
import viewmodels.{ContactDetailsViewModel, Message}

class TrusteeContactDetailsController @Inject()(
                                                 @TrusteesIndividual override val navigator: Navigator,
                                                 override val appConfig: FrontendAppConfig,
                                                 override val messagesApi: MessagesApi,
                                                 override val cacheConnector: UserAnswersCacheConnector,
                                                 authenticate: AuthAction,
                                                 getData: DataRetrievalAction,
                                                 requireData: DataRequiredAction,
                                                 formProvider: ContactDetailsFormProvider
                                               ) extends controllers.ContactDetailsController {

  private val form = formProvider()

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      TrusteeDetailsId(index).retrieve.right.map {
        trusteeDetails =>
          get(TrusteeContactDetailsId(index), form, viewmodel(mode, index, trusteeDetails.fullName, srn))
      }
  }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      TrusteeDetailsId(index).retrieve.right.map {
        trusteeDetails =>
          post(TrusteeContactDetailsId(index), mode, form, viewmodel(mode, index, trusteeDetails.fullName, srn))
      }
  }

  private def viewmodel(mode: Mode, index: Index, trusteeName: String, srn: Option[String]) = ContactDetailsViewModel(
    postCall = routes.TrusteeContactDetailsController.onSubmit(mode, index, srn),
    title = Message("messages__trustee_contact_details__title"),
    heading = Message("messages__trustee_contact_details__heading"),
    body = Message("messages__contact_details__body"),
    subHeading = Some(trusteeName)
  )
}
