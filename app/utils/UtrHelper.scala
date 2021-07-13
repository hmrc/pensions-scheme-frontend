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
import identifiers.register.establishers.company.director.DirectorEnterUTRId
import identifiers.register.establishers.partnership.PartnershipEnterUTRId
import identifiers.register.establishers.partnership.partner.PartnerEnterUTRId
import identifiers.register.trustees.company.{CompanyEnterUTRId => TrusteeCompanyUTRId}
import identifiers.register.trustees.partnership.{PartnershipEnterUTRId => TrusteePartnershipUTRId}
import identifiers.register.trustees.TrusteeKindId
import models.ReferenceValue
import models.register.establishers.EstablisherKind
import models.register.trustees.TrusteeKind

import scala.annotation.tailrec

object UtrHelper extends Enumerable.Implicits{

  private[utils] def countEstablishers(userAnswers: UserAnswers):Int = {
    @tailrec
    def count(i:Int):Int = {
      userAnswers.get(EstablisherKindId(i)) match {
        case None => i
        case Some(_) => count(i + 1)
      }
    }
    count(0)
  }

  private[utils] def countTrustees(userAnswers: UserAnswers):Int = {
    @tailrec
    def count(i:Int):Int = {
      userAnswers.get(TrusteeKindId(i)) match {
        case None => i
        case Some(_) => count(i + 1)
      }
    }
    count(0)
  }

  private[utils] def countDirectors(userAnswers: UserAnswers, establisherNo: Int): Int = {
    @tailrec
    def count(i: Int): Int = {
      userAnswers.get(DirectorEnterUTRId(establisherNo, i)) match {
        case None => i
        case Some(_) => count(i + 1)
      }
    }
    count(0)
  }

  private[utils] def countPartners(userAnswers: UserAnswers, establisherNo: Int): Int = {
    @tailrec
    def count(i: Int): Int = {
      userAnswers.get(PartnerEnterUTRId(establisherNo, i)) match {
        case None => i
        case Some(_) => count(i + 1)
      }
    }
    count(0)
  }

  private def getDirectorIds(userAnswers: UserAnswers, establisherNo: Int): Seq[TypedIdentifier[ReferenceValue]] ={
    (0 until countDirectors(userAnswers, establisherNo)).foldLeft[Seq[TypedIdentifier[ReferenceValue]]](Nil) {
      (ids, index) =>
          val seqDirectorUTRId = userAnswers.get(DirectorEnterUTRId(establisherNo, index)) match {
            case Some(_) => Seq(DirectorEnterUTRId(establisherNo, index))
            case _ => Nil
          }
        ids ++ seqDirectorUTRId
    }
  }

  private def getPartnerIds(userAnswers: UserAnswers, establisherNo: Int): Seq[TypedIdentifier[ReferenceValue]] = {
    (0 until countPartners(userAnswers, establisherNo)).foldLeft[Seq[TypedIdentifier[ReferenceValue]]](Nil) {
      (ids, index) =>
        val seqPartnerUTRId = userAnswers.get(PartnerEnterUTRId(establisherNo, index)) match {
          case Some(_) => Seq(PartnerEnterUTRId(establisherNo, index))
          case _ => Nil
        }
        ids ++ seqPartnerUTRId
    }
  }

    def stripUtr(userAnswers: UserAnswers): UserAnswers = {
    val allEstablisherIds = (0 until countEstablishers(userAnswers)).foldLeft[Seq[TypedIdentifier[ReferenceValue]]](Nil) {
      (ids, index) =>
        val seqEstablisherUTRId = userAnswers.get(EstablisherKindId(index)) match {
          case Some(EstablisherKind.Company) =>
            Seq(CompanyEnterUTRId(index)) ++ getDirectorIds(userAnswers, index)
          case Some(EstablisherKind.Partnership) =>
            Seq(PartnershipEnterUTRId(index)) ++ getPartnerIds(userAnswers, index)
          case _ =>
            Nil
        }
        ids ++ seqEstablisherUTRId
    }
      val allTrusteeIds = (0 until countTrustees(userAnswers)).foldLeft[Seq[TypedIdentifier[ReferenceValue]]](Nil) {
        (ids, index) =>
          val seqTrusteeUTRId = userAnswers.get(TrusteeKindId(index)) match {
            case Some(TrusteeKind.Company) =>
              Seq(TrusteeCompanyUTRId(index))
            case Some(TrusteeKind.Partnership) =>
              Seq(TrusteePartnershipUTRId(index))
            case _ =>
              Nil
          }
          ids ++ seqTrusteeUTRId
      }
    filter(userAnswers, allTrusteeIds ++ allEstablisherIds)
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
