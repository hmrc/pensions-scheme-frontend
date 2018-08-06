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

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import identifiers.register.SchemeDetailsId
import identifiers.register.establishers.IsEstablisherCompleteId
import identifiers.register.establishers.partnership.{IsPartnershipCompleteId, PartnershipDetailsId, PartnershipReviewId}
import javax.inject.Inject
import models.{Index, NormalMode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.EstablisherPartnership
import utils.{Navigator, SectionComplete}
import views.html.register.establishers.partnership.partnershipReview

import scala.concurrent.Future

class PartnershipReviewController @Inject()(appConfig: FrontendAppConfig,
                                            override val messagesApi: MessagesApi,
                                            @EstablisherPartnership navigator: Navigator,
                                            authenticate: AuthAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            sectionComplete: SectionComplete) extends FrontendController with I18nSupport with Retrievals {

  def onPageLoad(index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      (SchemeDetailsId and PartnershipDetailsId(index)).retrieve.right.map {
        case schemeDetails ~ partnershipDetails =>
          val partners: Seq[String] = request.userAnswers.allPartnersAfterDelete(index).map(_.name)

          Future.successful(Ok(partnershipReview(appConfig, index, schemeDetails.schemeName, partnershipDetails.name, partners)))
      }
  }

  def onSubmit(index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      val allPartners = request.userAnswers.allPartnersAfterDelete(index)
      val allPartnersCompleted = allPartners.nonEmpty & (allPartners.count(!_.isCompleted) == 0)

      val isPartnershipComplete = request.userAnswers.get(IsPartnershipCompleteId(index)).getOrElse(false)

      if (allPartnersCompleted & isPartnershipComplete) {
        sectionComplete.setCompleteFlag(IsEstablisherCompleteId(index), request.userAnswers, value = true).map { _ =>
          Redirect(navigator.nextPage(PartnershipReviewId(index), NormalMode, request.userAnswers))
        }
      }
      else {
        Future.successful(Redirect(navigator.nextPage(PartnershipReviewId(index), NormalMode, request.userAnswers)))
      }
  }

}
