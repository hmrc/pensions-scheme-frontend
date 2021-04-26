/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.racdac

import connectors.{PensionAdministratorConnector, UserAnswersCacheConnector}
import controllers.actions._
import forms.racdac.RACDACContractOrPolicyNumberFormProvider
import identifiers.racdac.{RACDACContractOrPolicyNumberId, RACDACNameId}
import models.Mode
import models.requests.DataRequest
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.UserAnswers
import views.html.racdac.racDACContractOrPolicyNumber

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RACDACContractOrPolicyNumberController @Inject()(
                                      override val messagesApi: MessagesApi,
                                      dataCacheConnector: UserAnswersCacheConnector,
                                      navigator: Navigator,
                                      authenticate: AuthAction,
                                      getData: DataRetrievalAction,
                                      requireData: DataRequiredAction,
                                      formProvider: RACDACContractOrPolicyNumberFormProvider,
                                      pensionAdministratorConnector: PensionAdministratorConnector,
                                      val controllerComponents: MessagesControllerComponents,
                                      val view: racDACContractOrPolicyNumber
                                    )(implicit val executionContext: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport
    with Retrievals {

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate() andThen getData() andThen requireData).async {
    implicit request => {
      withRACDACName{ racdacName =>
        val form = formProvider(racdacName)
        val preparedForm = request.userAnswers.get(RACDACContractOrPolicyNumberId).fold(form)(v => form.fill(v))
          pensionAdministratorConnector.getPSAName.map { psaName =>
            Ok(view(preparedForm, mode, psaName, racdacName))
          }
      }
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate() andThen getData() andThen requireData).async {
    implicit request =>
      withRACDACName { racdacName =>
        val form = formProvider(racdacName)
        form.bindFromRequest().fold(
          (formWithErrors: Form[_]) => {
            pensionAdministratorConnector.getPSAName.map { psaName =>
              BadRequest(view(formWithErrors, mode, psaName, racdacName))
            }
          },
          value =>
            dataCacheConnector.save(request.externalId, RACDACContractOrPolicyNumberId, value).map {
              cacheMap => Redirect(navigator.nextPage(RACDACContractOrPolicyNumberId, mode, UserAnswers(cacheMap)))
            }
        )
      }
  }

  private def withRACDACName(func: String => Future[Result])(implicit request: DataRequest[AnyContent]):Future[Result] = {
    request.userAnswers.get(RACDACNameId) match {
      case None => Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
      case Some(racdacName) => func(racdacName)
    }
  }
}
