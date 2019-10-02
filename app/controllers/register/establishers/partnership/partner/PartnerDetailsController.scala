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

package controllers.register.establishers.partnership.partner

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import forms.register.PersonDetailsFormProvider
import identifiers.register.establishers.partnership.PartnershipDetailsId
import identifiers.register.establishers.partnership.partner.{IsNewPartnerId, PartnerDetailsId}
import javax.inject.Inject
import models.person.PersonDetails
import models.{Index, Mode}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.EstablishersPartner
import utils.{SectionComplete, UserAnswers}
import views.html.register.establishers.partnership.partner.partnerDetails

import scala.concurrent.{ExecutionContext, Future}

class PartnerDetailsController @Inject()(
                                          appConfig: FrontendAppConfig,
                                          override val messagesApi: MessagesApi,
                                          userAnswersService: UserAnswersService,
                                          @EstablishersPartner navigator: Navigator,
                                          authenticate: AuthAction,
                                          getData: DataRetrievalAction,
                                          allowAccess: AllowAccessActionProvider,
                                          requireData: DataRequiredAction,
                                          formProvider: PersonDetailsFormProvider,
                                          sectionComplete: SectionComplete
                                        ) (implicit val ec: ExecutionContext) extends FrontendController with Retrievals with I18nSupport {

  private val form = formProvider()

  def onPageLoad(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        PartnershipDetailsId(establisherIndex).retrieve.right.map { _ =>
          val preparedForm = request.userAnswers.get[PersonDetails](PartnerDetailsId(establisherIndex, partnerIndex)) match {
            case None => form
            case Some(value) => form.fill(value)
          }
          val submitUrl = controllers.register.establishers.partnership.partner.routes.PartnerDetailsController.onSubmit(
            mode, establisherIndex, partnerIndex, srn)
          Future.successful(Ok(partnerDetails(appConfig, preparedForm, mode, establisherIndex, partnerIndex, existingSchemeName, submitUrl, srn)))
        }
    }

  def onSubmit(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
          form.bindFromRequest().fold(
            (formWithErrors: Form[_]) => {
              val submitUrl = controllers.register.establishers.partnership.partner.routes.PartnerDetailsController.onSubmit(
                mode, establisherIndex, partnerIndex, srn)
              Future.successful(BadRequest(partnerDetails(appConfig, formWithErrors, mode, establisherIndex, partnerIndex, existingSchemeName, submitUrl, srn)))
            },
            value => {
              val answers = request.userAnswers.set(IsNewPartnerId(establisherIndex, partnerIndex))(true).flatMap(
                _.set(PartnerDetailsId(establisherIndex, partnerIndex))(value)).asOpt.getOrElse(request.userAnswers)
              userAnswersService.upsert(mode, srn, answers.json).flatMap {
                cacheMap =>
                  Future.successful(Redirect(navigator.nextPage(PartnerDetailsId(establisherIndex, partnerIndex), mode, UserAnswers(cacheMap), srn)))
              }
            }
          )
    }
}
