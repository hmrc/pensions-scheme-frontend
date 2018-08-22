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

package controllers

import audit.{AuditService, EmailEvent}
import controllers.model.{EmailEvents, Opened}
import javax.inject.Inject
import play.api.libs.json.JsValue
import play.api.mvc.{Action, BodyParsers, Result}
import uk.gov.hmrc.crypto.{ApplicationCrypto, Crypted}
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

class EmailResponseController @Inject()(
                                       auditService: AuditService,
                                       crypto: ApplicationCrypto
                                       ) extends FrontendController {

  def post(id: String): Action[JsValue] = Action(BodyParsers.parse.tolerantJson) {
    implicit request =>

      validatePsaId(id) match {
        case Left(result) => result
        case Right(psaId) => request.body.validate[EmailEvents].fold(
          _ => BadRequest,
          valid => {
            valid.events
              .map {
                _.event
              }
              .filterNot {
                case Opened => true
                case _ => false
              }
              .foreach { event =>
                auditService.sendEvent(EmailEvent(psaId, event))
              }
            Ok
          }
        )
      }

  }

  private def validatePsaId(id: String): Either[Result, PsaId] =
    try {
      Right(PsaId {
        crypto.QueryParameterCrypto.decrypt(Crypted(id)).value
      })
    } catch {
      case _: IllegalArgumentException => Left(Forbidden("Malformed PSAID"))
    }

}
