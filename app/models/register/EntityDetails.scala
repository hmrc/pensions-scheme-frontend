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

package models.register

import models.register.trustees.TrusteeKind
import models.NormalMode
import models.register.establishers.EstablisherKind
import viewmodels.EditableItem

sealed trait EntityDetails {
  def route(id1: Int, id2: Option[Int]): EditableItem
}

case class TrusteeIndividualNameCopy(name: String, isDeleted: Boolean) extends EntityDetails {
  override def route(id: Int, id2: Option[Int]): EditableItem =
    EditableItem(id, name, isDeleted, controllers.register.trustees.individual.routes.TrusteeDetailsController.onPageLoad(NormalMode, id).url,
      controllers.register.trustees.routes.ConfirmDeleteTrusteeController.onPageLoad(id, TrusteeKind.Individual).url)
}

case class TrusteeCompanyNameCopy(name: String, isDeleted: Boolean) extends EntityDetails {
  override def route(id: Int, id2: Option[Int]): EditableItem =
    EditableItem(id, name, isDeleted, controllers.register.trustees.company.routes.CompanyDetailsController.onPageLoad(NormalMode, id).url,
      controllers.register.trustees.routes.ConfirmDeleteTrusteeController.onPageLoad(id, TrusteeKind.Company).url)
}

case class EstablisherIndividualNameCopy(name: String, isDeleted: Boolean) extends EntityDetails {
  override def route(id: Int, id2: Option[Int]): EditableItem =
    EditableItem(id, name, isDeleted, controllers.register.establishers.individual.routes.EstablisherDetailsController.onPageLoad(NormalMode, id).url,
      controllers.register.establishers.routes.ConfirmDeleteEstablisherController.onPageLoad(id, EstablisherKind.Indivdual).url)
}

case class EstablisherCompanyNameCopy(name: String, isDeleted: Boolean) extends EntityDetails {
  override def route(id: Int, id2: Option[Int]): EditableItem =
    EditableItem(id, name, isDeleted, controllers.register.establishers.company.routes.CompanyDetailsController.onPageLoad(NormalMode, id).url,
      controllers.register.establishers.routes.ConfirmDeleteEstablisherController.onPageLoad(id, EstablisherKind.Company).url)
}
