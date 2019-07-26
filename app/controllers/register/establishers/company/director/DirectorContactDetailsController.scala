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

package controllers.register.establishers.company.director

import config.FrontendAppConfig
import controllers.actions._
import forms.ContactDetailsFormProvider
import identifiers.register.establishers.company.director.{DirectorContactDetailsId, DirectorDetailsId}
import javax.inject.Inject
import models.{Index, Mode}
import navigators.Navigator
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import utils._
import utils.annotations.EstablishersCompanyDirector
import viewmodels.{ContactDetailsViewModel, Message}

import scala.concurrent.ExecutionContext

class DirectorContactDetailsController @Inject()(
                                                  @EstablishersCompanyDirector override val navigator: Navigator,
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

  def onPageLoad(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        DirectorDetailsId(establisherIndex, directorIndex).retrieve.right.map {
          director =>
            get(DirectorContactDetailsId(establisherIndex, directorIndex), form, viewmodel(mode, establisherIndex, directorIndex, director.fullName, srn))
        }
    }

  def onSubmit(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      DirectorDetailsId(establisherIndex, directorIndex).retrieve.right.map {
        director =>
          post(DirectorContactDetailsId(establisherIndex, directorIndex), mode, form, viewmodel(mode, establisherIndex, directorIndex, director.fullName, srn))
      }
  }

  private def viewmodel(mode: Mode, establisherIndex: Index, directorIndex: Index, directorName: String, srn: Option[String]) = ContactDetailsViewModel(
    postCall = routes.DirectorContactDetailsController.onSubmit(mode, establisherIndex, directorIndex, srn),
    title = Message("messages__company_director_contact__title"),
    heading = Message("messages__company_director_contact__heading"),
    body = Message("messages__contact_details__body"),
    subHeading = Some(directorName),
    srn = srn
  )
}
