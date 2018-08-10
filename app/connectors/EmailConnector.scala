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

import com.google.inject.{ImplementedBy, Inject, Singleton}
import config.FrontendAppConfig
import models.SendEmailRequest
import play.api.Logger
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import scala.concurrent.{ExecutionContext, Future}


sealed trait EmailStatus
case object EmailSent extends EmailStatus
case object EmailNotSent extends EmailStatus

@ImplementedBy (classOf[EmailConnectorImpl])
trait EmailConnector {
  def sendEmail(emailAddress: String, templateName: String, params: Map[String, String])
               (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[EmailStatus]
}

@Singleton
class EmailConnectorImpl @Inject()(http: HttpClient, config: FrontendAppConfig) extends EmailConnector {

  lazy val postUrl: String = s"${config.emailApiUrl}/hmrc/email"

  override def sendEmail(emailAddress: String, templateName: String, params: Map[String, String])
                        (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[EmailStatus] = {

    val sendEmailReq = SendEmailRequest(List(emailAddress), templateName, params, force = true)

    val jsonData = Json.toJson(sendEmailReq)

    http.POST(postUrl, jsonData).map { response =>
      response.status match {
        case ACCEPTED =>
          Logger.info("Email sent successfully.")
          EmailSent
        case status =>
          Logger.warn(s"Email not sent. Failure with response status $status")
          EmailNotSent
      }
    } recoverWith logExceptions
  }

  private def logExceptions: PartialFunction[Throwable, Future[EmailStatus]] = {
        case t: Throwable =>
            Logger.warn("Unable to connect to Email Service", t)
            Future.successful(EmailNotSent)
  }
}
