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
import connectors.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions._
import identifiers.register.trustees.ConfirmDeleteTrusteeId
import identifiers.register.trustees.company.CompanyDetailsId
import identifiers.register.trustees.individual.TrusteeDetailsId
import identifiers.register.trustees.partnership.PartnershipDetailsId
import javax.inject.Inject
import models.register.trustees.TrusteeKind
import models.register.trustees.TrusteeKind.{Company, Individual, Partnership}
import models.requests.DataRequest
import models.{Index, NormalMode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.Trustees
import utils.{Navigator, UserAnswers}
import views.html.register.trustees.confirmDeleteTrustee

import scala.concurrent.{ExecutionContext, Future}

class ConfirmDeleteTrusteeController @Inject()(appConfig: FrontendAppConfig,
                                               override val messagesApi: MessagesApi,
                                               authenticate: AuthAction,
                                               getData: DataRetrievalAction,
                                               requireData: DataRequiredAction,
                                               @Trustees navigator: Navigator,
                                               dataCacheConnector: UserAnswersCacheConnector) (implicit val ec: ExecutionContext)
  extends FrontendController with I18nSupport with Retrievals {

  def onPageLoad(index: Index, trusteeKind: TrusteeKind): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      getDeletableTrustee(index, trusteeKind, request.userAnswers) map {
        trustee =>
          if (trustee.isDeleted) {
            Future.successful(Redirect(routes.AlreadyDeletedController.onPageLoad(index, trusteeKind)))
          } else {
            retrieveSchemeName {
              schemeName =>
                Future.successful(
                  Ok(
                    confirmDeleteTrustee(
                      appConfig,
                      trustee.name,
                      routes.ConfirmDeleteTrusteeController.onSubmit(index, trusteeKind)
                    )
                  )
                )
            }
          }
      } getOrElse Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
  }

  def onSubmit(index: Index, trusteeKind: TrusteeKind): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      deleteTrustee(trusteeKind, index) match {
        case Right(futureUserAnswers) =>
          futureUserAnswers.map { userAnswers =>
            Redirect(navigator.nextPage(ConfirmDeleteTrusteeId, NormalMode, userAnswers))
          }
        case Left(result) => result
      }
  }

  private def deleteTrustee(trusteeKind: TrusteeKind, trusteeIndex: Index)(implicit dataRequest: DataRequest[AnyContent]) = {
    trusteeKind match {
      case Company =>
        CompanyDetailsId(trusteeIndex).retrieve.right.map { companyDetails =>
          dataCacheConnector.save(CompanyDetailsId(trusteeIndex), companyDetails.copy(isDeleted = true))
        }
      case Individual =>
        TrusteeDetailsId(trusteeIndex).retrieve.right.map { trusteeDetails =>
          dataCacheConnector.save(TrusteeDetailsId(trusteeIndex), trusteeDetails.copy(isDeleted = true))
        }
      case Partnership =>
        PartnershipDetailsId(trusteeIndex).retrieve.right.map { partnershipDetails =>
          dataCacheConnector.save(PartnershipDetailsId(trusteeIndex), partnershipDetails.copy(isDeleted = true))
        }
      case _ =>
        Left(Future.successful(SeeOther(controllers.routes.SessionExpiredController.onPageLoad().url)))
    }
  }

  private case class DeletableTrustee(name: String, isDeleted: Boolean)

  private def getDeletableTrustee(index: Index, trusteeKind: TrusteeKind, userAnswers: UserAnswers): Option[DeletableTrustee] = {
    trusteeKind match {
      case Individual => userAnswers.get(TrusteeDetailsId(index)).map(details => DeletableTrustee(details.fullName, details.isDeleted))
      case Company => userAnswers.get(CompanyDetailsId(index)).map(details => DeletableTrustee(details.companyName, details.isDeleted))
      case Partnership => userAnswers.get(PartnershipDetailsId(index)).map(details => DeletableTrustee(details.name, details.isDeleted))
      case _ => None
    }
  }
}
