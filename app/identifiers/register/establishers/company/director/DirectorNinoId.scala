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

package identifiers.register.establishers.company.director

import identifiers._
import identifiers.register.establishers.EstablishersId
import models.Nino
import play.api.libs.json.JsPath
import utils.checkyouranswers.CheckYourAnswers
import utils.checkyouranswers.CheckYourAnswers.NinoCYA

case class DirectorNinoId(establisherIndex: Int, directorIndex: Int) extends TypedIdentifier[Nino] {
  override def path: JsPath = EstablishersId(establisherIndex).path \ "director" \ directorIndex \ DirectorNinoId.toString
}

object DirectorNinoId {
  override lazy val toString: String = "directorNino"

  implicit val cya: CheckYourAnswers[DirectorNinoId] =
    NinoCYA(
      label = "messages__director_nino_question_cya_label",
      changeHasNino = "messages__visuallyhidden__director__nino_yes_no",
      changeNino = "messages__visuallyhidden__director__nino",
      changeNoNino = "messages__visuallyhidden__director__nino_no"
    )()
}
