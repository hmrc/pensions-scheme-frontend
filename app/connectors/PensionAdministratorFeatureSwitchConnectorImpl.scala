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

package connectors

import com.google.inject.Inject
import config.FrontendAppConfig
import play.api.http.Status._
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import uk.gov.hmrc.http.client.HttpClientV2

import scala.concurrent.{ExecutionContext, Future}

class PensionAdministratorFeatureSwitchConnectorImpl @Inject()(httpClientV2: HttpClientV2, appConfig: FrontendAppConfig)
  extends FeatureSwitchConnector {

  override def toggleOn(name: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] = {

    val url = url"${appConfig.pensionsAdministratorUrl}/pension-administrator/test-only/toggle-on/${name}"

    httpClientV2.get(url)
      .execute[HttpResponse].map { response =>
      response.status match {
        case NO_CONTENT => true
        case _ => false
      }
    }
  }

  override def toggleOff(name: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] = {

    val url = url"${appConfig.pensionsAdministratorUrl}/pension-administrator/test-only/toggle-off/${name}"

    httpClientV2.get(url)
      .execute[HttpResponse].map { response =>
        response.status match {
          case NO_CONTENT => true
          case _ => false
        }
      }
  }

  override def reset(name: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] = {
    val url = url"${appConfig.pensionsAdministratorUrl}/pension-administrator/test-only/reset/${name}"

    httpClientV2.get(url)
      .execute[HttpResponse].map { response =>
        response.status match {
          case NO_CONTENT => true
          case _ => false
        }
      }
  }

  override def get(name: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Boolean]] = {
    val url = url"${appConfig.pensionsAdministratorUrl}/pension-administrator/test-only/get/${name}"

    httpClientV2.get(url)
      .execute[HttpResponse].map { response =>
      response.status match {
        case OK => Some(response.json.as[Boolean])
        case _ => None
      }
    }.recoverWith {
      case _ =>
        Future.successful(None)
    }
  }
}


