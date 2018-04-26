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

package utils

import org.scalatest.{MustMatchers, OptionValues, WordSpecLike}

class BusinessMatchingSpec extends WordSpecLike with MustMatchers with OptionValues {

  val expectedWithoutSpaces = BusinessMatching("CHRISWILLIAMS", "CHRISWILLIAMSSONS")
  val expectedWithSpaces = BusinessMatching("CHRIS WILLIAMS", "CHRIS WILLIAMS SONS")

  "isMatch" must {
    "return true" when {
      "given names of ltd and limited" in {
      }
    }
  }


  "convertToUpper" must {
    "convert the names to upper case" in {
      val businessMatching = BusinessMatching("chris williAMS", "CHRIS williams soNs")
      businessMatching.convertToUpper mustEqual expectedWithSpaces
    }
  }

  "removeSpaces" must {
    "remove spaces from the names" in {
      val businessMatching = BusinessMatching("CHRIS WILLIAMS", "CHRIS WILLIAMS SONS")
      businessMatching.removeSpaces mustEqual expectedWithoutSpaces
    }
  }

  "removeSpecialWords" must {
    "remove special words from the names" in {
      val businessMatching = BusinessMatching("CHRIS WILLIAMS LTD CO", "CHRIS WILLIAMS AND SONS CO.")
      businessMatching.removeSpecialWords mustEqual expectedWithSpaces
    }
  }

  "removeSpecialCharacters" must {
    "remove special characters from the names" in {
      val businessMatching = BusinessMatching("CHRIS WILLIAM'S", "CHRIS WILLIAMS & SONS")
      businessMatching.removeSpecialCharacters mustEqual expectedWithSpaces
    }
  }

  "removeNonAlphaNumeric" must {
    "remove non-alphanumeric characters from the names" in {
      val businessMatching = BusinessMatching("CHRI$S WILLIAMS!", "CHRI%S WILLI@AMS SONS.")
      businessMatching.removeNonAlphaNumeric mustEqual expectedWithoutSpaces
    }
  }

  "lengthCheck" must {
    "revert the effect of removing special words" when {
      "one of the names has length zero after transformations" in {
        val businessMatchingExp = BusinessMatching("PARTNERSHIPCOCOLTD.", "CHRISWILLIAMSSONS")
        val businessMatching = BusinessMatching("Partnership CO & co Ltd.", "Chris William's & Sons")
        businessMatching.removeSpaces.convertToUpper.removeSpecialCharacters.lengthCheck mustEqual businessMatchingExp
      }
    }
  }

  "shortenLongest" must {
    "shorten the longer of the two values" when {

      "x% of longer name is less than length of shorter name" in {
        val businessMatchingExp = BusinessMatching("CHRI$S WILLIAMS!", "CHRI%S WILLI@AMS")
        val businessMatching = BusinessMatching("CHRI$S WILLIAMS!", "CHRI%S WILLI@AMS SONS.")
        businessMatching.shortenLongest mustEqual businessMatchingExp
      }

      "x% of longer name is more than length of shorter name" in {
        val businessMatchingExp = BusinessMatching("CHRI$S WILLIAMS! CARPE", "CHRI%S WILLI@AMS")
        val businessMatching = BusinessMatching("CHRI$S WILLIAMS! CARPETS AND HEAVY MACHINERY", "CHRI%S WILLI@AMS")
        businessMatching.shortenLongest mustEqual businessMatchingExp
      }
    }
  }

  "isMatch" must {
    "return true" when {
      "names after transformation match" in {
        val businessMatching = BusinessMatching("CHRI$S AND WILLIAMS!", "CHRI%S WILLI@AMS UNLIMITED.")
        businessMatching.isMatch mustBe true
      }
    }

    "return false" when {
      "names after transformation do not match" in {
        val businessMatching = BusinessMatching("CHRI$S WILLIAMS! CARPETS AND HEAVY MACHINERY", "CHRI%S WILLI@AMS")
        businessMatching.isMatch mustBe false
      }
    }
  }



}


