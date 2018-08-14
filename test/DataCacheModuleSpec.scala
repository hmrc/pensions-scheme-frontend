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

import connectors.{DataCacheConnector, MicroserviceCacheConnector, MongoCacheConnector}
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.inject._
import play.api.{Configuration, Environment}

class DataCacheModuleSpec extends WordSpec with MustMatchers with OptionValues {

  ".bindings" must {

    "bind the `MongoCacheConnector` when configuration is set to `public`" in {

      val config = Configuration("journey-cache" -> "public")
      val bindings = new DataCacheModule().bindings(Environment.simple(), config)
      val binding = bind[DataCacheConnector].to[MongoCacheConnector]

      bindings.head.target.value mustEqual binding.target.value
    }

    "bind the `MicroserviceCacheConnector` when the configuration is set to `protected`" in {

      val config = Configuration("journey-cache" -> "protected")
      val bindings = new DataCacheModule().bindings(Environment.simple(), config)
      val binding = bind[DataCacheConnector].to[MicroserviceCacheConnector]

      bindings.head.target.value mustEqual binding.target.value
    }

    "bind the `MicroserviceCacheConnector` when no configuration value is given" in {

      val config = Configuration()
      val bindings = new DataCacheModule().bindings(Environment.simple(), config)
      val binding = bind[DataCacheConnector].to[MicroserviceCacheConnector]

      bindings.head.target.value mustEqual binding.target.value
    }

    "bind the `MicroserviceCacheConnector` when the configuration is set to anything else" in {

      val config = Configuration("journey-cache" -> "foobar")
      val bindings = new DataCacheModule().bindings(Environment.simple(), config)
      val binding = bind[DataCacheConnector].to[MicroserviceCacheConnector]

      bindings.head.target.value mustEqual binding.target.value
    }
  }
}
