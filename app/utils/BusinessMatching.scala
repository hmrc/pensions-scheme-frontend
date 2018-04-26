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

case class BusinessMatching(name1: String, name2: String) {

  val shortenPercentage = 50

  val specialWords = List("AND", "CCC", "CIC", "COMPANIES", "COMPANY", "CORPORATION", "INCORPORATED",
    "CORP", "CO.", "CO", "INC.", "INC", "UNLIMITED", "LIMITED", "LLP", "LP", "ULTD", "UNLTD", "LTD",
    "PARTNERSHIP", "PLC", "THE")

  def convertToUpper: BusinessMatching = BusinessMatching(name1.toUpperCase, name2.toUpperCase)

  def removeSpaces: BusinessMatching = {
    BusinessMatching(
      name1.filterNot((x: Char) => x.isWhitespace),
      name2.filterNot((x: Char) => x.isWhitespace)
    )
  }

  def removeSpecialWords: BusinessMatching = {
    BusinessMatching(f(name1), f(name2))
  }

  private def f(word:String) = specialWords.foldLeft[String](word)((z,i) =>(z.replace(i, "")).replaceAll("\\s{2,}", " ").trim())

  def removeSpecialCharacters: BusinessMatching = {
    BusinessMatching(
      name1.replaceAll("[&']", "").replaceAll("\\s{2,}", " ").trim(),
      name2.replaceAll("[&']", "").replaceAll("\\s{2,}", " ").trim()
    )
  }

  def removeNonAlphaNumeric: BusinessMatching = {
    BusinessMatching(
      name1.replaceAll("[^a-zA-Z\\d]", "").replaceAll("\\s{2,}", " ").trim(),
      name2.replaceAll("[^a-zA-Z\\d]", "").replaceAll("\\s{2,}", " ").trim()
    )
  }

  def lengthCheck: BusinessMatching = {
    if(name1.length.equals(0) | name2.length.equals(0)) {
        this.removeSpaces.convertToUpper
    } else {
        this
    }
  }


  def shortenLongest: BusinessMatching = {
    name1.length < name2.length match {
      case true => BusinessMatching(name1,shorten(name2, name1, shortenPercentage))
      case false => BusinessMatching(shorten(name1, name2, shortenPercentage),name2)
    }
  }

  private def shorten(long: String, short: String, x: Int): String = {
    val x_percent_of_long = long.length*x*0.01
    val len_of_short = short.length
    if (x_percent_of_long < len_of_short)
      long.take(len_of_short)
    else
      long.take(x_percent_of_long.toInt)
  }

  def isEqual: Boolean = name1.equals(name2)

  def isMatch: Boolean = {
    this.convertToUpper.removeSpaces.removeSpecialWords
      .lengthCheck.removeSpecialCharacters
      .removeNonAlphaNumeric.lengthCheck.shortenLongest
      .isEqual
  }

}
