/*
 * Copyright 2024 HM Revenue & Customs
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
import controllers.Retrievals
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import identifiers.register.trustees.company.CompanyDetailsId
import identifiers.register.trustees.individual.TrusteeNameId
import identifiers.register.trustees.partnership.PartnershipDetailsId
import javax.inject.Inject
import models.register.trustees.TrusteeKind
import models.register.trustees.TrusteeKind.{Company, Individual, Partnership}
import models.requests.DataRequest
import models.{Index, Mode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Enumerable
import viewmodels.{AlreadyDeletedViewModel, Message}
import views.html.alreadyDeleted

import scala.concurrent.{ExecutionContext, Future}

class AlreadyDeletedController @Inject()(
                                          appConfig: FrontendAppConfig,
                                          override val messagesApi: MessagesApi,
                                          authenticate: AuthAction,
                                          getData: DataRetrievalAction,
                                          requireData: DataRequiredAction,
                                          val controllerComponents: MessagesControllerComponents,
                                          val view: alreadyDeleted
                                        )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController
  with Retrievals with I18nSupport with Enumerable.Implicits {

  def onPageLoad(mode: Mode, index: Index, trusteeKind: TrusteeKind, srn: SchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        trusteeName(index, trusteeKind, srn) match {
          case Right(trusteeName) =>
            Future.successful(Ok(view(vm(index, trusteeName, mode, srn, existingSchemeName))))
          case Left(result) => result
        }
    }

  private def vm(index: Index, trusteeName: String, mode: Mode, srn: SchemeReferenceNumber, schemeName: Option[String]) =
    AlreadyDeletedViewModel(
    title = Message("messages__alreadyDeleted__trustee_title"),
    deletedEntity = trusteeName,
    returnCall = controllers.register.trustees.routes.AddTrusteeController.onPageLoad(mode, srn),
    srn = srn,
    schemeName = schemeName
  )

  private def trusteeName(index: Index, trusteeKind: TrusteeKind, srn: SchemeReferenceNumber)
                         (implicit dataRequest: DataRequest[AnyContent]): Either[Future[Result], String] = {
    trusteeKind match {
      case Company => CompanyDetailsId(index).retrieve.map(_.companyName)
      case Individual => TrusteeNameId(index).retrieve.map(_.fullName)
      case Partnership => PartnershipDetailsId(index).retrieve.map(_.name)
    }
  }
}
