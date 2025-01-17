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

package controllers.actions

import config.FrontendAppConfig
import connectors.PensionsSchemeConnector
import models.requests.OptionalDataRequest
import models.{EmptyOptionalSchemeReferenceNumber, OptionalSchemeReferenceNumber}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.MessagesApi
import play.api.mvc.{RequestHeader, Result}
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.frontend.http.FrontendErrorHandler

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class FakeAllowAccessAction(srn: OptionalSchemeReferenceNumber,
                            pensionsSchemeConnector: PensionsSchemeConnector,
                            errorHandler: FrontendErrorHandler,
                            allowPsa: Boolean = true,
                            allowPsp: Boolean = false) extends
  AllowAccessAction(srn, pensionsSchemeConnector, FakeAllowAccessAction.getMockConfig, errorHandler, allowPsa, allowPsp) {
  override def filter[A](request: OptionalDataRequest[A]): Future[Option[Result]] = Future.successful(None)
}

object FakeAllowAccessAction extends MockitoSugar {
  def getMockConfig: FrontendAppConfig = mock[FrontendAppConfig]
}

case class FakeAllowAccessProvider(srn: OptionalSchemeReferenceNumber = EmptyOptionalSchemeReferenceNumber,
                                   pensionsSchemeConnector: Option[PensionsSchemeConnector] = None
                                  ) extends AllowAccessActionProvider with MockitoSugar {

  private val errorHandler = new FrontendErrorHandler {
    implicit protected val ec: ExecutionContext = ExecutionContext.global

    override def standardErrorTemplate(pageTitle: String, heading: String, message: String)
                                      (implicit request: RequestHeader): Future[Html] = {
      Future.successful(Html(""))
    }

    override def messagesApi: MessagesApi = ???
  }

  override def apply(srn: OptionalSchemeReferenceNumber, allowPsa: Boolean = true, allowPsp: Boolean = false ): AllowAccessAction = {
    new FakeAllowAccessAction(
      srn,
      pensionsSchemeConnector match {
        case None =>
          val psc = mock[PensionsSchemeConnector]
          when(psc.checkForAssociation(any(), any(), any())(any(), any(), any()))
            .thenReturn(Future.successful(Right(true)))
          psc
        case Some(psc) => psc
      },
      errorHandler
    )
  }
}

