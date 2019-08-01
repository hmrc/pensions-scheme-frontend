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

import com.typesafe.config.ConfigException
import config.{FeatureSwitchManagementService, FeatureSwitchManagementServiceProductionImpl, FeatureSwitchManagementServiceTestImpl}
import navigators.{Navigator, TrusteesCompanyNavigator, TrusteesCompanyNavigatorHnS}
import play.api.inject.{Binding, Module}
import play.api.{Configuration, Environment}
import utils.annotations.TrusteesCompany

import scala.util.{Failure, Success, Try}

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

    val trusteesCompanyNavigatorBinding = Seq(
      if (hubSpokeEnabled) {
        bind(classOf[Navigator]).qualifiedWith[TrusteesCompany].to(classOf[TrusteesCompanyNavigatorHnS])
      } else {
        bind(classOf[Navigator]).qualifiedWith[TrusteesCompany].to(classOf[TrusteesCompanyNavigator])
      }
    )

    featureSwitchBinding ++ trusteesCompanyNavigatorBinding
  }
}
