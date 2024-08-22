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

package controllers.register.trustees.company

import config.FrontendAppConfig
import connectors.AddressLookupConnector
import controllers.actions._
import controllers.address.PostcodeLookupController
import forms.address.PostCodeLookupFormProvider
import identifiers.register.trustees.company.{CompanyDetailsId, CompanyPreviousAddressPostcodeLookupId}
import javax.inject.Inject
import models.{Index, Mode}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel
import views.html.address.postcodeLookup

import scala.concurrent.ExecutionContext

class CompanyPreviousAddressPostcodeLookupController @Inject()(
                                                                val appConfig: FrontendAppConfig,
                                                                override val messagesApi: MessagesApi,
                                                                val userAnswersService: UserAnswersService,
                                                                val navigator: Navigator,
                                                                authenticate: AuthAction,
                                                                getData: DataRetrievalAction,
                                                                allowAccess: AllowAccessActionProvider,
                                                                requireData: DataRequiredAction,
                                                                formProvider: PostCodeLookupFormProvider,
                                                                val addressLookupConnector: AddressLookupConnector,
                                                                val controllerComponents: MessagesControllerComponents,
                                                                val view: postcodeLookup
                                                              )(implicit val ec: ExecutionContext) extends
  PostcodeLookupController with I18nSupport {

  override protected val form: Form[String] = formProvider()
  private[controllers] val manualAddressCall = routes.CompanyPreviousAddressController.onPageLoad _
  private[controllers] val postCall = routes.CompanyPreviousAddressPostcodeLookupController.onSubmit _
  private[controllers] val title: Message = "messages__companyPreviousAddressPostcodeLookup__title"
  private[controllers] val heading: Message = "messages__companyPreviousAddressPostcodeLookup__heading"

  def onPageLoad(mode: Mode, index: Index, srn: SchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        viewmodel(index, mode, srn).retrieve map get
    }

  def onSubmit(mode: Mode, index: Index, srn: SchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        viewmodel(index, mode, srn).retrieve.map { vm =>
          post(CompanyPreviousAddressPostcodeLookupId(index), vm, mode)
        }
    }

  private def viewmodel(index: Int, mode: Mode, srn: SchemeReferenceNumber): Retrieval[PostcodeLookupViewModel] =
    Retrieval {
      implicit request =>
        CompanyDetailsId(index).retrieve.map {
          details =>
            PostcodeLookupViewModel(
              postCall(mode, index, srn),
              manualAddressCall(mode, index, srn),
              title = Message(title),
              heading = Message(heading, details.companyName),
              subHeading = Some(details.companyName),
              srn = srn
            )
        }
    }
}
