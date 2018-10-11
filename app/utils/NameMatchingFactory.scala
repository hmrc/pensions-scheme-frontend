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

package utils

import config.FrontendAppConfig
import connectors.{PensionAdministratorConnector, UserAnswersCacheConnector}
import javax.inject.Inject
import models.PSAName
import models.requests.OptionalDataRequest
import play.api.libs.json.{JsValue, Reads}
import play.api.mvc.AnyContent
import uk.gov.hmrc.crypto.{ApplicationCrypto, PlainText}
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}
import utils.annotations.PSANameCache

import scala.concurrent.{ExecutionContext, Future}

class NameMatchingFactory @Inject()(
                                     @PSANameCache val pSANameCacheConnector: UserAnswersCacheConnector,
                                     pensionAdministratorConnector: PensionAdministratorConnector,
                                     crypto: ApplicationCrypto,
                                     config: FrontendAppConfig
                                   ) {

  private def retrievePSAName(implicit request: OptionalDataRequest[AnyContent], ec: ExecutionContext, hc: HeaderCarrier): Future[String] = {
    if (config.isWorkPackageOneEnabled) {
      pensionAdministratorConnector.getPSAName
    } else {

      val encryptedCacheId = crypto.QueryParameterCrypto.encrypt(PlainText(request.psaId.id)).value

      pSANameCacheConnector.fetch(encryptedCacheId) flatMap { psaOpt =>

        val psaName = for {
          psaJs <- psaOpt
          psaName <- psaJs.asOpt[PSAName]
        } yield {
          psaName.psaName
        }

        psaName match {
          case Some(name) => Future.successful(name)
          case _ => Future.failed(new NotFoundException("Cannot retrieve PSA name from collection"))
        }

      }
    }
  }


  def nameMatching(schemeName: String)
                  (implicit request: OptionalDataRequest[AnyContent],
                   ec: ExecutionContext,
                   hc: HeaderCarrier, r: Reads[PSAName]): Future[NameMatching] = retrievePSAName map { NameMatching(schemeName, _) }

}
