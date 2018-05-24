/*
 * Copyright 2018 HM Revenue & Customs
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

package controllers.register.trustees.company

import config.FrontendAppConfig
import connectors.{AddressLookupConnector, DataCacheConnector}
import controllers.actions._
import controllers.address.PostcodeLookupController
import forms.address.PostCodeLookupFormProvider
import identifiers.register.trustees.company.{CompanyDetailsId, CompanyPostcodeLookupId}
import javax.inject.Inject
import models.{Index, Mode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.TrusteesCompany
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel

class CompanyPostCodeLookupController @Inject()(
                                                 val appConfig: FrontendAppConfig,
                                                 override val messagesApi: MessagesApi,
                                                 val cacheConnector: DataCacheConnector,
                                                 @TrusteesCompany override val navigator: Navigator,
                                                 authenticate: AuthAction,
                                                 getData: DataRetrievalAction,
                                                 requireData: DataRequiredAction,
                                                 formProvider: PostCodeLookupFormProvider,
                                                 val addressLookupConnector: AddressLookupConnector
                                               ) extends PostcodeLookupController with I18nSupport {

  private[controllers] val manualAddressCall = routes.CompanyAddressController.onPageLoad _
  private[controllers] val postCall = routes.CompanyPostCodeLookupController.onSubmit _

  private[controllers] val title: Message = "messages__companyAddress__title"
  private[controllers] val heading: Message = "messages__companyAddress__title"

  override protected val form: Form[String] = formProvider()

  private def viewmodel(index: Int, mode: Mode): Retrieval[PostcodeLookupViewModel] =
    Retrieval {
      implicit request =>
        CompanyDetailsId(index).retrieve.right.map {
          details =>
            PostcodeLookupViewModel(
              postCall(mode, index),
              manualAddressCall(mode, index),
              title = Message(title),
              heading = Message(heading),
              subHeading = Some(details.companyName)
            )
        }
    }

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] =
    (authenticate andThen getData andThen requireData).async {
      implicit request =>
        viewmodel(index, mode).retrieve.right map get
    }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] =
    (authenticate andThen getData andThen requireData).async {
      implicit request =>
        viewmodel(index, mode).retrieve.right.map { vm =>
          post(CompanyPostcodeLookupId(index), vm, mode)
        }
    }
}
