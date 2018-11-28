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

package controllers.register.establishers.company

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import forms.register.establishers.IsDormantFormProvider
import identifiers.register.DeclarationDormantId
import identifiers.register.establishers.company.IsCompanyDormantId
import models.register.DeclarationDormant
import models.register.DeclarationDormant._
import models.{Mode, NormalMode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.EstablishersCompany
import utils.{Enumerable, Navigator, UserAnswers}
import views.html.register.establishers.isDormant

import scala.concurrent.Future

class IsCompanyDormantController @Inject()(appConfig: FrontendAppConfig,
                                 override val messagesApi: MessagesApi,
                                 dataCacheConnector: UserAnswersCacheConnector,
                                 @EstablishersCompany navigator: Navigator,
                                 authenticate: AuthAction,
                                 getData: DataRetrievalAction,
                                 requireData: DataRequiredAction,
                                 formProvider: IsDormantFormProvider) extends FrontendController with Enumerable.Implicits with I18nSupport with Retrievals {

  private val form: Form[DeclarationDormant] = formProvider()
  private def postCall(mode: Mode, index: Int): Call = routes.IsCompanyDormantController.onSubmit(mode, index)

  def onPageLoad(mode: Mode, index: Int): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveCompanyName(index) {
        companyName =>
          val preparedForm = request.userAnswers.get(IsCompanyDormantId(index)).fold(form)(v=> form.fill(v))
          Future.successful(Ok(isDormant(appConfig, preparedForm, companyName, postCall(mode, index))))
      }
  }

  def onSubmit(mode: Mode, index: Int): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveCompanyName(index) { companyName =>
        form.bindFromRequest().fold(
          (formWithErrors: Form[_]) =>
            Future.successful(BadRequest(isDormant(appConfig, formWithErrors, companyName, postCall(mode, index)))),
          {
            case Yes =>
              dataCacheConnector.save(request.externalId, IsCompanyDormantId(index), DeclarationDormant.values(1)).flatMap { _ =>
                dataCacheConnector.save(request.externalId, DeclarationDormantId, DeclarationDormant.values(1)).map (cacheMap =>
                 Redirect(navigator.nextPage(IsCompanyDormantId(index), NormalMode, UserAnswers(cacheMap))))
             }
            case No =>
              dataCacheConnector.save(request.externalId, IsCompanyDormantId(index), DeclarationDormant.values(0)).map(cacheMap =>
                Redirect(navigator.nextPage(IsCompanyDormantId(index), mode, UserAnswers(cacheMap))))

          }

        )
      }
  }

}
