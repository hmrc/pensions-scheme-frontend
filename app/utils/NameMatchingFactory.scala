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
import models.requests.{DataRequest, OptionalDataRequest}
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

  def nameMatching(schemeName: String)
                  (implicit request: DataRequest[AnyContent],
                   ec: ExecutionContext,
                   hc: HeaderCarrier, r: Reads[PSAName]): Future[NameMatching] =
    pensionAdministratorConnector.getPSAName map { NameMatching(schemeName, _) }

}
