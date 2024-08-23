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

package controllers

import config.FrontendAppConfig
import connectors.{MinimalPsaConnector, UserAnswersCacheConnector}
import controllers.actions._
import forms.DeleteSchemeFormProvider
import identifiers.SchemeNameId
import models.SchemeReferenceNumber
import models.requests.OptionalDataRequest
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.UserAnswers
import views.html.deleteScheme

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DeleteSchemeController @Inject()(
                                        appConfig: FrontendAppConfig,
                                        override val messagesApi: MessagesApi,
                                        dataCacheConnector: UserAnswersCacheConnector,
                                        minimalPsaConnector: MinimalPsaConnector,
                                        authenticate: AuthAction,
                                        getData: DataRetrievalAction,
                                        formProvider: DeleteSchemeFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: deleteScheme
                                      )(implicit val ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Retrievals {

  private val form: Form[Boolean] = formProvider()
  private val overviewPage = Redirect(appConfig.managePensionsSchemeOverviewUrl)
  private val sessionExpired: Future[Result] = Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad))


  def onPageLoad(srn: SchemeReferenceNumber): Action[AnyContent] = (authenticate() andThen getData(srn = srn)).async {
    implicit request =>
      getSchemeInfo { (schemeName, psaName, hintTextMessageKey) =>
              Future.successful(Ok(view(form, schemeName, psaName, hintTextMessageKey)))
      }
  }

  def onSubmit(srn: SchemeReferenceNumber): Action[AnyContent] = (authenticate() andThen getData(srn = srn)).async {
    implicit request =>
      getSchemeInfo { (schemeName, psaName, hintTextMessageKey) =>
        form.bindFromRequest().fold(
          (formWithErrors: Form[_]) =>
            Future.successful(BadRequest(view(formWithErrors, schemeName, psaName, hintTextMessageKey))),
          {
            case true => dataCacheConnector.removeAll(request.externalId).map {
              _ =>
                overviewPage
            }
            case false => Future.successful(overviewPage)
          }
        )
      }
  }


  private def getSchemeInfo(f: (String, String, String) => Future[Result])
                           (implicit request: OptionalDataRequest[AnyContent]): Future[Result] = {

    dataCacheConnector.fetch(request.externalId).flatMap {
      case None => sessionExpired
      case Some(data) =>
        val ua = UserAnswers(data)
        val schemeName = ua.get(SchemeNameId)
        request.psaId.map { psaId =>
          minimalPsaConnector.getPsaNameFromPsaID(psaId.id).flatMap(_.map { psaName =>
            f(schemeName.getOrElse("messages__thisScheme"), psaName,
              "messages__deleteScheme__hint")
        }.getOrElse(sessionExpired))
      }.getOrElse(sessionExpired)
    }
  }
}

