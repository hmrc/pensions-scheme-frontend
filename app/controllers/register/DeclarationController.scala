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

package controllers.register

import config.FrontendAppConfig
import connectors._
import controllers.Retrievals
import controllers.actions._
import controllers.register.routes.DeclarationController
import identifiers.SchemeTypeId
import identifiers.register._
import identifiers.register.establishers.company.{CompanyDetailsId, IsCompanyDormantId}
import javax.inject.Inject
import models.NormalMode
import models.register.DeclarationDormant
import models.register.DeclarationDormant.Yes
import models.register.SchemeType.MasterTrust
import models.requests.DataRequest
import navigators.Navigator
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.crypto.ApplicationCrypto
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.annotations.Register
import utils.hstasklisthelper.{HsTaskListHelper, HsTaskListHelperRegistration}
import utils.{Enumerable, UserAnswers}
import views.html.register.declaration

import scala.concurrent.{ExecutionContext, Future}

class DeclarationController @Inject()(
                                       appConfig: FrontendAppConfig,
                                       override val messagesApi: MessagesApi,
                                       dataCacheConnector: UserAnswersCacheConnector,
                                       @Register navigator: Navigator,
                                       authenticate: AuthAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       pensionsSchemeConnector: PensionsSchemeConnector,
                                       emailConnector: EmailConnector,
                                       crypto: ApplicationCrypto,
                                       pensionAdministratorConnector: PensionAdministratorConnector,
                                       val controllerComponents: MessagesControllerComponents,
                                       val view: declaration
                                      )(implicit val executionContext: ExecutionContext) extends FrontendBaseController
                                        with Retrievals with I18nSupport with Enumerable.Implicits {

  def onPageLoad: Action[AnyContent] = (authenticate andThen getData() andThen requireData).async {
    implicit request =>
      val ua = request.userAnswers
      println("\n\n\n new HsTaskListHelperRegistration(ua).declarationEnabled(ua) : "+new HsTaskListHelperRegistration(ua).declarationEnabled(ua))
      if(new HsTaskListHelperRegistration(ua).declarationEnabled(ua)) {
        showPage(Ok.apply)
      } else {
        Future.successful(Redirect(controllers.routes.SchemeTaskListController.onPageLoad(NormalMode, None)))
      }
  }

  def onClickAgree: Action[AnyContent] = (authenticate andThen getData() andThen requireData).async {
    implicit request =>
      for {
        cacheMap <- dataCacheConnector.save(request.externalId, DeclarationId, value = true)
        submissionResponse <- pensionsSchemeConnector.registerScheme(UserAnswers(cacheMap), request.psaId.id)
        cacheMap <- dataCacheConnector.save(request.externalId, SubmissionReferenceNumberId, submissionResponse)
        _ <- sendEmail(submissionResponse.schemeReferenceNumber, request.psaId)
      } yield {
        Redirect(navigator.nextPage(DeclarationId, NormalMode, UserAnswers(cacheMap)))
      }
  }

  private def showPage(status: HtmlFormat.Appendable => Result)(implicit request: DataRequest[AnyContent]): Future[Result] = {
    val isCompany = request.userAnswers.hasCompanies(NormalMode)
    val href = DeclarationController.onClickAgree()

    val declarationDormantValue = if (isDeclarationDormant) DeclarationDormant.values.head else DeclarationDormant.values(1)
    val readyForRender = if (isCompany) {
      dataCacheConnector.save(request.externalId, DeclarationDormantId, declarationDormantValue).map(_ => ())
    } else {
      Future.successful(())
    }

    readyForRender.flatMap { _ =>
      request.userAnswers.get(identifiers.DeclarationDutiesId) match {
        case Some(hasWorkingKnowledge) => Future.successful(
          status(
            view(isCompany, isDormant = isDeclarationDormant,
              request.userAnswers.get(SchemeTypeId).contains(MasterTrust), hasWorkingKnowledge, existingSchemeName, href)
          )
        )
        case _ => Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
      }
    }
  }

  private def isDeclarationDormant(implicit request: DataRequest[AnyContent]): Boolean =
    request.userAnswers.allEstablishersAfterDelete(
      NormalMode
    ).exists { allEstablishers =>
      allEstablishers.id match {
        case CompanyDetailsId(index) =>
          isDormant(request.userAnswers.get(IsCompanyDormantId(index)))
        case _ =>
          false
      }
    }

  private def isDormant(dormant: Option[DeclarationDormant]): Boolean = {
    dormant match {
      case Some(Yes) => true
      case _ => false
    }
  }

  private def sendEmail(srn: String, psaId: PsaId)(implicit request: DataRequest[AnyContent]): Future[EmailStatus] = {
    Logger.debug("Fetch email from API")

    pensionAdministratorConnector.getPSAEmail flatMap { email =>
      emailConnector.sendEmail(email, appConfig.emailTemplateId, Map("srn" -> formatSrnForEmail(srn)), psaId)
    } recoverWith {
      case _: Throwable => Future.successful(EmailNotSent)
    }
  }

  private def formatSrnForEmail(srn: String): String = {
    //noinspection ScalaStyle
    val (start, end) = srn.splitAt(6)
    start + ' ' + end
  }

}