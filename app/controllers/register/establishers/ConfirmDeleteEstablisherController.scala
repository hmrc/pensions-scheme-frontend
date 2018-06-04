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

package controllers.register.establishers

import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.Retrievals
import controllers.actions._
import identifiers.register.SchemeDetailsId
import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.establishers.individual.EstablisherDetailsId
import identifiers.register.establishers.{ConfirmDeleteEstablisherId, EstablishersId}
import javax.inject.Inject
import models.register.establishers.EstablisherKind
import models.register.establishers.EstablisherKind._
import models.requests.DataRequest
import models.{Index, NormalMode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{Navigator, UserAnswers}
import views.html.register.establishers.confirmDeleteEstablisher

import scala.concurrent.Future

class ConfirmDeleteEstablisherController @Inject()(
                                                    appConfig: FrontendAppConfig,
                                                    override val messagesApi: MessagesApi,
                                                    dataCacheConnector: DataCacheConnector,
                                                    navigator: Navigator,
                                                    authenticate: AuthAction,
                                                    getData: DataRetrievalAction,
                                                    requireData: DataRequiredAction
                                                  ) extends FrontendController with I18nSupport with Retrievals {

  def onPageLoad(establisherIndex: Index, establisherKind: EstablisherKind): Action[AnyContent] =
    (authenticate andThen getData andThen requireData).async {
      implicit request =>
        SchemeDetailsId.retrieve.right.map {
          case schemeDetails =>
            establisherName(establisherIndex, establisherKind) match {
              case Right(establisherName) =>
                Future.successful(
                  Ok(
                    confirmDeleteEstablisher(
                      appConfig,
                      schemeDetails.schemeName,
                      establisherName,
                      routes.ConfirmDeleteEstablisherController.onSubmit(establisherIndex),
                      routes.AddEstablisherController.onPageLoad(NormalMode)
                    )
                  )
                )
              case Left(result) => result
            }
        }
    }


  def onSubmit(establisherIndex: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      dataCacheConnector.remove(request.externalId, EstablishersId(establisherIndex)).map {
        json =>
          Redirect(navigator.nextPage(ConfirmDeleteEstablisherId, NormalMode)(UserAnswers(json)))
      }
  }

  private def establisherName(establisherIndex: Index, establisherKind: EstablisherKind)
                             (implicit dataRequest: DataRequest[AnyContent]): Either[Future[Result], String] = {
    establisherKind match {
      case Company => CompanyDetailsId(establisherIndex).retrieve.right.map(_.companyName)
      case Indivdual => EstablisherDetailsId(establisherIndex).retrieve.right.map(_.fullName)
      case Partnership => Left(Future.successful(SeeOther(controllers.routes.SessionExpiredController.onPageLoad().url)))
      case _ => Left(Future.successful(SeeOther(controllers.routes.SessionExpiredController.onPageLoad().url)))
    }
  }

  private def postCall(index: Index, establisherKind: EstablisherKind) = {
    routes.ConfirmDeleteEstablisherController.onSubmit(index)
  }

}
