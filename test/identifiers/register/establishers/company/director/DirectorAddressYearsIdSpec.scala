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

package identifiers.register.establishers.company.director

import models.AddressYears
import models.address.Address
import org.scalatest.{Matchers, OptionValues, WordSpec}
import utils.UserAnswers

class DirectorAddressYearsIdSpec extends WordSpec with Matchers with OptionValues {

  import DirectorAddressYearsIdSpec._

  "DirectorAddressYearsId cleanup" when {

    "AddressYears is set to 'over a year'" must {
      "remove the data for DirectorPreviousAddressPostcodeLookupId" in {
        val newAnswers = userAnswers.set(addressYearsId)(AddressYears.OverAYear).asOpt.value
        newAnswers.get(previousAddressPostcodeLookupId) shouldNot be(defined)
      }

      "remove the data for DirectorPreviousAddressId" in {
        val newAnswers = userAnswers.set(addressYearsId)(AddressYears.OverAYear).asOpt.value
        newAnswers.get(previousAddressId) shouldNot be(defined)
      }
    }

    "AddressYears is set to 'under a year'" must {
      "not remove the data for DirectorPreviousAddressPostcodeLookupId" in {
        val newAnswers = userAnswers.set(addressYearsId)(AddressYears.UnderAYear).asOpt.value
        newAnswers.get(previousAddressPostcodeLookupId) should be(defined)
      }

      "not remove the data for DirectorPreviousAddressId" in {
        val newAnswers = userAnswers.set(addressYearsId)(AddressYears.UnderAYear).asOpt.value
        newAnswers.get(previousAddressId) should be(defined)
      }
    }

    "AddressYears is removed" must {
      "not remove the data for DirectorPreviousAddressPostcodeLookupId" in {
        val newAnswers = userAnswers.remove(addressYearsId).asOpt.value
        newAnswers.get(previousAddressPostcodeLookupId) should be(defined)
      }

      "not remove the data for DirectorPreviousAddressId" in {
        val newAnswers = userAnswers.remove(addressYearsId).asOpt.value
        newAnswers.get(previousAddressId) should be(defined)
      }
    }

  }

}

object DirectorAddressYearsIdSpec extends OptionValues {

  private val establisherIndex = 0
  private val directorIndex = 0

  private val previousAddressPostcodeLookupId = DirectorPreviousAddressPostcodeLookupId(establisherIndex, directorIndex)
  private val previousAddressId = DirectorPreviousAddressId(establisherIndex, directorIndex)
  private val addressYearsId = DirectorAddressYearsId(establisherIndex, directorIndex)

  private val address = Address(
    "test-address-line1",
    "test-address-line2",
    None,
    None,
    None,
    "test-country"
  )

  private val userAnswers = UserAnswers()
    .set(addressYearsId)(AddressYears.UnderAYear)
    .flatMap(_.set(previousAddressPostcodeLookupId)(Seq(address)))
    .flatMap(_.set(previousAddressId)(address))
    .asOpt.value

}
