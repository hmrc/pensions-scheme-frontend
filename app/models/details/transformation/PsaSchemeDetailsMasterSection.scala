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

package models.details.transformation

import javax.inject.Inject
import models.details._
import viewmodels.MasterSection

import scala.language.implicitConversions

case class PsaSchemeDetailsMasterSection @Inject()(schemeDetails: SchemeDetailsSection[SchemeDetails],
                                                   establisherDetails: EstablisherInfoSection,
                                                   trusteeDetails: TrusteeInfoSection) {

  def transformMasterSection(data: PsaSchemeDetails): Seq[MasterSection] = {

    val schemeDetailsSection = Seq(MasterSection(None, Seq(schemeDetails.transformSuperSection(data.schemeDetails))))

    val establisherDetailsSection = data.establisherDetails.map {
      establisher =>
        establisherDetails.transformMasterSection(establisher)
    }.toSeq

    val trusteeDetailsSection = data.trusteeDetails.map {
      trustee =>
        trusteeDetails.transformMasterSection(trustee)
    }.toSeq

    schemeDetailsSection ++ establisherDetailsSection ++ trusteeDetailsSection

  }
}
