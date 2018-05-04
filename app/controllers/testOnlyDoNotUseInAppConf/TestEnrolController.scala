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

package controllers.testOnlyDoNotUseInAppConf

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.{DataCacheConnector, PSANameCacheConnector}
import controllers.Retrievals
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import forms.mappings.Mappings
import identifiers.TypedIdentifier
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.Enumerable
import views.html.testOnlyDoNotUseInAppConf.testEnrol
import views.html.whatYouWillNeed

import scala.concurrent.Future

class TestEnrolController @Inject()(
                                     appConfig: FrontendAppConfig,
                                     override val messagesApi: MessagesApi,
                                     psaNameCacheConnector: PSANameCacheConnector,
                                     authenticate: AuthAction,
                                     getData: DataRetrievalAction,
                                     requireData: DataRequiredAction
                                   ) extends FrontendController with Mappings with I18nSupport with Enumerable.Implicits with Retrievals {

  val formProvider: Form[String] =
    Form(
      "psaName" -> text("messages__enrolment__error__name_invalid")
    )

  case object PsaNameId extends TypedIdentifier[String] {
    override def toString: String = "psaName"
  }

  def onPageLoad: Action[AnyContent] = authenticate {
    implicit request =>
    Ok(testEnrol(appConfig,formProvider))
  }

  def onSubmit(): Action[AnyContent] = (authenticate andThen getData).async {
    implicit request =>
      formProvider.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(testEnrol(appConfig, formWithErrors))),
        (value) =>
          psaNameCacheConnector.save(request.externalId, PsaNameId, value).map(_ =>
            Redirect(controllers.routes.WhatYouWillNeedController.onPageLoad())

          )
      )
  }
}