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

package viewmodels

import models.register.establishers.company.director.DirectorDetails
import controllers.register.establishers.company.director.routes
import models.{Index, NormalMode}

import scala.language.implicitConversions

case class EditableItem(index: Int, name: String, deleteLink: String, editLink: String) {
  def id: String = s"item-$index"
  def deleteLinkId: String = s"$id-delete"
  def editLinkId: String = s"$id-edit"
}

object EditableItem {

  implicit def indexedCompanyDirectors(directors: (Int, Seq[DirectorDetails])): Seq[EditableItem] = {
    directors._2.zipWithIndex.map { case (director, index) =>
      EditableItem(
        index,
        director.directorName,
        routes.ConfirmDeleteDirectorController.onPageLoad(directors._1, index).url,
        routes.DirectorDetailsController.onPageLoad(NormalMode, directors._1, Index(index)).url
      )
    }
  }

}
