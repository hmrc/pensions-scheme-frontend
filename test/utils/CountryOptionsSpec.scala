/*
 * Copyright 2017 HM Revenue & Customs
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

package utils

import base.SpecBase
import models.CountryOptions

class CountryOptionsSpec extends SpecBase {

  def countryOption(jsonFile: String = "country-canonical-list-test.json"): CountryOptions =
    new CountryOptions(environment, frontendAppConfig){
    override lazy val locationCanonicalList: String = jsonFile
  }

  "Country Options" must {

    "build correctly the InputOptions with country list and country code" in {
      countryOption().options mustEqual Seq(InputOption("territory:AE-AZ", "Abu Dhabi"),
        InputOption("country:AF", "Afghanistan"))
    }

    "build the empty list if the file name is not correct" in {
      countryOption("country-canonical-list-test").options mustEqual Seq.empty
    }
  }
}
