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

package controllers.register.establishers.company

import config.FrontendAppConfig
import connectors.AddressLookupConnector
import controllers.actions._
import controllers.address.PostcodeLookupController
import forms.address.PostCodeLookupFormProvider
import identifiers.register.establishers.company.{CompanyDetailsId, CompanyPreviousAddressPostcodeLookupId}
import javax.inject.Inject
import models.{Index, Mode}
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import utils.Navigator
import utils.annotations.EstablishersCompany
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel

class CompanyPreviousAddressPostcodeLookupController @Inject()(
                                                                override val appConfig: FrontendAppConfig,
                                                                override val messagesApi: MessagesApi,
                                                                val userAnswersService: UserAnswersService,
                                                                override val addressLookupConnector: AddressLookupConnector,
                                                                @EstablishersCompany override val navigator: Navigator,
                                                                authenticate: AuthAction,
                                                                getData: DataRetrievalAction,
                                                                requireData: DataRequiredAction,
                                                                formProvider: PostCodeLookupFormProvider
                                                              ) extends PostcodeLookupController {

  private val title: Message = "messages__companyPreviousAddressPostcodeLookup__title"
  private val heading: Message = "messages__companyPreviousAddressPostcodeLookup__title"

  protected val form: Form[String] = formProvider()

  private def viewmodel(index: Int, srn: Option[String], mode: Mode): Retrieval[PostcodeLookupViewModel] =
    Retrieval {
      implicit request =>
        CompanyDetailsId(index).retrieve.right.map {
          details =>
            PostcodeLookupViewModel(
              routes.CompanyPreviousAddressPostcodeLookupController.onSubmit(mode, srn, index),
              routes.CompanyPreviousAddressController.onPageLoad(mode, srn, index),
              title = Message(title),
              heading = Message(heading),
              subHeading = Some(details.companyName),
              srn = srn
            )
        }
    }

  def onPageLoad(mode: Mode, srn: Option[String], index: Index): Action[AnyContent] =
    (authenticate andThen getData andThen requireData).async {
      implicit request =>
        viewmodel(index, srn, mode).retrieve.right map get
    }

  def onSubmit(mode: Mode, srn: Option[String], index: Index): Action[AnyContent] =
    (authenticate andThen getData andThen requireData).async {
      implicit request =>
        viewmodel(index, srn, mode).retrieve.right.map {
          vm =>
            post(CompanyPreviousAddressPostcodeLookupId(index), vm, mode)
        }
    }
}
