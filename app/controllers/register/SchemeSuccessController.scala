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

package controllers.register

import config.FrontendAppConfig
import connectors.{PensionAdministratorConnector, UserAnswersCacheConnector}
import controllers.Retrievals
import controllers.actions._
import identifiers.SchemeTypeId
import identifiers.racdac.{ContractOrPolicyNumberId, RACDACNameId}
import identifiers.register.SubmissionReferenceNumberId
import models.SchemeReferenceNumber
import models.register.SchemeType.MasterTrust
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsError, JsResult, JsSuccess}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.UserAnswers
import views.html.register.schemeSuccess

import java.time.LocalDate
import javax.inject.Inject
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

  def onPageLoad(srn: SchemeReferenceNumber): Action[AnyContent] = (authenticate() andThen getData(srn=srn) andThen requireData).async {
    implicit request =>

      pensionAdministratorConnector.getPSAEmail.flatMap { email =>
        SubmissionReferenceNumberId.retrieve.map { submissionReferenceNumber =>
          val newUserAnswers: JsResult[UserAnswers] = {
            request.userAnswers.get(RACDACNameId) match {
              case Some(racDacName) =>
                val contractOrPolicyNumberOption: Option[String] = request.userAnswers.get(ContractOrPolicyNumberId)
                  val updateUA: JsResult[UserAnswers] = UserAnswers().set(RACDACNameId)(racDacName)
                if (contractOrPolicyNumberOption.isDefined) {
                  updateUA.flatMap(_.set(ContractOrPolicyNumberId)(contractOrPolicyNumberOption.get))
                } else updateUA
              case None => JsSuccess(UserAnswers())
            }
          }
          cacheConnector.removeAll(request.externalId).flatMap { _ =>
            newUserAnswers match {
              case JsSuccess(value, _) =>
                cacheConnector.upsert(request.externalId, value.json)
                  .map(_ => Ok(view(
                    LocalDate.now(),
                    submissionReferenceNumber.schemeReferenceNumber,
                    request.userAnswers.get(SchemeTypeId).contains(MasterTrust),
                    email
                  )))
              case JsError(_) => Future(Redirect(controllers.routes.SessionExpiredController.onPageLoad))
            }
          }
        }
      }
  }


  def onSubmit(srn: SchemeReferenceNumber): Action[AnyContent] = authenticate() {
    Redirect(appConfig.managePensionsSchemeOverviewUrl)
  }
}
