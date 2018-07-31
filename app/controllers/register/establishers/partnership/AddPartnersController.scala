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

package controllers.register.establishers.partnership

import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.Retrievals
import controllers.actions._
import forms.register.AddPartnersFormProvider
import identifiers.register.establishers.partnership.AddPartnersId
import javax.inject.Inject
import models.NormalMode
import play.api.Logger
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.JsResultException
import play.api.mvc.{Action, AnyContent, Call}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.Navigator
import utils.annotations.EstablishersPartner
import views.html.register.addPartners

import scala.concurrent.Future

class AddPartnersController @Inject()(
                                                     appConfig: FrontendAppConfig,
                                                     override val messagesApi: MessagesApi,
                                                     dataCacheConnector: DataCacheConnector,
                                                     @EstablishersPartner navigator: Navigator,
                                                     authenticate: AuthAction,
                                                     getData: DataRetrievalAction,
                                                     requireData: DataRequiredAction,
                                                     formProvider: AddPartnersFormProvider
                                                   ) extends FrontendController with I18nSupport with Retrievals {

  private val form: Form[Boolean] = formProvider()
  private def postUrl(index: Int): Call = routes.AddPartnersController.onSubmit(index)

  def onPageLoad(index: Int): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrievePartnershipName(index) {
        partnershipName =>
          val partners = request.userAnswers.allPartnersAfterDelete(index)
          Future.successful(Ok(addPartners(appConfig, form, index, partnershipName, partners, postUrl(index))))
      }
  }

  def onSubmit(index: Int): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      val partners = request.userAnswers.allPartnersAfterDelete(index)
      if (partners.isEmpty || partners.lengthCompare(appConfig.maxPartners) >= 0) {
        Future.successful(Redirect(navigator.nextPage(AddPartnersId(index), NormalMode, request.userAnswers)))
      }
      else {

        form.bindFromRequest().fold(
          (formWithErrors: Form[_]) =>
            retrievePartnershipName(index) {
              partnershipName =>
                Future.successful(
                  BadRequest(
                    addPartners(
                      appConfig,
                      formWithErrors,
                      index,
                      partnershipName,
                      partners,
                      postUrl(index)
                    )
                  )
                )
            },
          value =>
            request.userAnswers.set(AddPartnersId(index))(value).fold(
              errors => {
                Logger.error("Unable to set user answer", JsResultException(errors))
                Future.successful(InternalServerError)
              },
              userAnswers => {
                Future.successful(Redirect(navigator.nextPage(AddPartnersId(index), NormalMode, userAnswers)))
              }
            )
        )
      }
  }

}
