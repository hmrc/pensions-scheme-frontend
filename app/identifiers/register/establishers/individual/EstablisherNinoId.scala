/*
 * Copyright 2019 HM Revenue & Customs
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

package identifiers.register.establishers.individual

import identifiers._
import identifiers.register.establishers.EstablishersId
import models.Nino
import play.api.libs.json.JsPath
import utils.checkyouranswers.{CheckYourAnswers, NinoCYA}

case class EstablisherNinoId(index: Int) extends TypedIdentifier[Nino] {
  override def path: JsPath = EstablishersId(index).path \ EstablisherNinoId.toString
}

object EstablisherNinoId {
  override lazy val toString: String = "establisherNino"

  implicit val cya: CheckYourAnswers[EstablisherNinoId] =
    NinoCYA(
      label = "messages__establisher_individual_nino_question_cya_label",
      changeHasNino = "messages__visuallyhidden__establisher__nino_yes_no",
      changeNino = "messages__visuallyhidden__establisher__nino",
      changeNoNino = "messages__visuallyhidden__establisher__nino_no"
    )()
}
