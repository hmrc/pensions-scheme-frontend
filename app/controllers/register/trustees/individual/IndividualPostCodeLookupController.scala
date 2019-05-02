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

package controllers.register.trustees.individual

import config.FrontendAppConfig
import connectors.AddressLookupConnector
import controllers.actions._
import controllers.address.PostcodeLookupController
import forms.address.PostCodeLookupFormProvider
import identifiers.register.trustees.individual.{IndividualPostCodeLookupId, TrusteeDetailsId}
import javax.inject.Inject
import models.{Index, Mode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import utils.Navigator
import utils.annotations.TrusteesIndividual
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel

class IndividualPostCodeLookupController @Inject()(
                                                    val appConfig: FrontendAppConfig,
                                                    override val messagesApi: MessagesApi,
                                                    val userAnswersService: UserAnswersService,
                                                    @TrusteesIndividual override val navigator: Navigator,
                                                    authenticate: AuthAction,
                                                    getData: DataRetrievalAction,
                                                    allowAccess: AllowAccessActionProvider,
                                                    requireData: DataRequiredAction,
                                                    formProvider: PostCodeLookupFormProvider,
                                                    val addressLookupConnector: AddressLookupConnector
                                                  ) extends PostcodeLookupController with I18nSupport {
  override protected val form: Form[String] = formProvider()

  private def postCodeViewmodel(index: Int, mode: Mode, srn: Option[String]): Retrieval[PostcodeLookupViewModel] =
    Retrieval {
      implicit request =>
        TrusteeDetailsId(index).retrieve.right.map {
          details =>
            PostcodeLookupViewModel(
              routes.IndividualPostCodeLookupController.onSubmit(mode, index, srn),
              routes.TrusteeAddressController.onPageLoad(mode, index, srn),
              title = Message("messages__individualPostCodeLookup__title"),
              heading = Message("messages__individualPostCodeLookup__heading"),
              subHeading = Some(details.fullName),
              hint = Some(Message("messages__common_individual_postCode_lookup__lede")),
              enterPostcode = Message("messages__trustee_individualPostCodeLookup__enter_postcode"),
              srn= srn
            )
        }
    }

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
    implicit request =>
      postCodeViewmodel(index, mode, srn).retrieve.right map get
  }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      postCodeViewmodel(index, mode, srn).retrieve.right.map { vm =>
        post(IndividualPostCodeLookupId(index), vm, mode)
      }
  }
}
