/*
 * Copyright 2020 HM Revenue & Customs
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
import identifiers.register.trustees.individual.{IndividualPreviousAddressPostCodeLookupId, TrusteeNameId}
import javax.inject.Inject
import models.{Index, Mode}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel

import scala.concurrent.ExecutionContext

class IndividualPreviousAddressPostcodeLookupController @Inject()(val appConfig: FrontendAppConfig,
                                                                  override val messagesApi: MessagesApi,
                                                                  val userAnswersService: UserAnswersService,
                                                                  val navigator: Navigator,
                                                                  authenticate: AuthAction,
                                                                  getData: DataRetrievalAction,
                                                                  allowAccess: AllowAccessActionProvider,
                                                                  requireData: DataRequiredAction,
                                                                  formProvider: PostCodeLookupFormProvider,
                                                                  val addressLookupConnector: AddressLookupConnector
                                                                 )(implicit val ec: ExecutionContext) extends PostcodeLookupController with I18nSupport {
  override protected val form: Form[String] = formProvider()

  private def viewmodel(index: Int, mode: Mode, srn: Option[String]): Retrieval[PostcodeLookupViewModel] =
    Retrieval {
      implicit request =>
        trusteeName(index).retrieve.right.map {
          name =>
            PostcodeLookupViewModel(
              routes.IndividualPreviousAddressPostcodeLookupController.onSubmit(mode, index, srn),
              routes.TrusteePreviousAddressController.onPageLoad(mode, index, srn),
              title = Message("messages__trustee_individual_previous_address__heading", Message("messages__theIndividual")),
              heading = Message("messages__trustee_individual_previous_address__heading", name),
              subHeading = Some(name),
              srn = srn
            )
        }
    }

  val trusteeName: Index => Retrieval[String] = (trusteeIndex: Index) => Retrieval {
    implicit request =>
        TrusteeNameId(trusteeIndex).retrieve.right.map(_.fullName)
  }

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        viewmodel(index, mode, srn).retrieve.right map get
    }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      viewmodel(index, mode, srn).retrieve.right.map { vm =>
        post(IndividualPreviousAddressPostCodeLookupId(index), vm, mode)
      }
  }
}
