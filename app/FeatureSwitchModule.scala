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

import config.{FeatureSwitchManagementService, FeatureSwitchManagementServiceProductionImpl, FeatureSwitchManagementServiceTestImpl}
import navigators._
import play.api.inject.{Binding, Module}
import play.api.{Configuration, Environment}
import utils.annotations.{TrusteesCompany, TrusteesIndividual}

class FeatureSwitchModule extends Module {

  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {

    val featureSwitchBinding = Seq(
      if (configuration.underlying.getBoolean("enable-dynamic-switches")) {
        bind[FeatureSwitchManagementService].to[FeatureSwitchManagementServiceTestImpl]
      } else {
        bind[FeatureSwitchManagementService].to[FeatureSwitchManagementServiceProductionImpl]
      }
    )

    val hubSpokeEnabled = configuration.getBoolean("features.is-establisher-company-hns").getOrElse(false)

    val trusteesNavigatorBinding =
      if (hubSpokeEnabled) {
        Seq(bind(classOf[Navigator]).qualifiedWith[TrusteesIndividual].to(classOf[TrusteesIndividualNavigator]),
        bind(classOf[Navigator]).qualifiedWith[TrusteesCompany].to(classOf[TrusteesCompanyNavigator]))
      } else {
        Seq(bind(classOf[Navigator]).qualifiedWith[TrusteesIndividual].to(classOf[TrusteesIndividualNavigatorOld]),
        bind(classOf[Navigator]).qualifiedWith[TrusteesCompany].to(classOf[TrusteesCompanyNavigatorOld]))
      }

    featureSwitchBinding ++ trusteesNavigatorBinding
  }
}
