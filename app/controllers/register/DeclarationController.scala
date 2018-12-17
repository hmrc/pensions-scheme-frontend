/*
 * Copyright 2018 HM Revenue & Customs
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
import forms.register.DeclarationFormProvider
import identifiers.register.establishers.company.{CompanyDetailsId, IsCompanyDormantId}
import identifiers.register.establishers.partnership.{IsPartnershipDormantId, PartnershipDetailsId}
import identifiers.register._
import javax.inject.Inject
import models.NormalMode
import models.register.{DeclarationDormant, EstablisherCompanyEntity, EstablisherIndividualEntity, EstablisherPartnershipEntity}
import models.register.DeclarationDormant.{No, Yes}
import models.register.SchemeType.MasterTrust
import models.requests.DataRequest
import play.api.Logger
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Result}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.crypto.ApplicationCrypto
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.Register
import utils.{Enumerable, Navigator, UserAnswers}
import views.html.register.declaration

import scala.concurrent.Future
import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.establishers.partnership.PartnershipDetailsId

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
                                       psaNameCacheConnector: PSANameCacheConnector,
                                       crypto: ApplicationCrypto,
                                       pensionAdministratorConnector: PensionAdministratorConnector
                                     ) extends FrontendController with Retrievals with I18nSupport with Enumerable.Implicits {

  private val form = formProvider()

  def onPageLoad: Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      showPage(Ok.apply, form)
  }

  def onSubmit: Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveSchemeName {
        _ =>
          form.bindFromRequest().fold(
            (formWithErrors: Form[_]) => {
              showPage(BadRequest.apply, formWithErrors)
            },
            value =>
              if (appConfig.isHubEnabled) {
                for {
                  cacheMap <- dataCacheConnector.save(request.externalId, DeclarationId, value = true)
                  submissionResponse <- pensionsSchemeConnector.registerScheme(UserAnswers(cacheMap), request.psaId.id)
                  cacheMap <- dataCacheConnector.save(request.externalId, SubmissionReferenceNumberId, submissionResponse)
                  _ <- sendEmail(submissionResponse.schemeReferenceNumber, request.psaId)
                } yield {
                  Redirect(navigator.nextPage(DeclarationId, NormalMode, UserAnswers(cacheMap)))
                }
              } else {
                dataCacheConnector.save(request.externalId, DeclarationId, value).map(cacheMap =>
                  Redirect(navigator.nextPage(DeclarationId, NormalMode, UserAnswers(cacheMap))))
              }
          )
      }
  }

  private def showPage(status: HtmlFormat.Appendable => Result, form: Form[_])(implicit request: DataRequest[AnyContent]) =
    if (appConfig.isHubEnabled) {
      hsShowPage(status, form)
    } else {
      nonHsShowPage(status, form)
    }

  private def nonHsShowPage(status: HtmlFormat.Appendable => Result, form: Form[_])(implicit request: DataRequest[AnyContent]) = {
    SchemeDetailsId.retrieve.right.map { _ =>
      val isCompany = request.userAnswers.hasCompanies
      request.userAnswers.get(DeclarationDormantId) match {
        case Some(Yes) => Future.successful(
          status(
            declaration(appConfig, form, isCompany, isDormant = true, showMasterTrustDeclaration, hasWorkingKnowledge = false)
          )
        )
        case Some(No) => Future.successful(
          status(
            declaration(appConfig, form, isCompany, isDormant = false, showMasterTrustDeclaration, hasWorkingKnowledge = false)
          )
        )
        case None if !isCompany => Future.successful(
          status(
            declaration(appConfig, form, isCompany, isDormant = false, showMasterTrustDeclaration, hasWorkingKnowledge = false)
          )
        )
        case _ => Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
      }
    }
  }


  private def hsShowPage(status: HtmlFormat.Appendable => Result, form: Form[_])(implicit request: DataRequest[AnyContent]) = {
    SchemeDetailsId.retrieve.right.map { _ =>
      val isCompany = request.userAnswers.hasCompanies

      val declarationDormantValue = if (isDeclarationDormant) DeclarationDormant.values.head else DeclarationDormant.values(1)
      val readyForRender = if (isCompany) {
        dataCacheConnector.save(request.externalId, DeclarationDormantId, declarationDormantValue).map(_ => ())
      } else {
        Future.successful(())
      }

      readyForRender.flatMap { _ =>
       request.userAnswers.get(DeclarationDutiesId) match {
          case Some(hasWorkingKnowledge) => Future.successful(
            status(
              declaration(appConfig, form, isCompany, isDormant = isDeclarationDormant, showMasterTrustDeclaration, hasWorkingKnowledge)
            )
          )
          case _ => Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
        }
      }
    }
  }

  private def showMasterTrustDeclaration(implicit request: DataRequest[AnyContent]): Boolean =
    request.userAnswers.get(SchemeDetailsId).map(_.schemeType).contains(MasterTrust)

  private def isDeclarationDormant(implicit request: DataRequest[AnyContent]): Boolean =
    request.userAnswers.allEstablishersAfterDelete.exists { allEstablishers =>
      allEstablishers.id match {
        case CompanyDetailsId(index) =>
          isDormant(request.userAnswers.get(IsCompanyDormantId(index)))
        case PartnershipDetailsId(index) =>
          isDormant(request.userAnswers.get(IsPartnershipDormantId(index)))
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