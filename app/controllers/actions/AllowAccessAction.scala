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

package controllers.actions

import com.google.inject.ImplementedBy
import identifiers.IsPsaSuspendedId
import models.requests.OptionalDataRequest
import play.api.mvc.Results._
import play.api.mvc.{ActionFilter, Result}

import scala.concurrent.Future

class AllowAccessAction(srn: Option[String]) extends ActionFilter[OptionalDataRequest]{

  override protected def filter[A](request: OptionalDataRequest[A]): Future[Option[Result]] = {

    request.userAnswers match {
      case None => Future.successful(None)
      case Some(userAnswers) => userAnswers.get(IsPsaSuspendedId) match {
        case Some(true) => Future.successful(Some(Redirect(controllers.register.routes.CannotMakeChangesController.onPageLoad(srn))))
        case _ => Future.successful(None) //todo- url manipulation by changing srn should be handled here (currently handles users in NormalMode as well)
      }
    }
  }

}

class AllowAccessActionProviderImpl extends AllowAccessActionProvider{
  def apply(srn: Option[String]): AllowAccessAction = {
    new AllowAccessAction(srn)
  }
}

@ImplementedBy(classOf[AllowAccessActionProviderImpl])
trait AllowAccessActionProvider{
  def apply(srn: Option[String]) : AllowAccessAction
}
