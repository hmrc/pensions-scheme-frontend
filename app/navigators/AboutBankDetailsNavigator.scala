/*
 * Copyright 2024 HM Revenue & Customs
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
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.routes._
import identifiers.{BankAccountDetailsId, UKBankAccountId}
import models.NormalMode
import utils.UserAnswers

class AboutBankDetailsNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector,
                                          appConfig: FrontendAppConfig) extends AbstractNavigator {

  override protected def routeMap(from: NavigateFrom, srn: SchemeReferenceNumber): Option[NavigateTo] = navRouteMap(from)

  override protected def editrouteMap(from: NavigateFrom, srn: SchemeReferenceNumber): Option[NavigateTo] = navRouteMap(from)

  private def navrouteMap(from: NavigateFrom, srn: SchemeReferenceNumber): Option[NavigateTo] = {
    from.id match {
      case UKBankAccountId => ukBankAccountRoutes(from.userAnswers)
      case BankAccountDetailsId => checkYourAnswers
      case _ => None
    }
  }

  private def ukBankAccountRoutes(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(UKBankAccountId) match {
      case Some(true) => NavigateTo.dontSave(BankAccountDetailsController.onPageLoad(NormalMode))
      case Some(false) => checkYourAnswers
      case None => NavigateTo.dontSave(SessionExpiredController.onPageLoad)
    }
  }

  private def checkYourAnswers: Option[NavigateTo] =
    NavigateTo.dontSave(controllers.routes.CheckYourAnswersBankDetailsController.onPageLoad())

  protected def updateRouteMap(from: NavigateFrom, srn: SchemeReferenceNumber): Option[NavigateTo] = None

  protected def checkUpdateRouteMap(from: NavigateFrom, srn: SchemeReferenceNumber): Option[NavigateTo] = None
}
