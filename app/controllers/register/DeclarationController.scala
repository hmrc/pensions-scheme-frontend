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

package controllers.register

import config.{FeatureSwitchManagementService, FrontendAppConfig}
import connectors._
import controllers.Retrievals
import controllers.actions._
import forms.register.DeclarationFormProvider
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
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Result}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.crypto.ApplicationCrypto
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.Register
import utils.{Enumerable, Toggles, UserAnswers}
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
                                       formProvider: DeclarationFormProvider,
                                       pensionsSchemeConnector: PensionsSchemeConnector,
                                       emailConnector: EmailConnector,
                                       crypto: ApplicationCrypto,
                                       pensionAdministratorConnector: PensionAdministratorConnector,
                                       fs: FeatureSwitchManagementService
                                     )(implicit val ec: ExecutionContext) extends FrontendController with Retrievals with I18nSupport with Enumerable.Implicits {

  private val form = formProvider()

  def onPageLoad: Action[AnyContent] = (authenticate andThen getData() andThen requireData).async {
    implicit request =>
      showPage(Ok.apply, form)
  }

  def onSubmit: Action[AnyContent] = (authenticate andThen getData() andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) => {
          showPage(BadRequest.apply, formWithErrors)
        },
        _ =>
          for {
            cacheMap <- dataCacheConnector.save(request.externalId, DeclarationId, value = true)
            submissionResponse <- pensionsSchemeConnector.registerScheme(UserAnswers(cacheMap), request.psaId.id)
            cacheMap <- dataCacheConnector.save(request.externalId, SubmissionReferenceNumberId, submissionResponse)
            _ <- sendEmail(submissionResponse.schemeReferenceNumber, request.psaId)
          } yield {
            Redirect(navigator.nextPage(DeclarationId, NormalMode, UserAnswers(cacheMap)))
          }
      )
  }

  private def showPage(status: HtmlFormat.Appendable => Result, form: Form[_])(implicit request: DataRequest[AnyContent]) = {
    val isCompany = request.userAnswers.hasCompanies(fs.get(Toggles.isEstablisherCompanyHnSEnabled))

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
            declaration(appConfig, form, isCompany, isDormant = isDeclarationDormant,
              request.userAnswers.get(SchemeTypeId).contains(MasterTrust), hasWorkingKnowledge, existingSchemeName)
          )
        )
        case _ => Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
      }
    }
  }

  private def isDeclarationDormant(implicit request: DataRequest[AnyContent]): Boolean =
    request.userAnswers.allEstablishersAfterDelete(fs.get(Toggles.isEstablisherCompanyHnSEnabled)).exists { allEstablishers =>
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