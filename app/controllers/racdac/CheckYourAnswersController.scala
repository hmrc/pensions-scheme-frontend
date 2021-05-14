/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.racdac

import config.FrontendAppConfig
import connectors.PensionAdministratorConnector
import controllers.actions._
import identifiers.racdac.{ContractOrPolicyNumberId, RACDACNameId}
import models.CheckMode
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.checkyouranswers.Ops._
import utils.{CountryOptions, Enumerable, UserAnswers}
import viewmodels.{AnswerSection, CYAViewModel, Message}
import views.html.racdac.checkYourAnswers

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class CheckYourAnswersController @Inject()(appConfig: FrontendAppConfig,
                                                      override val messagesApi: MessagesApi,
                                                      authenticate: AuthAction,
                                                      getData: DataRetrievalAction,
                                                      requireData: DataRequiredAction,
                                                      allowAccess: AllowAccessActionProvider,
                                                      implicit val countryOptions: CountryOptions,
                                                      val controllerComponents: MessagesControllerComponents,
                                                      val pensionAdministratorConnector: PensionAdministratorConnector,
                                                      val view: checkYourAnswers
                                                     )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController with Enumerable.Implicits with I18nSupport with Retrievals {

  def onPageLoad: Action[AnyContent] = (authenticate() andThen getData() andThen allowAccess(None) andThen requireData).async {
    implicit request =>

      implicit val userAnswers: UserAnswers = request.userAnswers
      val schemeName = request.userAnswers.get(RACDACNameId)

      val racdacNameSection = AnswerSection(
        None,
        RACDACNameId.row(controllers.racdac.routes.RACDACNameController.onPageLoad(CheckMode).url)
      )

      val racdacContractNoSection = AnswerSection(
        None,
        ContractOrPolicyNumberId.row(controllers.racdac.routes.ContractOrPolicyNumberController.onPageLoad(CheckMode).url)
      )

      val vm = CYAViewModel(
        answerSections = Seq(racdacNameSection, racdacContractNoSection),
        href = controllers.racdac.routes.DeclarationController.onPageLoad(),
        schemeName = schemeName,
        returnOverview = true,
        hideEditLinks = request.viewOnly,
        srn = None,
        hideSaveAndContinueButton = request.viewOnly,
        title = Message("checkYourAnswers.hs.title"),
        h1 = Message("checkYourAnswers.hs.title")
      )
      pensionAdministratorConnector.getPSAName.map { psaName =>
       Ok(view(vm,psaName))
      }
  }
}