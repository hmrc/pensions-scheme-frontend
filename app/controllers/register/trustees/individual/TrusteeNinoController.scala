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

package controllers.register.trustees.individual

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions._
import forms.register.trustees.individual.TrusteeNinoFormProvider
import identifiers.register.trustees.individual.{TrusteeDetailsId, TrusteeNinoId}
import javax.inject.Inject
import models.{Index, Mode, Nino}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{Navigator}
import utils.annotations.TrusteesIndividual
import views.html.register.trustees.individual.trusteeNino

import scala.concurrent.{ExecutionContext, Future}

class TrusteeNinoController @Inject()(appConfig: FrontendAppConfig,
                                      override val messagesApi: MessagesApi,
                                      authenticate: AuthAction,
                                      getData: DataRetrievalAction,
                                      requireData: DataRequiredAction,
                                      @TrusteesIndividual navigator: Navigator,
                                      dataCacheConnector: UserAnswersCacheConnector) (implicit val ec: ExecutionContext)
  extends FrontendController with I18nSupport with Retrievals {

  private val form: Form[Nino] = new TrusteeNinoFormProvider()()

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      TrusteeDetailsId(index).retrieve.right.map { trusteeDetails =>
        val filledForm = request.userAnswers.get(TrusteeNinoId(index)).fold(form)(form.fill)
        val submitUrl = controllers.register.trustees.individual.routes.TrusteeNinoController.onSubmit(mode, index, srn)
        Future.successful(Ok(trusteeNino(appConfig, filledForm, mode, index, existingSchemeName, submitUrl)))
      }
  }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        errors => TrusteeDetailsId(index).retrieve.right.map { trusteeDetails =>
          val submitUrl = controllers.register.trustees.individual.routes.TrusteeNinoController.onSubmit(mode, index, srn)
          Future.successful(BadRequest(trusteeNino(appConfig, errors, mode, index, existingSchemeName, submitUrl)))
        },
        nino => dataCacheConnector.save(TrusteeNinoId(index), nino).map { userAnswers =>
          Redirect(navigator.nextPage(TrusteeNinoId(index), mode, userAnswers))
        }
      )
  }

}
