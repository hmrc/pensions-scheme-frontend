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

import identifiers.register.establishers.individual.EstablisherDetailsId
import models.requests.DataRequest
import play.api.mvc.AnyContent

import scala.language.implicitConversions

//noinspection MutatorLikeMethodIsParameterless
case class EditableItem(index:Int,  name: String, isDeleted:Boolean , editLink: String, deleteLink: String) {
  def deleteLinkId: String = s"$id-delete"

  def editLinkId: String = s"$id-edit"

  def id: String = s"person-$index"

  def isComplete(implicit request: DataRequest[AnyContent]): Option[Boolean] = EstablisherDetailsId.isComplete(index)
}
