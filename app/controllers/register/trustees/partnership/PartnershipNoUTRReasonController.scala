/*
 * Copyright 2024 HM Revenue & Customs
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
import controllers.actions._
import controllers.{ReasonController, Retrievals}
import forms.ReasonFormProvider
import identifiers.register.trustees.partnership.{PartnershipDetailsId, PartnershipNoUTRReasonId}
import javax.inject.Inject
import models.requests.DataRequest
import models.{Index, Mode}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import utils.Enumerable
import viewmodels.{Message, ReasonViewModel}
import views.html.reason

import scala.concurrent.ExecutionContext

class PartnershipNoUTRReasonController @Inject()(
                                                  override val appConfig: FrontendAppConfig,
                                                  override val messagesApi: MessagesApi,
                                                  override val userAnswersService: UserAnswersService,
                                                  override val navigator: Navigator,
                                                  authenticate: AuthAction,
                                                  getData: DataRetrievalAction,
                                                  allowAccess: AllowAccessActionProvider,
                                                  requireData: DataRequiredAction,
                                                  formProvider: ReasonFormProvider,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  val view: reason
                                                )(implicit val ec: ExecutionContext) extends ReasonController with
  Retrievals
  with I18nSupport with Enumerable.Implicits {

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        PartnershipDetailsId(index).retrieve.map { details =>
          val partnershipName = details.name
          get(PartnershipNoUTRReasonId(index), viewModel(mode, index, srn, partnershipName), form(partnershipName))
        }
    }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        PartnershipDetailsId(index).retrieve.map { details =>
          val partnershipName = details.name
          post(PartnershipNoUTRReasonId(index), mode, viewModel(mode, index, srn, partnershipName), form
          (partnershipName))
        }
    }

  private def form(companyName: String)(implicit request: DataRequest[AnyContent]): Form[String] =
    formProvider("messages__reason__error_utrRequired", companyName)

  private def viewModel(mode: Mode, index: Index, srn: Option[String], partnershipName: String): ReasonViewModel =
    ReasonViewModel(
      postCall = routes.PartnershipNoUTRReasonController.onSubmit(mode, index, srn),
      title = Message("messages__whyNoUTR", Message("messages__thePartnership")),
      heading = Message("messages__whyNoUTR", partnershipName),
      srn = srn
    )
}
