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
import connectors.{DataCacheConnector, PensionsSchemeConnector}
import controllers.actions._
import identifiers.register.SubmissionReferenceNumberId
import identifiers.register.adviser.{AdviserAddressId, AdviserDetailsId, CheckYourAnswersId}
import javax.inject.Inject
import models.{CheckMode, NormalMode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.CheckYourAnswers.Ops._
import utils.annotations.Adviser
import utils.{CountryOptions, Navigator}
import viewmodels.AnswerSection
import views.html.check_your_answers

class CheckYourAnswersController @Inject()(appConfig: FrontendAppConfig,
                                         override val messagesApi: MessagesApi,
                                         dataCacheConnector: DataCacheConnector,
                                         authenticate: AuthAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         @Adviser navigator: Navigator,
                                         implicit val countryOptions: CountryOptions,
                                         pensionsSchemeConnector: PensionsSchemeConnector) extends FrontendController with I18nSupport {

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
        dataCacheConnector.save(request.externalId, SubmissionReferenceNumberId, submissionResponse).map { _ =>
          Redirect(navigator.nextPage(CheckYourAnswersId, NormalMode, request.userAnswers))
        }
      )

  }
}
