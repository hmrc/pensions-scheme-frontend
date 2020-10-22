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
import connectors.{PensionAdministratorConnector, UserAnswersCacheConnector}
import controllers.Retrievals
import controllers.actions._
import identifiers.SchemeTypeId
import identifiers.register.SubmissionReferenceNumberId
import javax.inject.Inject
import models.register.SchemeType.MasterTrust
import models.requests.DataRequest
import java.time.LocalDate
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.register.schemeSuccess

import scala.concurrent.{ExecutionContext, Future}

class SchemeSuccessController @Inject()(appConfig: FrontendAppConfig,
                                        override val messagesApi: MessagesApi,
                                        cacheConnector: UserAnswersCacheConnector,
                                        authenticate: AuthAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        pensionAdministratorConnector: PensionAdministratorConnector,
                                        val controllerComponents: MessagesControllerComponents,
                                        val view: schemeSuccess
                                       )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController with I18nSupport with Retrievals {

  def onPageLoad: Action[AnyContent] = (authenticate() andThen getData() andThen requireData).async {
    implicit request =>

      pensionAdministratorConnector.getPSAEmail.flatMap { email =>
        SubmissionReferenceNumberId.retrieve.right.map { submissionReferenceNumber =>
          cacheConnector.removeAll(request.externalId).flatMap { _ =>
            Future.successful(Ok(
              view(
                LocalDate.now(),
                submissionReferenceNumber.schemeReferenceNumber,
                request.userAnswers.get(SchemeTypeId).contains(MasterTrust),
                email
              )
            ))
          }
        }
      }
  }

  def onSubmit: Action[AnyContent] = authenticate() {
    implicit request =>
      Redirect(appConfig.managePensionsSchemeOverviewUrl)
  }
}
