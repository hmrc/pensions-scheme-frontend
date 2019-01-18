/*
 * Copyright 2019 HM Revenue & Customs
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

import identifiers.SchemeNameId
import models.requests.{DataRequest, OptionalDataRequest}
import play.api.mvc.{AnyContent, WrappedRequest}

trait IDataFromRequest {

  protected def existingSchemeName[A <:WrappedRequest[AnyContent]](implicit request:A):Option[String] =
    request match {
      case optionalDataRequest: OptionalDataRequest[_] => optionalDataRequest.userAnswers.flatMap(_.get(SchemeNameId))
      case dataRequest: DataRequest[_] => dataRequest.userAnswers.get(SchemeNameId)
      case _ => None
    }
}
