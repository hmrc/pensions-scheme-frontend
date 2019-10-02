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
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import identifiers.register.establishers.IsEstablisherNewId
import identifiers.register.establishers.partnership.{PartnershipDetailsId, PartnershipReviewId}
import javax.inject.Inject
import models.{Index, Mode}
import navigators.Navigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.AllowChangeHelper
import utils.annotations.{EstablisherPartnership, NoSuspendedCheck}
import views.html.register.establishers.partnership.partnershipReview

import scala.concurrent.{ExecutionContext, Future}

class PartnershipReviewController @Inject()(appConfig: FrontendAppConfig,
                                            override val messagesApi: MessagesApi,
                                            @EstablisherPartnership navigator: Navigator,
                                            authenticate: AuthAction,
                                            getData: DataRetrievalAction,
                                            @NoSuspendedCheck allowAccess: AllowAccessActionProvider,
                                            requireData: DataRequiredAction,
                                            userAnswersService: UserAnswersService,
                                            allowChangeHelper: AllowChangeHelper)(implicit val ec: ExecutionContext)
  extends FrontendController with I18nSupport with Retrievals {

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
    implicit request =>
      PartnershipDetailsId(index).retrieve.right.map {
        case partnershipDetails =>
          val partners: Seq[String] = request.userAnswers.allPartnersAfterDelete(index, false).map(_.name)

          Future.successful(Ok(partnershipReview(appConfig,
            index,
            partnershipDetails.name,
            partners,
            existingSchemeName,
            srn,
            mode,
            viewOnly = request.viewOnly,
            hideSaveAndContinueButton = allowChangeHelper.hideSaveAndContinueButton(request, IsEstablisherNewId(index), mode))))
      }
  }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
        Future.successful(Redirect(navigator.nextPage(PartnershipReviewId(index), mode, request.userAnswers, srn)))
  }

}
