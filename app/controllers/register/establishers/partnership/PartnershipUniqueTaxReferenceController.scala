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
import controllers.actions._
import forms.register.establishers.partnership.PartnershipUniqueTaxReferenceFormProvider
import identifiers.register.establishers.partnership.PartnershipUniqueTaxReferenceID
import javax.inject.Inject
import models.{Index, Mode, UniqueTaxReference}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.EstablisherPartnership
import utils.{Enumerable, IDataFromRequest, Navigator, UserAnswers}
import views.html.register.establishers.partnership.partnershipUniqueTaxReference

import scala.concurrent.{ExecutionContext, Future}

class PartnershipUniqueTaxReferenceController @Inject()(
                                                         appConfig: FrontendAppConfig,
                                                         override val messagesApi: MessagesApi,
                                                         dataCacheConnector: UserAnswersCacheConnector,
                                                         authenticate: AuthAction,
                                                         @EstablisherPartnership navigator: Navigator,
                                                         getData: DataRetrievalAction,
                                                         requireData: DataRequiredAction,
                                                         formProvider: PartnershipUniqueTaxReferenceFormProvider
                                                       ) (implicit val ec: ExecutionContext) extends FrontendController with Retrievals with IDataFromRequest with I18nSupport with Enumerable.Implicits {

  private val form: Form[UniqueTaxReference] = formProvider()

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrievePartnershipName(index) {
        partnershipName =>
          val redirectResult = request.userAnswers.get(PartnershipUniqueTaxReferenceID(index)) match {
            case None =>
              Ok(partnershipUniqueTaxReference(appConfig, form, mode, index, existingSchemeName))
            case Some(value) =>
              Ok(partnershipUniqueTaxReference(appConfig, form.fill(value), mode, index, existingSchemeName))
          }
          Future.successful(redirectResult)
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrievePartnershipName(index) {
        partnershipName =>
          form.bindFromRequest().fold(
            (formWithErrors: Form[_]) =>
              Future.successful(BadRequest(partnershipUniqueTaxReference(appConfig, formWithErrors, mode, index,existingSchemeName))),
            value =>
              dataCacheConnector.save(
                request.externalId,
                PartnershipUniqueTaxReferenceID(index),
                value
              ).map {
                json =>
                  Redirect(navigator.nextPage(PartnershipUniqueTaxReferenceID(index), mode, UserAnswers(json)))
              }
          )
      }
  }

}
