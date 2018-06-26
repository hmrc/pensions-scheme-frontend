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

import controllers.register._
import models.register.establishers.EstablisherKind
import models.register.establishers.company.director.DirectorDetails
import models.register.trustees.TrusteeKind
import models.{Index, NormalMode, TrusteeEntityDetails, TrusteeList}

import scala.language.implicitConversions

//noinspection MutatorLikeMethodIsParameterless
case class EditableItem(index: Int, name: String, deleteLink: String, editLink: String) {
  def deleteLinkId: String = s"$id-delete"

  def editLinkId: String = s"$id-edit"

  def id: String = s"person-$index"
}

object EditableItem {

  implicit def indexedCompanyDirectors(directors: (Int, Seq[DirectorDetails])): Seq[EditableItem] = {
    directors._2.zipWithIndex.map {
      case (director, index) =>
        EditableItem(
          index,
          director.directorName,
          establishers.company.director.routes.ConfirmDeleteDirectorController.onPageLoad(directors._1, index).url,
          establishers.company.director.routes.DirectorDetailsController.onPageLoad(NormalMode, directors._1, Index(index)).url
        )
    }
  }

  implicit def fromEntityDetailsKind(trusteEntityDetails: Seq[TrusteeEntityDetails]) = {
    trusteEntityDetails.map{ trusteeEntity =>
      EditableItem(0, trusteeEntity.name, trusteeEntity.deleteUrl.getOrElse(""), trusteeEntity.changeUrl.getOrElse(""))
    }
  }

  implicit def fromEntityDetails(entity: Seq[(String, String, EntityKind)]): Seq[EditableItem] = entity.zipWithIndex.map {
    case ((entityName, changeUrl, entityKind), index) =>
      entityKind match {
        case EntityKind.Establisher =>
          val establisherKind = changeUrl match {
            case url if changeUrl == establishers.company.routes.CompanyDetailsController.onPageLoad(NormalMode, index).url
            => EstablisherKind.Company
            case url if changeUrl == establishers.individual.routes.EstablisherDetailsController.onPageLoad(NormalMode, index).url
            => EstablisherKind.Indivdual
            case _ => throw new IllegalArgumentException(s"Cannot determine establisher kind: $entityName")
          }

          EditableItem(
            index,
            entityName,
            establishers.routes.ConfirmDeleteEstablisherController.onPageLoad(index, establisherKind).url,
            changeUrl
          )

        case EntityKind.Trustee =>
          val trusteeKind = changeUrl match {
            case url if changeUrl == trustees.company.routes.CompanyDetailsController.onPageLoad(NormalMode, index).url
            => TrusteeKind.Company
            case url if changeUrl == trustees.individual.routes.TrusteeDetailsController.onPageLoad(NormalMode, index).url
            => TrusteeKind.Individual
            case _ => throw new IllegalArgumentException(s"Cannot determine trustee kind: $entityName")
          }

          EditableItem(
            index,
            entityName,
            trustees.routes.ConfirmDeleteTrusteeController.onPageLoad(Index(index), trusteeKind).url,
            changeUrl
          )
        case _ => throw new IllegalArgumentException(s"Cannot determine entity kind: $entityKind")
      }
  }
}
