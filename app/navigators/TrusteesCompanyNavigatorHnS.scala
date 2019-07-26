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

import com.google.inject.Inject
import connectors.UserAnswersCacheConnector
import identifiers.register.trustees.company.HasCompanyNumberId
import models.{Mode, NormalMode}
import play.api.mvc.Call
import utils.UserAnswers
import controllers.register.trustees.company.routes._

class TrusteesCompanyNavigatorHnS @Inject()(val dataCacheConnector: UserAnswersCacheConnector) extends AbstractNavigator {

  protected def routes(from: NavigateFrom, mode: Mode, srn: Option[String]): Option[NavigateTo] = {
    NavigateTo.dontSave(from.id match {
      case HasCompanyNumberId(id) =>
        hasCompanyNumberId(id, from.userAnswers)
      case _ =>
        controllers.routes.SessionExpiredController.onPageLoad()
    })
  }

  private def hasCompanyNumberId(id: Int, answers: UserAnswers): Call = {
    answers.get(HasCompanyNumberId(id)) match {
      case Some(true) => CompanyRegistrationNumberVariationsController.onPageLoad(NormalMode, None, id)
      case Some(false) => NoCompanyNumberController.onPageLoad(NormalMode, id, None)
      case _ => controllers.routes.SessionExpiredController.onPageLoad()
    }
  }

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = routes(from, NormalMode, None)

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] = ???

  override protected def updateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = ???

  override protected def checkUpdateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = ???
}
