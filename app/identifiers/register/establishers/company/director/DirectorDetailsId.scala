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

package identifiers.register.establishers.company.director

import identifiers._
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.company.OtherDirectorsId
import models.register.establishers.company.director.DirectorDetails
import play.api.libs.json.{JsPath, JsResult}
import utils.UserAnswers

case class DirectorDetailsId(establisherIndex:Int,directorIndex:Int) extends TypedIdentifier[DirectorDetails] {
  override def path: JsPath = EstablishersId(establisherIndex).path \ "director" \ directorIndex \ DirectorDetailsId.toString

  override def cleanup(value: Option[DirectorDetails], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    userAnswers.allDirectorsAfterDelete(this.establisherIndex).lengthCompare(10) match {
      case lengthCompare if lengthCompare <= 0 => userAnswers.remove(OtherDirectorsId(this.establisherIndex))
      case _ => super.cleanup(value, userAnswers)
    }
  }
}

object DirectorDetailsId{
  def collectionPath(establisherIndex: Int): JsPath =
    EstablishersId(establisherIndex).path \ "director" \\ DirectorDetailsId.toString

  override def toString: String = "directorDetails"
}
