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

package connectors

import com.google.inject.ImplementedBy
import config.FrontendAppConfig
import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import play.api.http.Status._

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[PensionAdministratorConnectorImpl])
trait PensionAdministratorConnector {

  def getPSAEmail(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[String]

}

@Singleton
class PensionAdministratorConnectorImpl @Inject()(http: HttpClient, config: FrontendAppConfig) extends PensionAdministratorConnector {

  def getPSAEmail(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[String] = {

    val url = config.pensionsAdministratorUrl + config.getPSAEmail

    http.GET(url) flatMap { response =>
      response.status match {
        case OK => Future.successful(response.body)
        case NOT_FOUND => Future.failed(new NotFoundException("Cannot retrieve email from Minimal PSA Details"))
      }
    }

  }

}
