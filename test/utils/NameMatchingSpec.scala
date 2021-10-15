/*
 * Copyright 2021 HM Revenue & Customs
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

import org.scalatest.{MustMatchers, OptionValues, WordSpecLike}

class NameMatchingSpec extends WordSpecLike with ArgumentMatchers with OptionValues {

  val expectedWithoutSpaces = NameMatching("CHRISWILLIAMS", "CHRISWILLIAMSSONS")
  val expectedWithSpaces = NameMatching("CHRIS WILLIAMS", "CHRIS WILLIAMS SONS")

  "convertToUpper" must {
    "convert the names to upper case" in {
      val nameMatching = NameMatching("chris williAMS", "CHRIS williams soNs")
      nameMatching.convertToUpper mustEqual expectedWithSpaces
    }
  }

  "removeSpaces" must {
    "remove spaces from the names" in {
      val nameMatching = NameMatching("CHRIS WILLIAMS", "CHRIS WILLIAMS SONS")
      nameMatching.removeSpaces mustEqual expectedWithoutSpaces
    }
  }

  "removeSpecialWords" must {
    "remove special words from the names" in {
      val nameMatching = NameMatching("CHRIS WILLIAMS LTD CO", "CHRIS WILLIAMS AND SONS CO.")
      nameMatching.removeSpecialWords mustEqual expectedWithSpaces
    }
  }

  "removeSpecialCharacters" must {
    "remove special characters from the names" in {
      val nameMatching = NameMatching("CHRIS WILLIAM'S", "CHRIS WILLIAMS & SONS")
      nameMatching.removeSpecialCharacters mustEqual expectedWithSpaces
    }
  }

  "removeNonAlphaNumeric" must {
    "remove non-alphanumeric characters from the names" in {
      val nameMatching = NameMatching("CHRI$S WILLIAMS!", "CHRI%S WILLI@AMS SONS.")
      nameMatching.removeNonAlphaNumeric mustEqual expectedWithoutSpaces
    }
  }

  "lengthCheck" must {
    "revert the effect of removing special words" when {
      "one of the names has length zero after transformations" in {
        val nameMatchingExp = NameMatching("PARTNERSHIPCOCOLTD.", "CHRISWILLIAMSSONS")
        val nameMatching = NameMatching("Partnership CO & co Ltd.", "Chris William's & Sons")
        nameMatching.removeSpaces.convertToUpper.removeSpecialCharacters.lengthCheck mustEqual nameMatchingExp
      }
    }
  }

  "isMatch" must {
    "return true" when {
      "names after transformation match for example 1" in {
        val nameMatching = NameMatching("CHRI$S AND WILLIAMS!", "CHRI%S WILLI@AMS UNLIMITED.")
        nameMatching.isMatch mustBe true
      }
      "names after transformation match for example 2" in {
        val nameMatching = NameMatching("The Bill Bloggs Company Ltd", "THE BiLl BlO  gGs CO Limited")
        nameMatching.isMatch mustBe true
      }

    }

    "return false" when {
      "names after transformation do not match" in {
        val nameMatching = NameMatching("CHRI$S WILLIAMS! CARPETS AND HEAVY MACHINERY", "CHRI%S WILLI@AMS")
        nameMatching.isMatch mustBe false
      }

      "names after transformation do not match when one of them is longer than the other" in {
        val nameMatching = NameMatching("AIROO AUTOMOTIVE UK LTD", "AIROO Automotive UK Group Life Scheme")

        nameMatching.isMatch mustBe false
      }

      "names after transformation do not match when one of them is longer than the other containing SASS" in {
        val nameMatching = NameMatching("Halkin Carpets UK Ltd", "Halkin Carpets UK Ltd SASS")

        nameMatching.isMatch mustBe false
      }

      "names after transformation do not match for  The Ground Waste Recycling Limited SSAS" in {
        val nameMatching = NameMatching("The Ground Waste Recycling Limited SSAS", "Ground Waste Recycling Ltd")

        nameMatching.isMatch mustBe false
      }
    }
  }
}
