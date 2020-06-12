/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers

import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.establishers.individual.EstablisherNameId
import identifiers.register.establishers.partnership.PartnershipDetailsId
import identifiers.{SchemeNameId, TypedIdentifier}
import models.person.PersonName
import models.requests.{DataRequest, OptionalDataRequest}
import models.{CompanyDetails, PartnershipDetails}
import play.api.libs.json.Reads
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContent, Result, WrappedRequest}

import scala.concurrent.Future
import scala.language.implicitConversions

trait Retrievals {

  private[controllers] def retrieveCompanyName(index: Int)
                                              (f: String => Future[Result])
                                              (implicit request: DataRequest[AnyContent]): Future[Result] = {
    retrieve[CompanyDetails](CompanyDetailsId(index)) { companyDetails =>
      f(companyDetails.companyName)
    }
  }

  private[controllers] def retrieve[A](id: TypedIdentifier[A])
                                      (f: A => Future[Result])
                                      (implicit request: DataRequest[AnyContent], r: Reads[A]): Future[Result] = {
    request.userAnswers.get(id).map(f).getOrElse {
      Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
    }

  }

  private[controllers] def retrievePartnershipName(index: Int)
                                                  (f: String => Future[Result])
                                                  (implicit request: DataRequest[AnyContent]): Future[Result] = {
    retrieve[PartnershipDetails](PartnershipDetailsId(index)) { partnershipDetails =>
      f(partnershipDetails.name)
    }
  }

  private[controllers] def retrieveEstablisherName(index: Int)
                                                  (f: String => Future[Result])
                                                  (implicit request: DataRequest[AnyContent]): Future[Result] = {
    retrieve[PersonName](EstablisherNameId(index)) { establisherName =>
      f(establisherName.fullName)
    }
  }

  private[controllers] def existingSchemeName[A <: WrappedRequest[AnyContent]](implicit request: A): Option[String] =
    request match {
      case optionalDataRequest: OptionalDataRequest[_] => optionalDataRequest.userAnswers.flatMap(_.get(SchemeNameId))
      case dataRequest: DataRequest[_] =>
        dataRequest.userAnswers.get(SchemeNameId)
      case _ => None
    }

  trait Retrieval[A] {
    self =>

    def retrieve(implicit request: DataRequest[AnyContent]): Either[Future[Result], A]

    def and[B](query: Retrieval[B]): Retrieval[A ~ B] =
      new Retrieval[A ~ B] {
        override def retrieve(implicit request: DataRequest[AnyContent]): Either[Future[Result], A ~ B] = {
          for {
            a <- self.retrieve.right
            b <- query.retrieve.right
          } yield new ~(a, b)
        }
      }
  }

  // scalastyle:off class.name
  case class ~[A, B](a: A, b: B)

  object Retrieval {

    def static[A](a: A): Retrieval[A] =
      Retrieval {
        implicit request =>
          Right(a)
      }

    def apply[A](f: DataRequest[AnyContent] => Either[Future[Result], A]): Retrieval[A] =
      new Retrieval[A] {
        override def retrieve(implicit request: DataRequest[AnyContent]): Either[Future[Result], A] =
          f(request)
      }
  }

  implicit def fromId[A](id: TypedIdentifier[A])(implicit rds: Reads[A]): Retrieval[A] =
    Retrieval {
      implicit request =>
        request.userAnswers.get(id) match {
          case Some(value) => Right(value)
          case None => Left(Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad())))
        }
    }

  implicit def merge(f: Either[Future[Result], Future[Result]]): Future[Result] =
    f.merge
}
