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
import identifiers.register.adviser._
import identifiers.register.{DeclarationDutiesId, IsWorkingKnowledgeCompleteId}
import javax.inject.Inject
import models.address.Address
import models.{CheckMode, NormalMode}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.Reads
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.crypto.ApplicationCrypto
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.Adviser
import utils.checkyouranswers.Ops._
import utils.checkyouranswers.{AddressCYA, CheckYourAnswers}
import utils.{CountryOptions, Navigator, SectionComplete}
import viewmodels.{AnswerSection, Message}
import views.html.check_your_answers

import scala.concurrent.ExecutionContext

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
                                          )(implicit val ec: ExecutionContext) extends FrontendController with I18nSupport {

  def onPageLoad: Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>
      val workingKnowledge = DeclarationDutiesId.row(controllers.routes.WorkingKnowledgeController.onPageLoad(CheckMode).url)
      val optionSeqAnswerSection = request.userAnswers.get(DeclarationDutiesId).flatMap {
        case ddi if ddi =>
          Some(Seq(AnswerSection(None, workingKnowledge)))
        case _ =>
          request.userAnswers.get(AdviserNameId).map { adviserName =>
            implicit def address[I <: TypedIdentifier[Address]](implicit rds: Reads[Address], countryOptions: CountryOptions): CheckYourAnswers[I] =
              AddressCYA(label = Message("adviserAddress.checkYourAnswersLabel", adviserName))()

            val adviserNameRow = AdviserNameId.row(routes.AdviserNameController.onPageLoad(CheckMode).url)
            val adviserEmailRow = AdviserEmailId.row(routes.AdviserEmailAddressController.onPageLoad(CheckMode).url)
              .map(ar => ar.copy(label = Messages(ar.label, adviserName)))
            val adviserPhoneRow = AdviserPhoneId.row(routes.AdviserPhoneController.onPageLoad(CheckMode).url)
              .map(ar => ar.copy(label = Messages(ar.label, adviserName)))
            val adviserAddressRow = AdviserAddressId.row(routes.AdviserAddressController.onPageLoad(CheckMode).url)

            Seq(AnswerSection(None, workingKnowledge ++ adviserNameRow ++ adviserEmailRow ++ adviserPhoneRow ++ adviserAddressRow))
          }
      }
      Ok(
        check_your_answers(
          appConfig,
          optionSeqAnswerSection.getOrElse(Seq.empty),
          controllers.register.adviser.routes.CheckYourAnswersController.onSubmit()
        )
      )
  }

  def onSubmit: Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      sectionComplete.setCompleteFlag(request.externalId, IsWorkingKnowledgeCompleteId, request.userAnswers, value = true).map { _ =>
        Redirect(navigator.nextPage(CheckYourAnswersId, NormalMode, request.userAnswers))
      }
  }
}
