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

package controllers.register.trustees.individual

import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.Retrievals
import controllers.actions._
import forms.register.IndividualDetailsFormProvider
import identifiers.register.SchemeDetailsId
import identifiers.register.trustees.TrusteeKindId
import identifiers.register.trustees.individual.TrusteeDetailsId
import javax.inject.Inject
import models.register.trustees.TrusteeKind._
import models.{Index, Mode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.TrusteesIndividual
import utils.{Enumerable, Navigator, UserAnswers}
import views.html.register.trustees.individual.trusteeDetails

import scala.concurrent.Future

class TrusteeDetailsController @Inject() (
                                           appConfig: FrontendAppConfig,
                                           override val messagesApi: MessagesApi,
                                           dataCacheConnector: DataCacheConnector,
                                           @TrusteesIndividual navigator: Navigator,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           formProvider: IndividualDetailsFormProvider
                                      ) extends FrontendController with Retrievals with I18nSupport with Enumerable.Implicits {

  private val form = formProvider()

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      SchemeDetailsId.retrieve.right.map {
        schemeDetails =>
          val redirectResult = request.userAnswers.get(TrusteeDetailsId(index)) match {
            case None => Ok(trusteeDetails(appConfig, form, mode, index, schemeDetails.schemeName))
            case Some(value) => Ok(trusteeDetails(appConfig, form.fill(value), mode, index, schemeDetails.schemeName))
          }
          Future.successful(redirectResult)
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      SchemeDetailsId.retrieve.right.map {
        schemeDetails =>
          form.bindFromRequest().fold(
            (formWithErrors: Form[_]) =>
              Future.successful(BadRequest(trusteeDetails(appConfig, formWithErrors, mode, index, schemeDetails.schemeName))),
            (value) =>
                request.userAnswers.upsert(TrusteeDetailsId(index))(value){
                  _.upsert(TrusteeKindId(index))(Individual){ answers =>
                    dataCacheConnector.upsert(request.externalId, answers.json).map{ cacheMap =>
                      Redirect(navigator.nextPage(TrusteeDetailsId(index), mode, new UserAnswers(cacheMap)))
                    }
                  }
                }
          )
      }
  }

}
