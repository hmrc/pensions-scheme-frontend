/*
 * Copyright 2022 HM Revenue & Customs
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
import identifiers._
import models.{CheckMode, NormalMode}

class WorkingKnowledgeNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector,
                                          appConfig: FrontendAppConfig) extends AbstractNavigator {

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = {
    from.id match {
      case AdviserNameId =>
        NavigateTo.dontSave(controllers.routes.AdviserEmailAddressController.onPageLoad(NormalMode))
      case AdviserEmailId =>
        NavigateTo.dontSave(controllers.routes.AdviserPhoneController.onPageLoad(NormalMode))
      case AdviserPhoneId =>
        NavigateTo.dontSave(controllers.routes.AdviserPostCodeLookupController.onPageLoad(NormalMode))
      case AdviserAddressPostCodeLookupId =>
        NavigateTo.dontSave(controllers.routes.AdviserAddressListController.onPageLoad(NormalMode))
      case AdviserAddressListId =>
        NavigateTo.dontSave(controllers.routes.AdviserCheckYourAnswersController.onPageLoad())
      case AdviserAddressId =>
        NavigateTo.dontSave(controllers.routes.AdviserCheckYourAnswersController.onPageLoad())
      case AdviserCheckYourAnswersId =>
        NavigateTo.dontSave(controllers.routes.PsaSchemeTaskListController.onPageLoad(NormalMode, None))
      case _ => None
    }
  }

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] = {
    from.id match {
      case AdviserNameId =>
        NavigateTo.dontSave(controllers.routes.AdviserCheckYourAnswersController.onPageLoad())
      case AdviserEmailId =>
        NavigateTo.dontSave(controllers.routes.AdviserCheckYourAnswersController.onPageLoad())
      case AdviserPhoneId =>
        NavigateTo.dontSave(controllers.routes.AdviserCheckYourAnswersController.onPageLoad())
      case AdviserAddressPostCodeLookupId =>
        NavigateTo.dontSave(controllers.routes.AdviserAddressListController.onPageLoad(CheckMode))
      case AdviserAddressListId =>
        NavigateTo.dontSave(controllers.routes.AdviserCheckYourAnswersController.onPageLoad())
      case AdviserAddressId =>
        NavigateTo.dontSave(controllers.routes.AdviserCheckYourAnswersController.onPageLoad())
      case _ => None
    }
  }

  protected def updateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = None

  protected def checkUpdateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = None
}
