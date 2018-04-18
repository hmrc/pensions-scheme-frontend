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

package controllers.register.trustees.individual

import javax.inject.Inject

import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import connectors.{AddressLookupConnector, DataCacheConnector}
import controllers.actions._
import config.FrontendAppConfig
import controllers.address.PostcodeLookupController
import forms.address.PostCodeLookupFormProvider
import identifiers.register.trustees.individual.{IndividualPostCodeLookupId, TrusteeDetailsId}
import models.{Index, Mode}
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.TrusteesIndividual
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel

class IndividualPostCodeLookupController @Inject()(
                                                    val appConfig: FrontendAppConfig,
                                                    override val messagesApi: MessagesApi,
                                                    val cacheConnector: DataCacheConnector,
                                                    @TrusteesIndividual override val navigator: Navigator,
                                                    authenticate: AuthAction,
                                                    getData: DataRetrievalAction,
                                                    requireData: DataRequiredAction,
                                                    formProvider: PostCodeLookupFormProvider,
                                                    val addressLookupConnector: AddressLookupConnector
                                                  ) extends PostcodeLookupController with I18nSupport {
  override protected val form: Form[String] = formProvider()

  private def postCodeViewmodel(index: Int, mode: Mode): Retrieval[PostcodeLookupViewModel] =
    Retrieval {
      implicit request =>
        TrusteeDetailsId(index).retrieve.right.map {
          details =>
            PostcodeLookupViewModel(
              routes.IndividualPostCodeLookupController.onSubmit(mode, index),
              routes.TrusteeAddressController.onPageLoad(mode, index),
              title = Message("messages__individualPostCodeLookup__title"),
              heading = Message("messages__individualPostCodeLookup__heading"),
              subHeading = Some(details.fullName),
              hint = Message("messages__common_individual_postCode_lookup__lede"),
              enterPostcode = Message("messages__trustee_individualPostCodeLookup__enter_postcode")
            )
        }
    }

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      postCodeViewmodel(index, mode).retrieve.right map get
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      postCodeViewmodel(index, mode).retrieve.right.map { vm =>
        post(IndividualPostCodeLookupId(index), vm, mode)
      }
  }
}
