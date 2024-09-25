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

package controllers.register.establishers.company

import config.FrontendAppConfig
import connectors.AddressLookupConnector
import controllers.actions._
import controllers.address.PostcodeLookupController
import forms.address.PostCodeLookupFormProvider
import identifiers.register.establishers.company.{CompanyDetailsId, CompanyPreviousAddressPostcodeLookupId}
import javax.inject.Inject
import models.{Index, Mode}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import utils.annotations.EstablishersCompany
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel
import views.html.address.postcodeLookup

import scala.concurrent.ExecutionContext
import models.SchemeReferenceNumber

class CompanyPreviousAddressPostcodeLookupController @Inject()(
                                                                override val appConfig: FrontendAppConfig,
                                                                override val messagesApi: MessagesApi,
                                                                val userAnswersService: UserAnswersService,
                                                                override val
                                                                addressLookupConnector: AddressLookupConnector,
                                                                @EstablishersCompany override val navigator: Navigator,
                                                                authenticate: AuthAction,
                                                                getData: DataRetrievalAction,
                                                                allowAccess: AllowAccessActionProvider,
                                                                requireData: DataRequiredAction,
                                                                formProvider: PostCodeLookupFormProvider,
                                                                val view: postcodeLookup,
                                                                val controllerComponents: MessagesControllerComponents
                                                              )(implicit val ec: ExecutionContext) extends
  PostcodeLookupController {

  protected val form: Form[String] = formProvider()
  private val title: Message = "messages__establisherPreviousPostCode__title"
  private val heading: Message = "messages__establisherPreviousPostCode__h1"

  def onPageLoad(mode: Mode, srn: Option[SchemeReferenceNumber], index: Index): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        viewmodel(index, srn, mode).retrieve map get
    }

  private def viewmodel(index: Int, srn: Option[SchemeReferenceNumber], mode: Mode): Retrieval[PostcodeLookupViewModel] =
    Retrieval {
      implicit request =>
        CompanyDetailsId(index).retrieve.map {
          details =>
            PostcodeLookupViewModel(
              routes.CompanyPreviousAddressPostcodeLookupController.onSubmit(mode, srn, index),
              routes.CompanyPreviousAddressController.onPageLoad(mode, srn, index),
              title = Message(title),
              heading = Message(heading, details.companyName),
              srn = srn
            )
        }
    }

  def onSubmit(mode: Mode, srn: Option[SchemeReferenceNumber], index: Index): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        viewmodel(index, srn, mode).retrieve.map {
          vm =>
            post(CompanyPreviousAddressPostcodeLookupId(index), vm, mode)
        }
    }
}
