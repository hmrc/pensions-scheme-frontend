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

package controllers.register.establishers.partnership.partner

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import forms.register.PersonNameFormProvider
import identifiers.register.establishers.partnership.partner.{IsNewPartnerId, PartnerNameId}
import javax.inject.Inject
import models.person.PersonName
import models.requests.DataRequest
import models.{Index, Mode}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.{Enumerable, UserAnswers}
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.personName

import scala.concurrent.{ExecutionContext, Future}

class PartnerNameController @Inject()(
                                       appConfig: FrontendAppConfig,
                                       override val messagesApi: MessagesApi,
                                       userAnswersService: UserAnswersService,
                                       navigator: Navigator,
                                       authenticate: AuthAction,
                                       getData: DataRetrievalAction,
                                       allowAccess: AllowAccessActionProvider,
                                       requireData: DataRequiredAction,
                                       formProvider: PersonNameFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       val view: personName
                                     )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController with Retrievals with I18nSupport with Enumerable.Implicits {

  def onPageLoad(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        val preparedForm = request.userAnswers.get[PersonName](PartnerNameId(establisherIndex, partnerIndex)) match {
          case None => form
          case Some(value) => form.fill(value)
        }
        Future.successful(Ok(view(preparedForm, viewmodel(mode, establisherIndex, partnerIndex, srn),
          existingSchemeName)))
    }

  private def form(implicit request: DataRequest[AnyContent]) = formProvider("messages__error__partner")

  private def viewmodel(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: Option[String]) =
    CommonFormWithHintViewModel(
    postCall = routes.PartnerNameController.onSubmit(mode, establisherIndex, partnerIndex, srn),
    title = Message("messages__partnerName__title"),
    heading = Message("messages__partnerName__heading"),
    srn = srn
  )

  def onSubmit(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        form.bindFromRequest().fold(
          (formWithErrors: Form[_]) =>
            Future.successful(BadRequest(view(formWithErrors, viewmodel(mode, establisherIndex, partnerIndex, srn),
              existingSchemeName)))
          ,
          value => {
            val answers = request.userAnswers.set(IsNewPartnerId(establisherIndex, partnerIndex))(true).flatMap(
              _.set(PartnerNameId(establisherIndex, partnerIndex))(value)).asOpt.getOrElse(request.userAnswers)

            userAnswersService.upsert(mode, srn, answers.json).map {
              cacheMap =>
                Redirect(navigator.nextPage(PartnerNameId(establisherIndex, partnerIndex), mode, UserAnswers
                (cacheMap), srn))
            }
          }
        )
    }
}

