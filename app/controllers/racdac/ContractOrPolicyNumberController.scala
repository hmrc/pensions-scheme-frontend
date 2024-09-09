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

package controllers.racdac

import connectors.{PensionAdministratorConnector, UserAnswersCacheConnector}
import controllers.actions._
import forms.racdac.ContractOrPolicyNumberFormProvider
import identifiers.racdac.ContractOrPolicyNumberId
import models.{Mode, SchemeReferenceNumber}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.UserAnswers
import utils.annotations.Racdac
import views.html.racdac.contractOrPolicyNumber

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ContractOrPolicyNumberController @Inject()(
                                                  override val messagesApi: MessagesApi,
                                                  @Racdac dataCacheConnector: UserAnswersCacheConnector,
                                                  navigator: Navigator,
                                                  authenticate: AuthAction,
                                                  @Racdac getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  allowAccess: AllowAccessActionProvider,
                                                  formProvider: ContractOrPolicyNumberFormProvider,
                                                  pensionAdministratorConnector: PensionAdministratorConnector,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  val view: contractOrPolicyNumber
                                    )(implicit val executionContext: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport
    with Retrievals
    with controllers.Retrievals
{

  def onPageLoad(mode: Mode, srn: SchemeReferenceNumber): Action[AnyContent] = (authenticate() andThen getData() andThen allowAccess(srn) andThen requireData).async {
    implicit request => {
      withRACDACName{ racdacName =>
        val form = formProvider(racdacName)
        val preparedForm = request.userAnswers.get(ContractOrPolicyNumberId).fold(form)(v => form.fill(v))
          pensionAdministratorConnector.getPSAName.map { psaName =>
            Ok(view(preparedForm, mode, psaName, racdacName, srn))
          }
      }
    }
  }

  def onSubmit(mode: Mode, srn: SchemeReferenceNumber): Action[AnyContent] = (authenticate() andThen getData() andThen allowAccess(srn) andThen requireData).async {
    implicit request =>
      withRACDACName { racdacName =>
        val form = formProvider(racdacName)
        form.bindFromRequest().fold(
          (formWithErrors: Form[_]) => {
            pensionAdministratorConnector.getPSAName.map { psaName =>
              BadRequest(view(formWithErrors, mode, psaName, racdacName, srn))
            }
          },
          value =>
            dataCacheConnector.save(request.externalId, ContractOrPolicyNumberId, value).map {
              cacheMap => Redirect(navigator.nextPage(ContractOrPolicyNumberId, mode, UserAnswers(cacheMap), srn))
            }
        )
      }
  }
}
