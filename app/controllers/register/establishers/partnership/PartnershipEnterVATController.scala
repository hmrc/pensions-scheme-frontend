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
import controllers.EnterVATController
import controllers.actions._
import forms.EnterVATFormProvider
import identifiers.register.establishers.partnership.{PartnershipDetailsId, PartnershipEnterVATId}
import javax.inject.Inject
import models.{Index, Mode}
import navigators.Navigator
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import viewmodels.{EnterVATViewModel, Message}

import scala.concurrent.ExecutionContext

class PartnershipEnterVATController @Inject()(
                                                    override val appConfig: FrontendAppConfig,
                                                    override val messagesApi: MessagesApi,
                                                    override val userAnswersService: UserAnswersService,
                                                    override val navigator: Navigator,
                                                    authenticate: AuthAction,
                                                    getData: DataRetrievalAction,
                                                    allowAccess: AllowAccessActionProvider,
                                                    requireData: DataRequiredAction,
                                                    formProvider: EnterVATFormProvider
                                              )(implicit val ec: ExecutionContext) extends EnterVATController {

  private def form(companyName: String) = formProvider(companyName)

  private def viewModel(mode: Mode, index: Index, srn: Option[String], partnershipName: String): EnterVATViewModel = {
    EnterVATViewModel(
      postCall = routes.PartnershipEnterVATController.onSubmit(mode, index, srn),
      title = Message("messages__enterVAT", Message("messages__thePartnership").resolve),
      heading = Message("messages__enterVAT", partnershipName),
      hint = Message("messages__enterVAT__hint", partnershipName),
      subHeading = None,
      srn = srn
    )
  }

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        PartnershipDetailsId(index).retrieve.right.map { details =>
          val partnershipName = details.name
          get(PartnershipEnterVATId(index), viewModel(mode, index, srn, partnershipName), form(partnershipName))
        }
    }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        PartnershipDetailsId(index).retrieve.right.map { details =>
          val partnershipName = details.name
          post(PartnershipEnterVATId(index), mode, viewModel(mode, index, srn, partnershipName), form(partnershipName))
        }
    }
}
