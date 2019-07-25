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

package navigators

import connectors.UserAnswersCacheConnector
import identifiers.{Identifier, LastPageId, TypedIdentifier}
import models._
import models.requests.IdentifiedRequest
import play.api.Logger
import play.api.libs.json.Reads
import play.api.mvc.Call
import uk.gov.hmrc.http.HeaderCarrier
import utils.UserAnswers

import scala.concurrent.ExecutionContext
import scala.util.Failure

trait Navigator {

  case class NavigateFrom(id: Identifier, userAnswers: UserAnswers)
  case class NavigateTo(page: Call, save: Boolean)

  object NavigateTo {

    def save(page: Call): Option[NavigateTo] = Some(NavigateTo(page, true))

    def dontSave(page: Call): Option[NavigateTo] = Some(NavigateTo(page, false))
  }

  def nextPage(id: Identifier, mode: Mode, userAnswers: UserAnswers, srn: Option[String] = None)
                             (implicit ex: IdentifiedRequest, ec: ExecutionContext, hc: HeaderCarrier): Call = {
    nextPageOptional(id, mode, userAnswers, srn)
      .getOrElse(defaultPage(id, mode))
  }

  def nextPageOptional(id: Identifier, mode: Mode, userAnswers: UserAnswers, srn: Option[String] = None)
              (implicit ex: IdentifiedRequest, ec: ExecutionContext, hc: HeaderCarrier): Option[Call]

  private def defaultPage(id: Identifier, mode: Mode): Call = {
    Logger.warn(s"No navigation defined for id $id in mode $mode")
    controllers.routes.IndexController.onPageLoad()
  }
}

abstract class AbstractNavigator extends Navigator {

  protected def navigateOrSessionExpired[A](answers: UserAnswers,
                                            id: => TypedIdentifier[A],
                                            destination: A => Call)(implicit reads: Reads[A]): Option[NavigateTo] =
    NavigateTo.dontSave(answers.get(id).fold(controllers.routes.SessionExpiredController.onPageLoad())(destination(_)))

  protected def dataCacheConnector: UserAnswersCacheConnector

  protected def routeMap(from: NavigateFrom): Option[NavigateTo]

  protected def editRouteMap(from: NavigateFrom): Option[NavigateTo]

  protected def updateRouteMap(from: NavigateFrom, srn: Option[String] = None): Option[NavigateTo]

  protected def checkUpdateRouteMap(from: NavigateFrom, srn: Option[String] = None): Option[NavigateTo]

  override final def nextPageOptional(id: Identifier, mode: Mode, userAnswers: UserAnswers, srn: Option[String] = None)
              (implicit ex: IdentifiedRequest, ec: ExecutionContext, hc: HeaderCarrier): Option[Call] = {

    val navigateTo = {
      mode match {
        case NormalMode => routeMap(NavigateFrom(id, userAnswers))
        case CheckMode => editRouteMap(NavigateFrom(id, userAnswers))
        case UpdateMode => updateRouteMap(NavigateFrom(id, userAnswers), srn)
        case CheckUpdateMode => checkUpdateRouteMap(NavigateFrom(id, userAnswers), srn)
      }
    }

    navigateTo.map(to => saveAndContinue(to, ex.externalId))
  }

  private[this] def saveAndContinue(navigation: NavigateTo, externalID: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Call = {
    if (navigation.save) {
      dataCacheConnector.save(externalID, LastPageId, LastPage(navigation.page.method, navigation.page.url)) andThen {
        case Failure(t: Throwable) => Logger.warn("Error saving user's current page", t)
      }
    }
    navigation.page
  }
}
