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

package forms.mappings

import models.register.SortCode
import play.api.data.format.Formatter
import play.api.data.{FormError, Forms, Mapping}

trait BankDetailsMapping extends Mappings {

  protected def sortCodeMapping(requiredKey: String = "error.required", invalidKey: String, maxErrorKey: String): Mapping[SortCode] = {

    val formatter: Formatter[SortCode] = new Formatter[SortCode] {

      val baseFormatter: Formatter[String] = stringFormatter(requiredKey)

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], SortCode] = {

        baseFormatter.bind(key, data)
          .right.map(_.trim.replaceAll("[ -]", ""))
          .right.flatMap {
          case str if !str.matches(regexSortCode) =>
            Left(Seq(FormError(key, invalidKey)))
          case str if str.trim.replaceAll("[- ]", "").length > 6 =>
            Left(Seq(FormError(key, maxErrorKey)))
          case str =>
            str.sliding(2, 2).toList match {
              case a :: b :: c :: Nil =>
                Right(SortCode(a, b, c))
              case _ =>
                Left(Seq(FormError(key, invalidKey)))
            }
        }
      }

      override def unbind(key: String, value: SortCode): Map[String, String] =
        baseFormatter.unbind(key, s"${value.first} ${value.second} ${value.third}")
    }

    Forms.of(formatter)
  }

}
