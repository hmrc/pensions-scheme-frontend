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
import identifiers.TypedIdentifier
import identifiers.register.{DeclarationDutiesId, IsWorkingKnowledgeCompleteId, SubmissionReferenceNumberId}
import identifiers.register.adviser.{AdviserAddressId, AdviserDetailsId, CheckYourAnswersId}
import javax.inject.Inject
import models.address.Address
import models.register.AdviserDetails
import models.requests.DataRequest
import models.{CheckMode, NormalMode, PSAName}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsValue, Reads}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.crypto.{ApplicationCrypto, PlainText}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.Adviser
import utils.checkyouranswers.{AddressCYA, CheckYourAnswers}
import utils.{CountryOptions, Navigator, SectionComplete, UserAnswers}
import viewmodels.{AnswerRow, AnswerSection, Message}
import views.html.check_your_answers
import utils.checkyouranswers.Ops._

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
                                           pensionAdministratorConnector: PensionAdministratorConnector,
                                           sectionComplete: SectionComplete
                                          ) extends FrontendController with I18nSupport {

  def onPageLoad: Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>
      val sections = if (appConfig.isHubEnabled) {

        request.userAnswers.get(AdviserDetailsId).map {
          adviser =>
          implicit def address[I <: TypedIdentifier[Address]](implicit rds: Reads[Address], countryOptions: CountryOptions): CheckYourAnswers[I] =
            AddressCYA(label = Message("messages__adviserAddress__cyaHeading", adviser.adviserName))()

          val workingKnowldge = DeclarationDutiesId.row(controllers.routes.WorkingKnowledgeController.onPageLoad().url)
          val adviserDetailsRow = AdviserDetailsId.row(routes.AdviserDetailsController.onPageLoad(CheckMode).url)
          val adviserAddressRow = AdviserAddressId.row(routes.AdviserAddressController.onPageLoad(CheckMode).url)

          Seq(AnswerSection(None, workingKnowldge ++ adviserDetailsRow ++ adviserAddressRow))
        }.getOrElse(Seq())
      } else {
        val adviserDetailsRow = AdviserDetailsId.row(routes.AdviserDetailsController.onPageLoad(CheckMode).url)
        val adviserAddressRow = AdviserAddressId.row(routes.AdviserAddressController.onPageLoad(CheckMode).url)
        Seq(AnswerSection(None, adviserDetailsRow ++ adviserAddressRow))
      }
      Ok(
        check_your_answers(
          appConfig,
          sections,
          controllers.register.adviser.routes.CheckYourAnswersController.onSubmit()
        )
      )
  }

  private def hsAnswerSections()(implicit request:models.requests.DataRequest[play.api.mvc.AnyContent]):Seq[AnswerSection] = {
    implicit def adviserDetails[I <: TypedIdentifier[AdviserDetails]](implicit rds: Reads[AdviserDetails]): CheckYourAnswers[I] = {
      new CheckYourAnswers[I] {
        override def row(id: I)(changeUrl: String, userAnswers: UserAnswers) = userAnswers.get(id).map {
          adviserDetails =>
            val res = AdviserAddressId.row(routes.AdviserAddressController.onPageLoad(CheckMode).url)

            Seq(
              AnswerRow(
                "messages__workingKnowledge__heading",
                Seq(s"${adviserDetails.adviserName}"),
                answerIsMessageKey = false,
                Some(changeUrl),
                Message("messages__visuallyhidden__common__name", adviserDetails.adviserName)
              ),
              AnswerRow(
                "messages__adviserName__heading",
                Seq(s"${adviserDetails.adviserName}"),
                answerIsMessageKey = false,
                Some(changeUrl),
                Message("messages__visuallyhidden__common__name", adviserDetails.adviserName)
              ),
              AnswerRow(
                Message("messages__adviser__email__address__heading", adviserDetails.adviserName),
                Seq(s"${adviserDetails.emailAddress}"),
                answerIsMessageKey = false,
                Some(changeUrl),
                "messages__visuallyhidden__adviser__email_address"
              )) ++ res

        }.getOrElse(Seq.empty[AnswerRow])

      }
    }

    implicit def address[I <: TypedIdentifier[Address]](implicit rds: Reads[Address], countryOptions: CountryOptions): CheckYourAnswers[I] =
      AddressCYA(label=Message("messages__adviserAddress__cyaHeading", ""))()

   val adviserDetailsRow = AdviserDetailsId.row(routes.AdviserDetailsController.onPageLoad(CheckMode).url)
  //  val adviserAddressRow = AdviserAddressId.row(routes.AdviserAddressController.onPageLoad(CheckMode).url)
    Seq(AnswerSection(None, adviserDetailsRow))
  }

  def onSubmit: Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      if (appConfig.isHubEnabled) {
        sectionComplete.setCompleteFlag(request.externalId, IsWorkingKnowledgeCompleteId, request.userAnswers, value=true).map { _ =>
          Redirect(navigator.nextPage(CheckYourAnswersId, NormalMode, request.userAnswers))
        }
      } else {
        pensionsSchemeConnector.registerScheme(request.userAnswers, request.psaId.id).flatMap(submissionResponse =>
          dataCacheConnector.save(request.externalId, SubmissionReferenceNumberId, submissionResponse).flatMap { _ =>
            sendEmail(submissionResponse.schemeReferenceNumber).map { _ =>
              Redirect(navigator.nextPage(CheckYourAnswersId, NormalMode, request.userAnswers))
            }
          }
        ) recoverWith {
          case _: InvalidPayloadException =>
            Future.successful(Redirect(controllers.routes.ServiceUnavailableController.onPageLoad()))
        }
      }
  }

  private def sendEmail(srn: String)(implicit request: DataRequest[AnyContent]): Future[EmailStatus] = {
    pensionAdministratorConnector.getPSAEmail flatMap { email =>
      emailConnector.sendEmail(email, "pods_scheme_register", Map("srn" -> formatSrnForEmail(srn)), request.psaId)
    } recoverWith {
      case _: Throwable => Future.successful(EmailNotSent)
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
