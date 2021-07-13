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

import identifiers.TypedIdentifier
import identifiers.register.establishers.EstablisherKindId
import identifiers.register.establishers.company.CompanyEnterUTRId
import identifiers.register.establishers.partnership.PartnershipEnterUTRId
import models.ReferenceValue
import models.register.establishers.EstablisherKind

import scala.annotation.tailrec

object UtrHelper extends Enumerable.Implicits{

  def countEstablishers(userAnswers: UserAnswers):Int = {
    @tailrec
    def count(i:Int):Int = {
      userAnswers.get(EstablisherKindId(i)) match {
        case None => i
        case Some(_) => count(i + 1)
      }
    }
    count(0)
  }

  def stripUtr(userAnswers: UserAnswers): UserAnswers = {
    val allIds = (0 until countEstablishers(userAnswers)).foldLeft[Seq[TypedIdentifier[ReferenceValue]]](Nil) {
      (ids, index) =>
        val establisherUTRId = userAnswers.get(EstablisherKindId(index)) match {
          case Some(EstablisherKind.Company) => Some(CompanyEnterUTRId(index))
          case Some(EstablisherKind.Partnership) => Some(PartnershipEnterUTRId(index))
          case _ => None
        }
        establisherUTRId.fold(ids)(ids ++ Seq(_))
    }


    filter(userAnswers, allIds)
  }

//
//  def stripUtr(userAnswers: UserAnswers): UserAnswers = {
//    (0 until countEstablishers(userAnswers)).foldLeft(userAnswers) {
//      (ua, index) =>
//        val uaUpdate =
//          filterUserAnswers(filterUserAnswers(ua, CompanyEnterUTRId(index)), PartnershipEnterUTRId(index))
//        (0 to 9).foldLeft(uaUpdate) {
//          (ua, directorOrPartnerIndex) =>
//            filterUserAnswers(filterUserAnswers(ua, DirectorEnterUTRId(index, directorOrPartnerIndex)), PartnerEnterUTRId(index, directorOrPartnerIndex))
//        }
//    }
//  }

  private def filter(userAnswers: UserAnswers, ids: Seq[TypedIdentifier[ReferenceValue]]): UserAnswers = {
    ids.foldLeft[UserAnswers](userAnswers) {
      (ua,id) =>
        ua.get(id) match {
          case None => ua
          case Some(v) =>
            val validUtr = strip(v.value)
            UserAnswers(ua.json).setOrException(id)(ReferenceValue(validUtr))
        }
    }
  }

  private def strip(utr: String): String = {
    val r = utr.replaceAll("""[a-zA-Z\s]""", "")
    val regexLongUtr = """^[\d]{13}$"""
    r match {
      case _ if r.matches(regexLongUtr) => r.substring(3)
      case _ => r
    }
  }
}
