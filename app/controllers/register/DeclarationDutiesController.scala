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
import connectors.{DataCacheConnector, EmailConnector, PensionsSchemeConnector}
import controllers.Retrievals
import controllers.actions._
import forms.register.DeclarationDutiesFormProvider
import identifiers.register.{DeclarationDutiesId, SubmissionReferenceNumberId}
import javax.inject.Inject
import models.NormalMode
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.Register
import utils.{Enumerable, Navigator, UserAnswers}
import views.html.register.declarationDuties

import scala.concurrent.Future

class DeclarationDutiesController @Inject()(
                                             appConfig: FrontendAppConfig,
                                             override val messagesApi: MessagesApi,
                                             dataCacheConnector: DataCacheConnector,
                                             @Register navigator: Navigator,
                                             authenticate: AuthAction,
                                             getData: DataRetrievalAction,
                                             requireData: DataRequiredAction,
                                             formProvider: DeclarationDutiesFormProvider,
                                             pensionsSchemeConnector: PensionsSchemeConnector,
                                             emailConnector: EmailConnector
                                           ) extends FrontendController with I18nSupport with Retrievals with Enumerable.Implicits {

  private val form = formProvider()

  def onPageLoad: Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveSchemeName {
        schemeName =>
          val result = request.userAnswers.get(DeclarationDutiesId) match {
            case Some(value) => Ok(declarationDuties(appConfig, form.fill(value), schemeName))
            case None => Ok(declarationDuties(appConfig, form, schemeName))
          }
          Future.successful(result)
      }
  }

  def onSubmit: Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveSchemeName {
        schemeName =>
          form.bindFromRequest().fold(
            (formWithErrors: Form[_]) =>
              Future.successful(BadRequest(declarationDuties(appConfig, formWithErrors, schemeName))),
            {
              case true =>
                dataCacheConnector.save(request.externalId, DeclarationDutiesId, true).flatMap { cacheMap =>
                  pensionsSchemeConnector.registerScheme(UserAnswers(cacheMap), request.psaId.id).flatMap { submissionResponse =>
                    dataCacheConnector.save(request.externalId, SubmissionReferenceNumberId, submissionResponse).flatMap { cacheMap =>
                      emailConnector.sendEmail("", "pods_psa_register", Map("srn" -> submissionResponse.schemeReferenceNumber)).map { _ =>
                        Redirect(navigator.nextPage(DeclarationDutiesId, NormalMode, UserAnswers(cacheMap)))
                      }
                    }
                  }
                }
              case false =>
                dataCacheConnector.save(request.externalId, DeclarationDutiesId, false).map(cacheMap =>
                  Redirect(navigator.nextPage(DeclarationDutiesId, NormalMode, UserAnswers(cacheMap))))
            }
          )
      }
  }
}
