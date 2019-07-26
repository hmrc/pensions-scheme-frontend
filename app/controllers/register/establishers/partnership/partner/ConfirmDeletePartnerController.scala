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
import forms.register.establishers.partnership.partner.ConfirmDeletePartnerFormProvider
import identifiers.register.establishers.IsEstablisherCompleteId
import identifiers.register.establishers.partnership.PartnershipDetailsId
import identifiers.register.establishers.partnership.partner.{ConfirmDeletePartnerId, PartnerDetailsId}
import javax.inject.Inject
import models.{CheckMode, Index, Mode, NormalMode}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.EstablishersPartner
import utils.{SectionComplete, UserAnswers}
import views.html.register.establishers.partnership.partner.confirmDeletePartner

import scala.concurrent.{ExecutionContext, Future}

class ConfirmDeletePartnerController @Inject()(
                                                appConfig: FrontendAppConfig,
                                                override val messagesApi: MessagesApi,
                                                userAnswersService: UserAnswersService,
                                                @EstablishersPartner navigator: Navigator,
                                                authenticate: AuthAction,
                                                getData: DataRetrievalAction,
                                                allowAccess: AllowAccessActionProvider,
                                                requireData: DataRequiredAction,
                                                sectionComplete: SectionComplete,
                                                formProvider: ConfirmDeletePartnerFormProvider
                                              )(implicit val ec: ExecutionContext) extends FrontendController with I18nSupport with Retrievals {

  private val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        (PartnershipDetailsId(establisherIndex) and PartnerDetailsId(establisherIndex, partnerIndex)).retrieve.right.map {
          case _ ~ partner =>
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
        PartnerDetailsId(establisherIndex, partnerIndex).retrieve.right.map {
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
                  userAnswersService.save(mode, srn, PartnerDetailsId(establisherIndex, partnerIndex), partnerDetails.copy(isDeleted = true))
                } else {
                  Future.successful(request.userAnswers.json)
                }
                deletionResult.flatMap {
                  jsValue =>
                    val userAnswers = UserAnswers(jsValue)
                    if (userAnswers.allPartnersAfterDelete(establisherIndex).isEmpty) {
                      userAnswers.upsert(IsEstablisherCompleteId(establisherIndex))(false) { result =>
                        userAnswersService.upsert(mode, srn, result.json).map { json =>
                          Redirect(navigator.nextPage(ConfirmDeletePartnerId(establisherIndex), mode, userAnswers, srn))
                        }
                      }
                    } else {
                      mode match {
                        case CheckMode | NormalMode =>
                          Future.successful(Redirect(navigator.nextPage(ConfirmDeletePartnerId(establisherIndex), mode, userAnswers, srn)))
                        case _ =>
                          userAnswers.upsert(IsEstablisherCompleteId(establisherIndex))(true) { result =>
                            userAnswersService.upsert(mode, srn, result.json).map { _ =>
                              Redirect(navigator.nextPage(ConfirmDeletePartnerId(establisherIndex), mode, userAnswers, srn))
                            }
                          }
                      }
                    }
                }
              }
            )
        }
    }
}
