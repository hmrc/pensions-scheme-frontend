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

import config.{FeatureSwitchManagementService, FrontendAppConfig}
import controllers.NinoController
import controllers.actions._
import forms.NINOFormProvider
import identifiers.register.establishers.partnership.partner.{PartnerDetailsId, PartnerNameId, PartnerNewNinoId}
import javax.inject.Inject
import models.{Index, Mode}
import navigators.Navigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import utils.Toggles
import utils.annotations.EstablishersPartner
import viewmodels.{Message, NinoViewModel}

import scala.concurrent.ExecutionContext

class PartnerNinoNewController @Inject()(
                                          val appConfig: FrontendAppConfig,
                                          val messagesApi: MessagesApi,
                                          val userAnswersService: UserAnswersService,
                                          val navigator: Navigator,
                                          authenticate: AuthAction,
                                          getData: DataRetrievalAction,
                                          allowAccess: AllowAccessActionProvider,
                                          requireData: DataRequiredAction,
                                          val formProvider: NINOFormProvider,
                                          fs: FeatureSwitchManagementService
                                        )(implicit val ec: ExecutionContext) extends NinoController with I18nSupport {

  private[controllers] val postCall = controllers.register.establishers.partnership.partner.routes.PartnerNinoNewController.onSubmit _

  private def viewmodel(name: String, establisherIndex: Index, partnerIndex: Index, mode: Mode, srn: Option[String]): NinoViewModel =
    NinoViewModel(
      postCall(mode, Index(establisherIndex), Index(partnerIndex), srn),
      title = Message("messages__enterNINO", Message("messages__thePartner").resolve),
      heading = Message("messages__enterNINO", name),
      hint = Message("messages__common__nino_hint"),
      srn = srn
    )

  private val partnerName: (Index, Index) => Retrieval[String] = (establisherIndex, partnerIndex) => Retrieval {
    implicit request =>
      if(fs.get(Toggles.isHnSEnabled)) {
        PartnerNameId(establisherIndex, partnerIndex).retrieve.right.map(_.fullName)
      } else {
        PartnerDetailsId(establisherIndex, partnerIndex).retrieve.right.map(_.fullName)
      }
  }

  def onPageLoad(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        println("\n\n\n mode : "+Mode.jsLiteral.to(mode))
        partnerName(establisherIndex, partnerIndex).retrieve.right.map {
          name =>
            get(
              PartnerNewNinoId(establisherIndex, partnerIndex),
              formProvider(name),
              viewmodel(name, establisherIndex, partnerIndex, mode, srn)
            )
        }
    }

  def onSubmit(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      partnerName(establisherIndex, partnerIndex).retrieve.right.map {
        name =>
          post(
            PartnerNewNinoId(establisherIndex, partnerIndex),
            mode,
            formProvider(name),
            viewmodel(name, establisherIndex, partnerIndex, mode, srn)
          )
      }
  }

}
