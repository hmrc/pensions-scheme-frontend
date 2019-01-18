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
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import identifiers._
import models.{CheckMode, NormalMode}
import utils.Navigator

class WorkingKnowledgeNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector, appConfig: FrontendAppConfig) extends Navigator {

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = {
    from.id match {
      case AdviserNameId =>
        NavigateTo.save(controllers.routes.AdviserEmailAddressController.onPageLoad(NormalMode))
      case AdviserEmailId =>
        NavigateTo.save(controllers.routes.AdviserPhoneController.onPageLoad(NormalMode))
      case AdviserPhoneId =>
        NavigateTo.save(controllers.routes.AdviserPostCodeLookupController.onPageLoad(NormalMode))
      case AdviserAddressPostCodeLookupId =>
        NavigateTo.save(controllers.routes.AdviserAddressListController.onPageLoad(NormalMode))
      case AdviserAddressListId =>
        NavigateTo.save(controllers.routes.AdviserAddressController.onPageLoad(NormalMode))
      case AdviserAddressId =>
        NavigateTo.save(controllers.routes.AdviserCheckYourAnswersController.onPageLoad())
      case AdviserCheckYourAnswersId =>
        NavigateTo.save(controllers.routes.SchemeTaskListController.onPageLoad())
      case _ => None
    }
  }

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] = {
    from.id match {
      case AdviserNameId =>
        NavigateTo.save(controllers.routes.AdviserCheckYourAnswersController.onPageLoad())
      case AdviserEmailId =>
        NavigateTo.save(controllers.routes.AdviserCheckYourAnswersController.onPageLoad())
      case AdviserPhoneId =>
        NavigateTo.save(controllers.routes.AdviserCheckYourAnswersController.onPageLoad())
      case AdviserAddressPostCodeLookupId =>
        NavigateTo.save(controllers.routes.AdviserAddressListController.onPageLoad(CheckMode))
      case AdviserAddressListId =>
        NavigateTo.save(controllers.routes.AdviserAddressController.onPageLoad(CheckMode))
      case AdviserAddressId =>
        NavigateTo.save(controllers.routes.AdviserCheckYourAnswersController.onPageLoad())
      case _ => None
    }
  }
}
