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

package controllers.register.trustees

import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.Retrievals
import controllers.actions._
import identifiers.register.SchemeDetailsId
import identifiers.register.trustees.company.CompanyDetailsId
import identifiers.register.trustees.individual.TrusteeDetailsId
import identifiers.register.trustees.{ConfirmDeleteTrusteeId, TrusteesId}
import javax.inject.Inject
import models.register.trustees.TrusteeKind
import models.register.trustees.TrusteeKind.{Company, Individual}
import models.requests.DataRequest
import models.{Index, NormalMode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.Trustees
import utils.{Navigator2, UserAnswers}
import views.html.register.trustees.confirmDeleteTrustee

import scala.concurrent.Future

class ConfirmDeleteTrusteeController @Inject()(appConfig: FrontendAppConfig,
                                               override val messagesApi: MessagesApi,
                                               authenticate: AuthAction,
                                               getData: DataRetrievalAction,
                                               requireData: DataRequiredAction,
                                               @Trustees navigator: Navigator2,
                                               dataCacheConnector: DataCacheConnector) extends FrontendController with I18nSupport with Retrievals {

  def onPageLoad(index: Index, trusteeKind: TrusteeKind): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      SchemeDetailsId.retrieve.right.map { schemeDetails =>
        trusteeName(index, trusteeKind) match {
          case Right(trusteeName) => Future.successful(
            Ok(
              confirmDeleteTrustee(
                appConfig,
                schemeDetails.schemeName,
                trusteeName,
                postCall(index, trusteeKind)
              )
            )
          )
          case Left(result) => result
        }
      }
  }

  def onSubmit(index: Index, trusteeKind: TrusteeKind): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      deleteTrustee(trusteeKind, index) match {
        case Right(futureUserAnswers) =>
          futureUserAnswers.map { userAnswers =>
            Redirect(navigator.nextPage(ConfirmDeleteTrusteeId, NormalMode, userAnswers))
          }
        case Left(result) =>
          result
      }
  }

  private def deleteTrustee(trusteeKind: TrusteeKind, establisherIndex: Index)(implicit dataRequest: DataRequest[AnyContent]):
  Either[Future[Result], Future[UserAnswers]] = {
    trusteeKind match {
      case Company =>
        CompanyDetailsId(establisherIndex).retrieve.right.map { companyDetails =>
          dataCacheConnector.save(CompanyDetailsId(establisherIndex), companyDetails.copy(isDeleted = true))
        }
      case Individual =>
        TrusteeDetailsId(establisherIndex).retrieve.right.map { establisherDetails =>
          dataCacheConnector.save(TrusteeDetailsId(establisherIndex), establisherDetails.copy(isDeleted = true))
        }
      case _ =>
        Left(Future.successful(SeeOther(controllers.routes.SessionExpiredController.onPageLoad().url)))
    }
  }

  private def trusteeName(index: Index, trusteeKind: TrusteeKind)(implicit dataRequest: DataRequest[AnyContent]): Either[Future[Result], String] = {
    trusteeKind match {
      case Company => CompanyDetailsId(index).retrieve.right.map(_.companyName)
      case Individual => TrusteeDetailsId(index).retrieve.right.map(_.fullName)
      case invalid => Left(Future.successful(BadRequest(s"Invalid trustee kind $invalid")))
    }
  }

  private def postCall(index: Index, trusteeKind: TrusteeKind) = {
    routes.ConfirmDeleteTrusteeController.onSubmit(index, trusteeKind)
  }

}
