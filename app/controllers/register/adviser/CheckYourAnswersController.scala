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

package controllers.register.adviser

import config.FrontendAppConfig
import connectors._
import controllers.actions._
import identifiers.register.SubmissionReferenceNumberId
import identifiers.register.adviser.{AdviserAddressId, AdviserDetailsId, CheckYourAnswersId}
import javax.inject.Inject
import models.requests.DataRequest
import models.{CheckMode, NormalMode, PSAName}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.crypto.{ApplicationCrypto, PlainText}
import uk.gov.hmrc.http.{HttpException, Upstream4xxResponse, Upstream5xxResponse}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.Adviser
import utils.checkyouranswers.Ops._
import utils.{CountryOptions, Navigator}
import viewmodels.AnswerSection
import views.html.check_your_answers

import scala.concurrent.Future

class CheckYourAnswersController @Inject()(appConfig: FrontendAppConfig,
                                           override val messagesApi: MessagesApi,
                                           dataCacheConnector: UserAnswersCacheConnector,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           @Adviser navigator: Navigator,
                                           implicit val countryOptions: CountryOptions,
                                           pensionsSchemeConnector: PensionsSchemeConnector,
                                           emailConnector: EmailConnector,
                                           psaNameCacheConnector: PSANameCacheConnector,
                                           crypto: ApplicationCrypto,
                                           pensionAdministratorConnector: PensionAdministratorConnector
                                          ) extends FrontendController with I18nSupport {

  def onPageLoad: Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>

      val adviserDetailsRow = AdviserDetailsId.row(routes.AdviserDetailsController.onPageLoad(CheckMode).url)
      val adviserAddressRow = AdviserAddressId.row(routes.AdviserAddressController.onPageLoad(CheckMode).url)
      val sections = Seq(AnswerSection(None, adviserDetailsRow ++ adviserAddressRow))

      Ok(
        check_your_answers(
          appConfig,
          sections,
          Some("messages__adviser__secondary_heading"),
          controllers.register.adviser.routes.CheckYourAnswersController.onSubmit()
        )
      )
  }

  def onSubmit: Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      pensionsSchemeConnector.registerScheme(request.userAnswers, request.psaId.id).flatMap(submissionResponse =>
        dataCacheConnector.save(request.externalId, SubmissionReferenceNumberId, submissionResponse).flatMap { _ =>
          sendEmail(submissionResponse.schemeReferenceNumber).map { _ =>
            Redirect(navigator.nextPage(CheckYourAnswersId, NormalMode, request.userAnswers))
          }
        }
      )
  }

  private def sendEmail(srn: String)(implicit request: DataRequest[AnyContent]): Future[EmailStatus] = {

      if(appConfig.isWorkPackageOneEnabled) {

        pensionAdministratorConnector.getPSAEmail flatMap { email =>
          emailConnector.sendEmail(email, "pods_scheme_register", Map("srn" -> formatSrnForEmail(srn)), request.psaId)
        } recoverWith {
          case _: Throwable => Future.successful(EmailNotSent)
        }

      } else {

        getName flatMap {
          case Some(value) =>
            value.as[PSAName].psaEmail match {
              case Some(email) => emailConnector.sendEmail(email, "pods_scheme_register", Map("srn" -> formatSrnForEmail(srn)), request.psaId)
              case _ => Future.successful(EmailNotSent)
            }
          case _ => Future.successful(EmailNotSent)
        }

      }
  }

  private def formatSrnForEmail(srn: String): String = {
    //noinspection ScalaStyle
    val (start, end) = srn.splitAt(6)
    start + ' ' + end
  }

  private def getName(implicit request: DataRequest[AnyContent]): Future[Option[JsValue]] = {
    val encryptedCacheId = crypto.QueryParameterCrypto.encrypt(PlainText(request.psaId.id)).value
    psaNameCacheConnector.fetch(encryptedCacheId)
  }

}
