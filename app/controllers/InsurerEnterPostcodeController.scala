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

package controllers

import config.FrontendAppConfig
import connectors.AddressLookupConnector
import controllers.actions._
import controllers.address.PostcodeLookupController
import forms.address.PostCodeLookupFormProvider
import identifiers.{InsuranceCompanyNameId, InsurerEnterPostCodeId}

import javax.inject.Inject
import models.{Mode, SchemeReferenceNumber}
import models.requests.DataRequest
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.UserAnswersService
import utils.annotations.{AboutBenefitsAndInsurance, InsuranceService}
import viewmodels.address.PostcodeLookupViewModel
import views.html.address.postcodeLookup

import scala.concurrent.ExecutionContext

class InsurerEnterPostcodeController @Inject()(val appConfig: FrontendAppConfig,
                                               override val messagesApi: MessagesApi,
                                               @InsuranceService val userAnswersService: UserAnswersService,
                                               val addressLookupConnector: AddressLookupConnector,
                                               @AboutBenefitsAndInsurance val navigator: Navigator,
                                               authenticate: AuthAction,
                                               getData: DataRetrievalAction,
                                               allowAccess: AllowAccessActionProvider,
                                               requireData: DataRequiredAction,
                                               formProvider: PostCodeLookupFormProvider,
                                               val controllerComponents: MessagesControllerComponents,
                                               val view: postcodeLookup
                                              )(implicit val ec: ExecutionContext) extends PostcodeLookupController {

  val postCall: (Mode, SchemeReferenceNumber) => Call = routes.InsurerEnterPostcodeController.onSubmit
  val manualCall: (Mode, SchemeReferenceNumber) => Call = routes.InsurerConfirmAddressController.onPageLoad

  val form: Form[String] = formProvider()

  def formWithError(messageKey: String): Form[String] = {
    form.withError("value", s"messages__error__postcode_$messageKey")
  }

  def onPageLoad(mode: Mode, srn: SchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData() andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        InsuranceCompanyNameId.retrieve.map { name =>
          get(viewModel(mode, srn, name))
        }
    }

  def viewModel(mode: Mode, srn: SchemeReferenceNumber, name: String)(implicit request: DataRequest[AnyContent])
  : PostcodeLookupViewModel =
    PostcodeLookupViewModel(
      postCall(mode, srn),
      manualCall(mode, srn),
      Messages("messages__insurer_enter_postcode__h1", Messages("messages__theInsuranceCompany")),
      Messages("messages__insurer_enter_postcode__h1", name),
      None,
      srn = srn
    )

  def onSubmit(mode: Mode, srn: SchemeReferenceNumber): Action[AnyContent] = (authenticate() andThen getData()
    andThen requireData).async {
    implicit request =>
      InsuranceCompanyNameId.retrieve.map { name =>
        post(InsurerEnterPostCodeId, viewModel(mode, srn, name), mode)
      }
  }

}
