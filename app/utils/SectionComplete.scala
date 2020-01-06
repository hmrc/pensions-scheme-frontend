/*
 * Copyright 2020 HM Revenue & Customs
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

import com.google.inject.{ImplementedBy, Inject}
import config.FrontendAppConfig
import connectors.{PensionSchemeVarianceLockConnector, UserAnswersCacheConnector}
import identifiers.TypedIdentifier
import play.api.libs.json.JsResultException
import services.UserAnswersService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent._

@ImplementedBy(classOf[SectionCompleteImpl])
trait SectionComplete {

  def setCompleteFlag(cacheId: String, id: TypedIdentifier[Boolean], userAnswers: UserAnswers, value: Boolean)
                     (implicit ec: ExecutionContext, hc: HeaderCarrier): Future[UserAnswers]
}

class SectionCompleteImpl @Inject()(dataCacheConnector: UserAnswersCacheConnector,
                                    userAnswersService: UserAnswersService,
                                    lockConnector: PensionSchemeVarianceLockConnector,
                                    appConfig: FrontendAppConfig) extends SectionComplete {

  override def setCompleteFlag(cacheId: String, id: TypedIdentifier[Boolean], userAnswers: UserAnswers, value: Boolean)
                              (implicit ec: ExecutionContext, hc: HeaderCarrier): Future[UserAnswers] = {

    userAnswers.set(id)(value).fold(
      invalid => Future.failed(JsResultException(invalid)),
      valid => Future.successful(valid)
    )

    dataCacheConnector.save(cacheId, id, value) map UserAnswers
  }

}