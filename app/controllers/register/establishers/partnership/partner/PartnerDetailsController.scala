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

package controllers.register.establishers.partnership.partner

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import forms.register.PersonDetailsFormProvider
import identifiers.register.establishers.IsEstablisherCompleteId
import identifiers.register.establishers.partnership.PartnershipDetailsId
import identifiers.register.establishers.partnership.partner.PartnerDetailsId
import javax.inject.Inject
import models.person.PersonDetails
import models.{Index, Mode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.EstablishersPartner
import utils.{Navigator, SectionComplete, UserAnswers}
import views.html.register.establishers.partnership.partner.partnerDetails

import scala.concurrent.Future

class PartnerDetailsController @Inject()(
                                          appConfig: FrontendAppConfig,
                                          override val messagesApi: MessagesApi,
                                          dataCacheConnector: UserAnswersCacheConnector,
                                          @EstablishersPartner navigator: Navigator,
                                          authenticate: AuthAction,
                                          getData: DataRetrievalAction,
                                          requireData: DataRequiredAction,
                                          formProvider: PersonDetailsFormProvider,
                                          sectionComplete: SectionComplete
                                        ) extends FrontendController with Retrievals with I18nSupport {

  private val form = formProvider()

  def onPageLoad(mode: Mode, establisherIndex: Index, partnerIndex: Index): Action[AnyContent] =
    (authenticate andThen getData andThen requireData).async {
      implicit request =>
        PartnershipDetailsId(establisherIndex).retrieve.right.map { partnershipDetails =>
          val preparedForm = request.userAnswers.get[PersonDetails](PartnerDetailsId(establisherIndex, partnerIndex)) match {
            case None => form
            case Some(value) => form.fill(value)
          }
          Future.successful(Ok(partnerDetails(appConfig, preparedForm, mode, establisherIndex, partnerIndex, partnershipDetails.name)))
        }
    }

  def onSubmit(mode: Mode, establisherIndex: Index, partnerIndex: Index): Action[AnyContent] =
    (authenticate andThen getData andThen requireData).async {
      implicit request =>
        PartnershipDetailsId(establisherIndex).retrieve.right.map { partnershipDetails =>
          form.bindFromRequest().fold(
            (formWithErrors: Form[_]) =>
              Future.successful(BadRequest(partnerDetails(appConfig, formWithErrors, mode, establisherIndex, partnerIndex, partnershipDetails.name)))
            ,
            value =>
              dataCacheConnector.save(request.externalId, PartnerDetailsId(establisherIndex, partnerIndex), value).flatMap {
                cacheMap =>
                  val userAnswers = UserAnswers(cacheMap)
                  val allPartners = userAnswers.allPartnersAfterDelete(establisherIndex)
                  val allPartnersCompleted = allPartners.count(_.isCompleted) == allPartners.size

                  if (allPartnersCompleted) {
                    Future.successful(Redirect(navigator.nextPage(PartnerDetailsId(establisherIndex, partnerIndex), mode, userAnswers)))
                  } else {
                    sectionComplete.setCompleteFlag(IsEstablisherCompleteId(establisherIndex), userAnswers, value = false).map { _ =>
                      Redirect(navigator.nextPage(PartnerDetailsId(establisherIndex, partnerIndex), mode, userAnswers))
                    }
                  }
              }
          )
        }
    }
}
