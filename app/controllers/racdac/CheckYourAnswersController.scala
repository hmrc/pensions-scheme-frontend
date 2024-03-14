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

package controllers.racdac

import config.FrontendAppConfig
import connectors.PensionAdministratorConnector
import controllers.actions._
import identifiers.racdac.{RACDACNameId, ContractOrPolicyNumberId}
import models.AuthEntity.PSP
import models.requests.DataRequest
import models.{Mode, UpdateMode, CheckMode, NormalMode}
import play.api.i18n.{MessagesApi, I18nSupport}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.Racdac
import utils.checkyouranswers.Ops._
import utils.{UserAnswers, CountryOptions, Enumerable}
import viewmodels.{AnswerSection, Message, CYAViewModel}
import views.html.racdac.checkYourAnswers

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject()(appConfig: FrontendAppConfig,
                                           override val messagesApi: MessagesApi,
                                           authenticate: AuthAction,
                                           @Racdac getData: DataRetrievalAction,
                                           getPspData: PspDataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           allowAccess: AllowAccessActionProvider,
                                           implicit val countryOptions: CountryOptions,
                                           val controllerComponents: MessagesControllerComponents,
                                           val pensionAdministratorConnector: PensionAdministratorConnector,
                                           val view: checkYourAnswers
                                          )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController with Enumerable.Implicits with I18nSupport with Retrievals {

  def onPageLoad(mode: Mode, srn: Option[String]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn, refreshData = true) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        val returnLinkDetails: Future[(String, String)] =(mode, srn) match {
          case (UpdateMode, Some(srnNo)) =>
            lazy val schemeName = request.userAnswers.get(RACDACNameId).getOrElse(throw MissingSchemeNameException)
            Future.successful((appConfig.schemeDashboardUrl(request.psaId, None).format(srnNo), schemeName))
          case _ =>
            pensionAdministratorConnector.getPSAName.map { psaName =>
              (appConfig.managePensionsSchemeOverviewUrl.url, psaName)
            }
        }

        returnLinkDetails.map { case (returnUrl, returnName) =>
          Ok(view(vm(mode), returnName, returnUrl))
        }
    }

  def pspOnPageLoad(srn: String): Action[AnyContent] =
    (authenticate(Some(PSP)) andThen getPspData(srn) andThen requireData).async {
      implicit request =>
        lazy val schemeName = request.userAnswers.get(RACDACNameId).getOrElse(throw MissingSchemeNameException)
        Future.successful(Ok(view(vm(UpdateMode), schemeName, appConfig.schemeDashboardUrl(None, request.pspId).format(srn))))
    }

  def vm(mode: Mode)(implicit request: DataRequest[AnyContent]): CYAViewModel = {
    implicit val userAnswers: UserAnswers = request.userAnswers

    lazy val schemeName = request.userAnswers.get(RACDACNameId)
    val h1: Message = if (mode == NormalMode) Message("checkYourAnswers.hs.title") else Message("messages__scheme_details__title")
    val racdacNameSection = AnswerSection(
      None,
      RACDACNameId.row(controllers.racdac.routes.RACDACNameController.onPageLoad(CheckMode).url)
    )

    val racdacContractNoSection = AnswerSection(
      None,
      ContractOrPolicyNumberId.row(controllers.racdac.routes.ContractOrPolicyNumberController.onPageLoad(CheckMode).url)
    )

    CYAViewModel(
      answerSections = Seq(racdacNameSection, racdacContractNoSection),
      href = controllers.racdac.routes.DeclarationController.onPageLoad(),
      schemeName = schemeName,
      returnOverview = true,
      hideEditLinks = request.viewOnly,
      srn = None,
      hideSaveAndContinueButton = request.viewOnly,
      title = h1,
      h1 = h1
    )
  }
}
