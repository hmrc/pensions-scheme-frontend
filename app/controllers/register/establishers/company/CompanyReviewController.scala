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

package controllers.register.establishers.company

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import identifiers.register.establishers.{IsEstablisherCompleteId, IsEstablisherNewId}
import identifiers.register.establishers.company.{CompanyDetailsId, CompanyReviewId, IsCompanyCompleteId}
import javax.inject.Inject
import models.{Index, Mode, NormalMode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.{CYA, EstablishersCompany}
import utils.{AllowChangeHelper, Navigator, SectionComplete}
import views.html.register.establishers.company.companyReview

import scala.concurrent.{ExecutionContext, Future}

class CompanyReviewController @Inject()(appConfig: FrontendAppConfig,
                                        override val messagesApi: MessagesApi,
                                        @EstablishersCompany navigator: Navigator,
                                        authenticate: AuthAction,
                                        getData: DataRetrievalAction,
                                        @CYA allowAccess: AllowAccessActionProvider,
                                        requireData: DataRequiredAction,
                                        userAnswersService: UserAnswersService,
                                        allowChangeHelper: AllowChangeHelper
                                       )(implicit val ec: ExecutionContext) extends FrontendController with I18nSupport with Retrievals {

  def onPageLoad(mode: Mode, srn: Option[String], index: Index): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
    implicit request =>
      CompanyDetailsId(index).retrieve.right.map {
        case companyDetails =>
          val directors: Seq[String] = request.userAnswers.allDirectorsAfterDelete(index).map(_.name)

          Future.successful(Ok(companyReview(appConfig,
            index,
            companyDetails.companyName,
            directors,
            existingSchemeName,
            mode,
            srn,
            viewOnly = request.viewOnly,
            hideSaveAndContinueButton = allowChangeHelper.hideSaveAndContinueButton(request, IsEstablisherNewId(index), mode))))
      }
  }

  def onSubmit(mode: Mode,  srn: Option[String], index: Index): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      val allDirectors = request.userAnswers.allDirectorsAfterDelete(index)
      val allDirectorsCompleted = allDirectors.nonEmpty & (allDirectors.count(!_.isCompleted) == 0)

      val isCompanyComplete = request.userAnswers.get(IsCompanyCompleteId(index)).getOrElse(false)

      if (allDirectorsCompleted & isCompanyComplete) {
        userAnswersService.setCompleteFlag(mode, srn, IsEstablisherCompleteId(index), request.userAnswers, value = true).map { _ =>
          Redirect(navigator.nextPage(CompanyReviewId(index), mode, request.userAnswers, srn))
        }
      }
      else {
        Future.successful(Redirect(navigator.nextPage(CompanyReviewId(index), mode, request.userAnswers, srn)))
      }
  }

}
