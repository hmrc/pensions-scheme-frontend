/*
 * Copyright 2019 HM Revenue & Customs
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

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import forms.register.PartnershipDetailsFormProvider
import identifiers.register.establishers.partnership.PartnershipDetailsId
import javax.inject.Inject
import models.{Index, Mode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.EstablisherPartnership
import utils.{Enumerable, Navigator, UserAnswers}
import views.html.register.establishers.partnership.partnershipDetails

import scala.concurrent.{ExecutionContext, Future}

class PartnershipDetailsController @Inject()(
                                              appConfig: FrontendAppConfig,
                                              override val messagesApi: MessagesApi,
                                              dataCacheConnector: UserAnswersCacheConnector,
                                              @EstablisherPartnership navigator: Navigator,
                                              authenticate: AuthAction,
                                              getData: DataRetrievalAction,
                                              requireData: DataRequiredAction,
                                              formProvider: PartnershipDetailsFormProvider
                                            ) (implicit val ec: ExecutionContext) extends FrontendController with Retrievals with I18nSupport with Enumerable.Implicits {

  private val form = formProvider()

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveSchemeName {
        schemeName =>
          val redirectResult = request.userAnswers
            .get(PartnershipDetailsId(index)) match {
            case None =>
              Ok(partnershipDetails(appConfig, form, mode, index))
            case Some(value) =>
              Ok(partnershipDetails(appConfig, form.fill(value), mode, index))
          }
          Future.successful(redirectResult)
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveSchemeName {
        schemeName =>
          form.bindFromRequest().fold(
            (formWithErrors: Form[_]) =>
              Future.successful(BadRequest(partnershipDetails(appConfig, formWithErrors, mode, index))),
            value =>
              dataCacheConnector.save(request.externalId, PartnershipDetailsId(index), value
              ).map {
                json =>
                  Redirect(navigator.nextPage(PartnershipDetailsId(index), mode, UserAnswers(json)))
              }
          )
      }
  }
}
