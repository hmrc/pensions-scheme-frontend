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

package services

import java.sql.Timestamp
import java.time.format.DateTimeFormatter

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.{MinimalPsaConnector, PensionSchemeVarianceLockConnector, UpdateSchemeCacheConnector, UserAnswersCacheConnector}
import identifiers.SchemeNameId
import identifiers.register.SubmissionReferenceNumberId
import models.LastUpdated
import models.requests.OptionalDataRequest
import play.api.Logger
import play.api.libs.json.{JsError, JsResultException, JsSuccess, JsValue}
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContent, Result}
import uk.gov.hmrc.http.HeaderCarrier
import utils.UserAnswers
import viewmodels.Message

import scala.concurrent.{ExecutionContext, Future}

class UrlsPartialService @Inject()(
                                  appConfig: FrontendAppConfig,
                                  dataCacheConnector: UserAnswersCacheConnector,
                                  pensionSchemeVarianceLockConnector: PensionSchemeVarianceLockConnector,
                                  updateConnector: UpdateSchemeCacheConnector,
                                  minimalPsaConnector: MinimalPsaConnector
                                  )(implicit ec: ExecutionContext) {

  def schemeLinks(implicit request: OptionalDataRequest[AnyContent], hc: HeaderCarrier): Future[Seq[OverviewLink]] =
    for {
      subscription <- subscriptionLinks
      variations <- variationsLinks
    } yield subscription ++ variations

  private def subscriptionLinks(implicit request: OptionalDataRequest[AnyContent], hc: HeaderCarrier): Future[Seq[OverviewLink]] =
   request.userAnswers match {

      case None => Future.successful(Seq(
          OverviewLink("register-new-scheme", appConfig.canBeRegisteredUrl,
          Message("messages__schemeOverview__scheme_subscription"))
        ))

      case Some(ua) =>
        ua.get(SchemeNameId) match {
          case Some(schemeName) =>
            lastUpdatedAndDeleteDate(request.externalId)
              .map(date =>
                Seq(
                  OverviewLink("continue-registration", appConfig.canBeRegisteredUrl,
                    Message("messages__schemeOverview__scheme_subscription_continue", schemeName, createFormattedDate(date, appConfig.daysDataSaved))),
                  OverviewLink("delete-registration", appConfig.deleteSubscriptionUrl,
                    Message("messages__schemeOverview__scheme_subscription_delete", schemeName))))
          case _ => Future.successful(Seq.empty[OverviewLink])
        }
    }

  private def variationsLinks(implicit request: OptionalDataRequest[AnyContent], hc: HeaderCarrier): Future[Seq[OverviewLink]] =
    pensionSchemeVarianceLockConnector.getLockByPsa(request.psaId.id).flatMap {
      case Some(schemeVariance) =>
        updateConnector.fetch(schemeVariance.srn).flatMap {
          case Some(data) => variationsDeleteDate(schemeVariance.srn).map { dateOfDeletion =>
            val schemeName = (data \ "schemeName").as[String]
            Seq(
              OverviewLink("continue-variation", appConfig.viewUrl.format(schemeVariance.srn),
                Message("messages__schemeOverview__scheme_variations_continue", schemeName, dateOfDeletion)),
              OverviewLink("delete-variation", appConfig.deleteVariationsUrl.format(schemeVariance.srn),
                Message("messages__schemeOverview__scheme_variations_delete", schemeName)))
          }
          case None => Future.successful(Seq.empty[OverviewLink])
        }
      case None =>
        Future.successful(Seq.empty[OverviewLink])
    }

  def checkIfSchemeCanBeRegistered(implicit request: OptionalDataRequest[AnyContent], hc: HeaderCarrier): Future[Result] =
    for {
      isPsaSuspended <- minimalPsaConnector.isPsaSuspended(request.psaId.id)
      result <- retrieveResult(request.userAnswers, isPsaSuspended)
    } yield {
      result
    }

  //LINK WITH PSA SUSPENSION HELPER METHODS
  private def retrieveResult(schemeDetailsCache: Option[UserAnswers], isPsaSuspended: Boolean
                            )(implicit request: OptionalDataRequest[AnyContent], hc: HeaderCarrier): Future[Result] =
    schemeDetailsCache match {
      case None => Future.successful(redirectBasedOnPsaSuspension(appConfig.registerUrl, isPsaSuspended))
      case Some(ua) => ua.get(SchemeNameId) match {
        case Some(_) => Future.successful(redirectBasedOnPsaSuspension(appConfig.continueUrl, isPsaSuspended))
        case _ => deleteDataIfSrnNumberFoundAndRedirect(ua, isPsaSuspended)
      }
    }

  private def deleteDataIfSrnNumberFoundAndRedirect(ua: UserAnswers, isPsaSuspended: Boolean
                                                   )(implicit request: OptionalDataRequest[AnyContent], hc: HeaderCarrier): Future[Result] =

    ua.get(SubmissionReferenceNumberId).fold {
      Logger.warn("Page load failed since both scheme name and srn number were not found in scheme registration mongo collection")
      Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
    }{ _ => dataCacheConnector.removeAll(request.externalId).map { _ =>
        Logger.warn("Data cleared as scheme name is missing and srn number was found in mongo collection")
        redirectBasedOnPsaSuspension(appConfig.registerSchemeUrl, isPsaSuspended)
      }}

  private def redirectBasedOnPsaSuspension(redirectUrl: String, isPsaSuspended: Boolean): Result =
      if (isPsaSuspended)  Redirect(appConfig.cannotStartRegUrl) else Redirect(redirectUrl)

  //DATE FORMATIING HELPER METHODS
  private val formatter = DateTimeFormatter.ofPattern("dd MMMM YYYY")

  private def createFormattedDate(dt: LastUpdated, daysToAdd: Int): String =
    new Timestamp(dt.timestamp).toLocalDateTime.plusDays(daysToAdd).format(formatter)

  private def currentTimestamp: LastUpdated = LastUpdated(System.currentTimeMillis)

  private def parseDateElseCurrent(dateOpt: Option[JsValue]): LastUpdated = {
    dateOpt.map(ts =>
      LastUpdated(
        ts.validate[Long] match {
          case JsSuccess(value, _) => value
          case JsError(errors) => throw JsResultException(errors)
        }
      )
    ).getOrElse(currentTimestamp)
  }

  private def lastUpdatedAndDeleteDate(externalId: String)(implicit hc: HeaderCarrier): Future[LastUpdated] =
    dataCacheConnector.lastUpdated(externalId).map { dateOpt =>
      parseDateElseCurrent(dateOpt)
    }

  private def variationsDeleteDate(srn: String)(implicit hc: HeaderCarrier): Future[String] =
    updateConnector.lastUpdated(srn).map { dateOpt =>
      s"${createFormattedDate(parseDateElseCurrent(dateOpt), appConfig.daysDataSaved)}"
    }

}

case class OverviewLink(id: String, url: String, linkText: Message)