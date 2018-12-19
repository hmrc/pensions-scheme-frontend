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

package controllers.register

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import identifiers.register.establishers.EstablisherKindId
import identifiers.register.trustees.HaveAnyTrusteesId
import identifiers.register.{SchemeDetailsId, SchemeReviewId}
import javax.inject.Inject
import models.{CheckMode, NormalMode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.Register
import utils.{Enumerable, Navigator}
import views.html.register.schemeReview

import scala.concurrent.{ExecutionContext, Future}

class SchemeReviewController @Inject()(appConfig: FrontendAppConfig,
                                       override val messagesApi: MessagesApi,
                                       @Register navigator: Navigator,
                                       authenticate: AuthAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction
                                      )(implicit val ec: ExecutionContext) extends FrontendController with I18nSupport with Enumerable.Implicits with Retrievals {

  def onPageLoad: Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      SchemeDetailsId.retrieve.right.map { schemeDetails =>
          val establishers = request.userAnswers.allEstablishersAfterDelete.map(_.name)
          val trustees = request.userAnswers.allTrusteesAfterDelete.map(_.name)

          Future.successful(Ok(schemeReview(appConfig, schemeDetails.schemeName, establishers, trustees,
            controllers.register.establishers.routes.AddEstablisherController.onPageLoad(NormalMode),
            trusteeEditUrl(request.userAnswers.get(HaveAnyTrusteesId)))))
      }
  }

  private def trusteeEditUrl(haveAnyTrustees: Option[Boolean]) = {
    haveAnyTrustees match {
      case Some(false) =>
        controllers.register.trustees.routes.HaveAnyTrusteesController.onPageLoad(NormalMode)
      case _ =>
        controllers.register.trustees.routes.AddTrusteeController.onPageLoad(CheckMode)
    }
  }

  def onSubmit: Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>
      Redirect(navigator.nextPage(SchemeReviewId, NormalMode, request.userAnswers))
  }

}
