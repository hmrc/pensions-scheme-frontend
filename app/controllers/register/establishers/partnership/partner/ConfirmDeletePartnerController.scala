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
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import forms.register.establishers.partnership.partner.ConfirmDeletePartnerFormProvider
import identifiers.register.establishers.partnership.partner.{ConfirmDeletePartnerId, PartnerNameId}
import javax.inject.Inject
import models.requests.DataRequest
import models.{Index, Mode}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{SectionComplete, UserAnswers}
import views.html.register.establishers.partnership.partner.confirmDeletePartner

import scala.concurrent.{ExecutionContext, Future}

class ConfirmDeletePartnerController @Inject()(
                                                appConfig: FrontendAppConfig,
                                                override val messagesApi: MessagesApi,
                                                userAnswersService: UserAnswersService,
                                                navigator: Navigator,
                                                authenticate: AuthAction,
                                                getData: DataRetrievalAction,
                                                allowAccess: AllowAccessActionProvider,
                                                requireData: DataRequiredAction,
                                                sectionComplete: SectionComplete,
                                                formProvider: ConfirmDeletePartnerFormProvider
                                              )(implicit val ec: ExecutionContext) extends FrontendController with I18nSupport with Retrievals {

  private val form: Form[Boolean] = formProvider()

  def deletePartner(establisherIndex: Index, partnerIndex: Index, mode: Mode, srn: Option[String]
                   )(implicit request: DataRequest[AnyContent]): Option[Future[JsValue]] =
    request.userAnswers.get(PartnerNameId(establisherIndex, partnerIndex)).map { partner =>
      userAnswersService.save(mode, srn, PartnerNameId(establisherIndex, partnerIndex), partner.copy(isDeleted = true))
    }

  def onPageLoad(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        PartnerNameId(establisherIndex, partnerIndex).retrieve.right.map {
          partner =>
            if (partner.isDeleted) {
              Future.successful(Redirect(routes.AlreadyDeletedController.onPageLoad(mode, establisherIndex, partnerIndex, srn)))
            } else {
              Future.successful(
                Ok(
                  confirmDeletePartner(
                    appConfig,
                    form,
                    partner.fullName,
                    routes.ConfirmDeletePartnerController.onSubmit(mode, establisherIndex, partnerIndex, srn),
                    existingSchemeName
                  )
                )
              )
            }
        }
    }

  def onSubmit(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        PartnerNameId(establisherIndex, partnerIndex).retrieve.right.map {
          partnerDetails =>
            form.bindFromRequest().fold(
              (formWithErrors: Form[_]) =>
                Future.successful(BadRequest(confirmDeletePartner(
                  appConfig,
                  formWithErrors,
                  partnerDetails.fullName,
                  routes.ConfirmDeletePartnerController.onSubmit(mode, establisherIndex, partnerIndex, srn),
                  existingSchemeName
                ))),
              value => {
                val deletionResult = if (value) {
                  deletePartner(establisherIndex, partnerIndex, mode, srn).getOrElse(Future.successful(request.userAnswers.json))
                } else {
                  Future.successful(request.userAnswers.json)
                }
                deletionResult.flatMap {
                  jsValue =>
                    Future.successful(Redirect(navigator.nextPage(ConfirmDeletePartnerId(establisherIndex), mode, UserAnswers(jsValue), srn)))
                }
              }
            )
        }
    }
}
