/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers

import config.FrontendAppConfig
import connectors.{MinimalPsaConnector, PensionSchemeVarianceLockConnector, UpdateSchemeCacheConnector}
import controllers.actions._
import forms.DeleteSchemeChangesFormProvider

import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.{MessagesApi, I18nSupport}
import play.api.libs.json.{JsError, JsSuccess}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.deleteSchemeChanges

import scala.concurrent.{ExecutionContext, Future}

class DeleteSchemeChangesController @Inject()(
                                               appConfig: FrontendAppConfig,
                                               override val messagesApi: MessagesApi,
                                               updateConnector: UpdateSchemeCacheConnector,
                                               lockConnector: PensionSchemeVarianceLockConnector,
                                               minimalPsaConnector: MinimalPsaConnector,
                                               authenticate: AuthAction,
                                               getData: DataRetrievalAction,
                                               formProvider: DeleteSchemeChangesFormProvider,
                                               val controllerComponents: MessagesControllerComponents,
                                               view: deleteSchemeChanges
                                             )(implicit val ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Retrievals {

  private lazy val overviewPage = Redirect(appConfig.managePensionsSchemeOverviewUrl)
  private lazy val postCall = routes.DeleteSchemeChangesController.onSubmit _
  private val form: Form[Boolean] = formProvider()

  def onPageLoad(srn: String): Action[AnyContent] = (authenticate() andThen getData()).async {
    implicit request =>
      request.psaId match {
        case Some(psaId) =>
          getSchemeName(srn, psaId.id) { (psaName, schemeName) =>
            Future.successful(Ok(view(form, schemeName, postCall(srn), psaName)))
          }
        case _ => Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad))

      }
  }

      private def getSchemeName(srn: String, psaId: String)(block: (String, String) => Future[Result])
                               (implicit hc: HeaderCarrier): Future[Result] =
        minimalPsaConnector.getPsaNameFromPsaID(psaId).flatMap { psaName =>
          updateConnector.fetch(srn).flatMap { data =>
            (data, psaName) match {

              case (Some(data), Some(psaName)) =>
                (data \ "schemeName").validate[String] match {
                  case JsSuccess(name, _) =>
                    block(psaName, name)
                  case JsError(_) => Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad))
                }
              case _ => Future.successful(overviewPage)
            }
          }
        }

      def onSubmit(srn: String): Action[AnyContent] = (authenticate() andThen getData()).async {
        implicit request =>
          request.psaId.map { psaId =>
            getSchemeName(srn, psaId.id) { (psaName, schemeName) =>
              form.bindFromRequest().fold(
                (formWithErrors: Form[_]) =>
                  Future.successful(BadRequest(view(formWithErrors, schemeName, postCall(srn), psaName))),
                {
                  case true =>
                    updateConnector.removeAll(srn).flatMap { _ =>
                      lockConnector.releaseLock(psaId.id, srn).map(_ => overviewPage)
                    }
                  case false => Future.successful(overviewPage)
                }
              )
            }
          }.getOrElse(Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad)))
      }
  }
