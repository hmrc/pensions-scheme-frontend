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

package controllers.register.establishers.partnership

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import forms.register.establishers.IsDormantFormProvider
import identifiers.register.DeclarationDormantId
import identifiers.register.establishers.IsDormantId
import models.register.DeclarationDormant
import models.register.establishers.Dormancy
import models.register.establishers.Dormancy._
import models.{Mode, NormalMode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.EstablisherPartnership
import utils.{Enumerable, Navigator, UserAnswers}
import views.html.register.establishers.isDormant

import scala.concurrent.Future

class IsPartnershipDormantController @Inject()(appConfig: FrontendAppConfig,
                                               override val messagesApi: MessagesApi,
                                               dataCacheConnector: UserAnswersCacheConnector,
                                               @EstablisherPartnership navigator: Navigator,
                                               authenticate: AuthAction,
                                               getData: DataRetrievalAction,
                                               requireData: DataRequiredAction,
                                               formProvider: IsDormantFormProvider) extends FrontendController
                                                with Enumerable.Implicits with I18nSupport with Retrievals {

  private val form: Form[Dormancy] = formProvider()
  private def postCall(mode: Mode, index: Int): Call = routes.IsPartnershipDormantController.onSubmit(mode, index)

  def onPageLoad(mode: Mode, index: Int): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrievePartnershipName(index) {
        partnershipName =>
          val preparedForm = request.userAnswers.get(IsDormantId(index)).fold(form)(v=> form.fill(v))
          Future.successful(Ok(isDormant(appConfig, preparedForm, partnershipName, postCall(mode, index))))
      }
  }

  def onSubmit(mode: Mode, index: Int): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrievePartnershipName(index) { partnershipName =>
        form.bindFromRequest().fold(
          (formWithErrors: Form[_]) =>
            Future.successful(BadRequest(isDormant(appConfig, formWithErrors, partnershipName, postCall(mode, index)))),
          {
            case Dormant =>
              dataCacheConnector.save(request.externalId, IsDormantId(index), Dormancy.values(0)).flatMap { _ =>
                dataCacheConnector.save(request.externalId, DeclarationDormantId, DeclarationDormant.values(0)).map (cacheMap =>
                 Redirect(navigator.nextPage(IsDormantId(index), NormalMode, UserAnswers(cacheMap))))
             }
            case NotDormant =>
              dataCacheConnector.save(request.externalId, IsDormantId(index), Dormancy.values(1)).map(cacheMap =>
                Redirect(navigator.nextPage(IsDormantId(index), mode, UserAnswers(cacheMap))))

          }

        )
      }
  }

}
