/*
 * Copyright 2023 HM Revenue & Customs
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

case class NameMatching(name1: String, name2: String) {

  private val specialWords = List("AND", "CCC", "CIC", "COMPANIES", "COMPANY", "CORPORATION", "INCORPORATED",
    "CORP", "CO.", "CO", "INC.", "INC", "UNLIMITED", "LIMITED", "LLP", "LP", "ULTD", "UNLTD", "LTD",
    "PARTNERSHIP", "PLC", "THE")

  def isMatch: Boolean = {
    this.convertToUpper
      .removeSpaces
      .removeSpecialWords
      .lengthCheck
      .removeSpecialCharacters
      .removeNonAlphaNumeric
      .lengthCheck
      .isEqual
  }

  def removeSpecialWords: NameMatching = {
    NameMatching(f(name1), f(name2))
  }

  private def f(word: String) = specialWords.foldLeft[String](word)((z, i) => (z.replace(i, "")).replaceAll("\\s{2," +
    "}", " ").trim())

  def removeSpecialCharacters: NameMatching = {
    NameMatching(
      name1.replaceAll("[&']", "").replaceAll("\\s{2,}", " ").trim(),
      name2.replaceAll("[&']", "").replaceAll("\\s{2,}", " ").trim()
    )
  }

  def removeNonAlphaNumeric: NameMatching = {
    NameMatching(
      name1.replaceAll("[^a-zA-Z\\d]", "").replaceAll("\\s{2,}", " ").trim(),
      name2.replaceAll("[^a-zA-Z\\d]", "").replaceAll("\\s{2,}", " ").trim()
    )
  }

  def lengthCheck: NameMatching = {
    if (name1.length.equals(0) | name2.length.equals(0)) {
      this.removeSpaces.convertToUpper
    } else {
      this
    }
  }

  def convertToUpper: NameMatching = NameMatching(name1.toUpperCase, name2.toUpperCase)

  def removeSpaces: NameMatching = {
    NameMatching(
      name1.filterNot((x: Char) => x.isWhitespace),
      name2.filterNot((x: Char) => x.isWhitespace)
    )
  }

  def isEqual: Boolean = name1.equals(name2)
}
