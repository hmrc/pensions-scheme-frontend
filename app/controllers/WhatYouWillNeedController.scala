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

package controllers

import config.FrontendAppConfig
import connectors.PSANameCacheConnector
import controllers.actions._
import identifiers.{PsaEmailId, PsaNameId}
import javax.inject.Inject
import models.{NormalMode, PSAName}
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.crypto.{ApplicationCrypto, PlainText}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.whatYouWillNeed

import scala.concurrent.Future

class WhatYouWillNeedController @Inject()(appConfig: FrontendAppConfig,
                                          override val messagesApi: MessagesApi,
                                          authenticate: AuthAction,
                                          psaNameCacheConnector: PSANameCacheConnector,
                                          crypto: ApplicationCrypto
                                         ) extends FrontendController with I18nSupport {

  def onPageLoad: Action[AnyContent] = authenticate {
    implicit request =>
      Ok(whatYouWillNeed(appConfig))
  }

  def onSubmit: Action[AnyContent] = authenticate.async {
    implicit request =>

      if (appConfig.isHubEnabled) {
        Future.successful(Redirect(controllers.routes.SchemeTaskListController.onPageLoad()))
      }
      else if(appConfig.isWorkPackageOneEnabled){
        Future.successful(Redirect(controllers.register.routes.SchemeDetailsController.onPageLoad(NormalMode)))
      } else {
        val encryptedCacheId = crypto.QueryParameterCrypto.encrypt(PlainText(request.psaId.id)).value
        for {
          psaNameFromExtId <- psaNameCacheConnector.fetch(request.externalId)
          psaNameFromPsaId <- psaNameCacheConnector.fetch(encryptedCacheId)
          psaNameAndEmail <- savePSANameAndEmail(psaNameFromExtId, psaNameFromPsaId, encryptedCacheId)
        } yield {
          Logger.debug(s"Saved PSA Name and Email $psaNameAndEmail")
          psaNameAndEmail match {
            case Some(psaNameJsValue) =>
              psaNameJsValue.as[PSAName].psaEmail match {
                case None =>
                  Redirect(controllers.register.routes.NeedContactController.onPageLoad)
                case _ =>
                  Redirect(controllers.register.routes.SchemeDetailsController.onPageLoad(NormalMode))
              }
            case _ =>
              Redirect(controllers.register.routes.SchemeDetailsController.onPageLoad(NormalMode))
          }
        }
      }

  }

  private def savePSANameAndEmail(psaNameFromExtId: Option[JsValue],
                                  psaNameFromPsaId: Option[JsValue],
                                  encryptedCacheId: String)(implicit hc: HeaderCarrier): Future[Option[JsValue]] = {

    if (psaNameFromExtId.nonEmpty && psaNameFromPsaId.isEmpty) {
      psaNameFromExtId match {
        case Some(psaNameJsValue) =>
          psaNameJsValue.validate[PSAName].fold(
            _ => {
              Future.failed(PSANameNotFoundException())
            },
            value => {
              for {
                _ <- psaNameCacheConnector.save(encryptedCacheId, PsaNameId, value.psaName)
                _ <- psaNameCacheConnector.save(encryptedCacheId, PsaEmailId, value.psaEmail.getOrElse(""))
              } yield {
                psaNameFromExtId
              }
            }
          )
        case _ =>
          Future(None)
      }
    } else {
      Future(psaNameFromPsaId)
    }
  }
}

final case class PSANameNotFoundException() extends Exception("Unable to retrieve PSA Name")
