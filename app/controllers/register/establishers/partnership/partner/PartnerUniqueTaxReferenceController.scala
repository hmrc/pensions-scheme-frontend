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

package controllers.register.establishers.partnership.partner

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions._
import forms.register.establishers.partnership.partner.PartnerUniqueTaxReferenceFormProvider
import identifiers.register.establishers.partnership.partner.{PartnerDetailsId, PartnerUniqueTaxReferenceId}
import javax.inject.Inject
import models.{Index, Mode, UniqueTaxReference}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.EstablishersPartner
import utils.{Enumerable, Navigator, UserAnswers}
import views.html.register.establishers.partnership.partner.partnerUniqueTaxReference

import scala.concurrent.{ExecutionContext, Future}

class PartnerUniqueTaxReferenceController @Inject()(
                                                     appConfig: FrontendAppConfig,
                                                     override val messagesApi: MessagesApi,
                                                     dataCacheConnector: UserAnswersCacheConnector,
                                                     @EstablishersPartner navigator: Navigator,
                                                     authenticate: AuthAction,
                                                     getData: DataRetrievalAction,
                                                     requireData: DataRequiredAction,
                                                     formProvider: PartnerUniqueTaxReferenceFormProvider
                                                   ) (implicit val ec: ExecutionContext) extends FrontendController with Retrievals with I18nSupport with Enumerable.Implicits {

  private val form: Form[UniqueTaxReference] = formProvider()

  def onPageLoad(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      PartnerDetailsId(establisherIndex, partnerIndex).retrieve.right.map { _ =>
        val preparedForm = request.userAnswers.get(PartnerUniqueTaxReferenceId(establisherIndex, partnerIndex)).fold(form)(form.fill)
        val submitUrl = controllers.register.establishers.partnership.partner.routes.PartnerUniqueTaxReferenceController.onSubmit(mode, establisherIndex, partnerIndex, srn)
        Future.successful(Ok(partnerUniqueTaxReference(appConfig, preparedForm, mode, establisherIndex, partnerIndex, existingSchemeName, submitUrl)))
      }
  }


  def onSubmit(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      PartnerDetailsId(establisherIndex, partnerIndex).retrieve.right.map { partner =>
        form.bindFromRequest().fold(
          (formWithErrors: Form[_]) => {
            val submitUrl = controllers.register.establishers.partnership.partner.routes.PartnerUniqueTaxReferenceController.onSubmit(mode, establisherIndex, partnerIndex, srn)
            Future.successful(BadRequest(partnerUniqueTaxReference(appConfig, formWithErrors, mode, establisherIndex, partnerIndex, existingSchemeName, submitUrl)))
          },
          (value) =>
            dataCacheConnector.save(
              request.externalId,
              PartnerUniqueTaxReferenceId(establisherIndex, partnerIndex),
              value
            ).map {
              json =>
                Redirect(navigator.nextPage(PartnerUniqueTaxReferenceId(establisherIndex, partnerIndex), mode, new UserAnswers(json)))
            }
        )
      }
  }

}
