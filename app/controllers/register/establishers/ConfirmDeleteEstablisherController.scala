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

package controllers.register.establishers

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions._
import identifiers.register.establishers.ConfirmDeleteEstablisherId
import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.establishers.individual.EstablisherDetailsId
import identifiers.register.establishers.partnership.PartnershipDetailsId
import javax.inject.Inject
import models.register.establishers.EstablisherKind
import models.register.establishers.EstablisherKind._
import models.requests.DataRequest
import models.{Index, NormalMode}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.Establishers
import utils.{Navigator, UserAnswers}
import views.html.register.establishers.confirmDeleteEstablisher

import scala.concurrent.{ExecutionContext, Future}

class ConfirmDeleteEstablisherController @Inject()(
                                                    appConfig: FrontendAppConfig,
                                                    override val messagesApi: MessagesApi,
                                                    dataCacheConnector: UserAnswersCacheConnector,
                                                    @Establishers navigator: Navigator,
                                                    authenticate: AuthAction,
                                                    getData: DataRetrievalAction,
                                                    requireData: DataRequiredAction
                                                  ) (implicit val ec: ExecutionContext) extends FrontendController with I18nSupport with Retrievals {

  private def getHintText(establisherKind: EstablisherKind):Option[String] = {
    establisherKind match {
      case EstablisherKind.Company =>
        Some(Messages(s"messages__confirmDeleteEstablisher__companyHint"))
      case EstablisherKind.Partnership =>
        Some(Messages(s"messages__confirmDeleteEstablisher__partnershipHint"))
      case _ => None
    }
  }

  def onPageLoad(index: Index, establisherKind: EstablisherKind): Action[AnyContent] =
    (authenticate andThen getData andThen requireData).async {
      implicit request =>
        getDeletableEstablisher(index, establisherKind, request.userAnswers) map {
          establisher =>
            if (establisher.isDeleted) {
              Future.successful(Redirect(routes.AlreadyDeletedController.onPageLoad(index, establisherKind)))
            } else {
              retrieveSchemeName {
                schemeName =>
                  Future.successful(
                    Ok(
                      confirmDeleteEstablisher(
                        appConfig,
                        establisher.name,
                        getHintText(establisherKind),
                        routes.ConfirmDeleteEstablisherController.onSubmit(index, establisherKind),
                        routes.AddEstablisherController.onPageLoad(NormalMode)
                      )
                    )
                  )
              }
            }
        } getOrElse Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
    }


  def onSubmit(establisherIndex: Index, establisherKind: EstablisherKind): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      deleteEstablisher(establisherKind, establisherIndex) match {
        case Right(futureUserAnswers) =>
          futureUserAnswers.map { userAnswers =>
            Redirect(navigator.nextPage(ConfirmDeleteEstablisherId, NormalMode, userAnswers))
          }
        case Left(result) =>
          result
      }
  }

  private def deleteEstablisher(establisherKind: EstablisherKind, establisherIndex: Index)(implicit dataRequest: DataRequest[AnyContent]) = {
    establisherKind match {
      case Company =>
        CompanyDetailsId(establisherIndex).retrieve.right.map { companyDetails =>
          dataCacheConnector.save(CompanyDetailsId(establisherIndex), companyDetails.copy(isDeleted = true))
        }
      case Indivdual =>
        EstablisherDetailsId(establisherIndex).retrieve.right.map { establisherDetails =>
          dataCacheConnector.save(EstablisherDetailsId(establisherIndex), establisherDetails.copy(isDeleted = true))
        }
      case Partnership =>
        PartnershipDetailsId(establisherIndex).retrieve.right.map { partnershipDetails =>
          dataCacheConnector.save(PartnershipDetailsId(establisherIndex), partnershipDetails.copy(isDeleted = true))
        }
      case _ =>
        Left(Future.successful(SeeOther(controllers.routes.SessionExpiredController.onPageLoad().url)))
    }
  }

  private case class DeletableEstablisher(name: String, isDeleted: Boolean)

  private def getDeletableEstablisher(index: Index, establisherKind: EstablisherKind, userAnswers: UserAnswers): Option[DeletableEstablisher] = {
    establisherKind match {
      case Indivdual => userAnswers.get(EstablisherDetailsId(index)).map(details => DeletableEstablisher(details.fullName, details.isDeleted))
      case Company => userAnswers.get(CompanyDetailsId(index)).map(details => DeletableEstablisher(details.companyName, details.isDeleted))
      case Partnership => userAnswers.get(PartnershipDetailsId(index)).map(details => DeletableEstablisher(details.name, details.isDeleted))
      case _ => None
    }
  }
}
