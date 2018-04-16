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

import models.CheckMode

sealed trait EntityDetails {
  def route(id1: Int, id2: Option[Int]): (String, String)
}

  case class TrusteeIndividualName(name: String) extends EntityDetails {
    override def route(id: Int, id2: Option[Int]): (String, String) =
      name ->
        controllers.register.trustees.individual.routes.TrusteeDetailsController.onPageLoad(CheckMode, id).url
  }

  case class TrusteeCompanyName(name: String) extends EntityDetails {
    override def route(id: Int, id2: Option[Int]): (String, String) =
      name ->
        controllers.register.trustees.company.routes.CompanyDetailsController.onPageLoad(CheckMode, id).url
  }

  case class EstablisherIndividualName(name: String) extends EntityDetails {
    override def route(id: Int, id2: Option[Int]): (String, String) =
      name ->
        controllers.register.establishers.individual.routes.EstablisherDetailsController.onPageLoad(CheckMode, id).url
  }

  case class EstablisherCompanyName(name: String) extends EntityDetails {
    override def route(id: Int, id2: Option[Int]): (String, String) =
      name ->
        controllers.register.establishers.company.routes.CompanyDetailsController.onPageLoad(CheckMode, id).url
  }
