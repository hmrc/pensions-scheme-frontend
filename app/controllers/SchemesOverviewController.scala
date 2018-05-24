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

import java.time.{Instant, ZoneId}
import javax.inject.Inject

import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.actions._
import identifiers.register.SchemeDetailsId
import models.LastUpdatedDate
import org.joda.time.{DateTime, DateTimeZone, LocalDate}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.schemesOverview

import scala.concurrent.Future

class SchemesOverviewController @Inject()(appConfig: FrontendAppConfig,
                                         override val messagesApi: MessagesApi,
                                          dataCacheConnector: DataCacheConnector,
                                         authenticate: AuthAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction) extends FrontendController with Retrievals with I18nSupport {

  def onPageLoad: Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers.get(SchemeDetailsId) match {
        case None => Future.successful(Ok(schemesOverview(appConfig, None, None, None)))
        case Some(scheme) =>
          dataCacheConnector.fetchValue(request.externalId, "lastUpdated").map { dateOpt =>
            val date = dateOpt.map(_.as[LastUpdatedDate]).getOrElse(currentTimestamp)
            Ok(schemesOverview(
                appConfig,
                Some(scheme.schemeName),
                Some(s"${f(date, 0)}"),
                Some(s"${f(date, appConfig.daysDataSaved)}")
            ))
        }
        }
      }

  private def f(dt: LastUpdatedDate, daysToAdd: Int): String = new LocalDate(dt.timestamp).plusDays(daysToAdd).toString
  private def currentTimestamp: LastUpdatedDate = LastUpdatedDate(DateTime.now(DateTimeZone.UTC).getMillis())

}
