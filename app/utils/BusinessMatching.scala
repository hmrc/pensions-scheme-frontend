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

class BusinessMatching(name1: String, name2: String) {

  private def convertToUpper(matching: (String, String) = (name1, name2)): (String, String) = ???

  private def removeSpaces(matching: (String, String) = (name1, name2)): (String, String) = ???

  private def removeSpecialWords(matching: (String, String) = (name1, name2)): (String, String) = ???

  private def removeSpecialCharacters(matching: (String, String) = (name1, name2)): (String, String) = ???

  private def removeNonAlphaNumeric(matching: (String, String) = (name1, name2)): (String, String) = ???

  private def lengthCheck(matching: (String, String) = (name1, name2)): (String, String) = {
    if(matching._1.length.equals(0) | matching._2.length.equals(0)) {
      ???
    } else {
      ???
    }
  }

  private def shortenLongest(matching: (String, String)): (String, String) = ???

  def isMatch: Boolean = ???

}
