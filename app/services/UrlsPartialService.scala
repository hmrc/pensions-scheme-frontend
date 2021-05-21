/*
 * Copyright 2021 HM Revenue & Customs
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

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.{MinimalPsaConnector, PensionSchemeVarianceLockConnector, UpdateSchemeCacheConnector, UserAnswersCacheConnector}
import identifiers.SchemeNameId
import identifiers.racdac.RACDACNameId
import identifiers.register.SubmissionReferenceNumberId
import models.FeatureToggle.Enabled
import models.FeatureToggleName.RACDAC
import models.requests.OptionalDataRequest
import models.{LastUpdated, PSAMinimalFlags}
import play.api.Logger
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.{JsError, JsResultException, JsSuccess, JsValue}
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContent, Result}
import uk.gov.hmrc.http.HeaderCarrier
import utils.UserAnswers
import viewmodels.Message

import java.sql.Timestamp
import java.time.format.DateTimeFormatter
import scala.concurrent.{ExecutionContext, Future}

class UrlsPartialService @Inject()(
                                    override val messagesApi: MessagesApi,
                                    appConfig: FrontendAppConfig,
                                    dataCacheConnector: UserAnswersCacheConnector,
                                    pensionSchemeVarianceLockConnector: PensionSchemeVarianceLockConnector,
                                    updateConnector: UpdateSchemeCacheConnector,
                                    minimalPsaConnector: MinimalPsaConnector,
                                    featureToggleService: FeatureToggleService
                                  ) extends I18nSupport {

  private val logger  = Logger(classOf[UrlsPartialService])

  def schemeLinks(psaId: String)
                 (
                   implicit request: OptionalDataRequest[AnyContent],
                   hc: HeaderCarrier,
                   ec: ExecutionContext
                 ): Future[Seq[OverviewLink]] = {
    for {
      subscription <- subscriptionLinks
      variations <- variationsLinks(psaId)
    } yield {
      subscription ++ variations
    }
  }

  private def racDACSchemeLink(implicit request: OptionalDataRequest[AnyContent],
                               hc: HeaderCarrier,
                          ec: ExecutionContext
                        ):Future[Seq[OverviewLink]] = {
    featureToggleService.get(RACDAC).flatMap {
      case Enabled(_) =>
        val racDACSchemeName = request.userAnswers.flatMap(_.get(RACDACNameId))
        racDACSchemeName match {
          case Some(racDacName) =>
            lastUpdatedAndDeleteDate(request.externalId) map { date =>
              val continueRegistrationLink = Seq(OverviewLink(
                id = "continue-declare-racdac",
                url = appConfig.declareAsRACDACUrl,
                linkText = Message(
                  "messages__schemeOverview__declare_racdac_continue",
                  racDacName,
                  createFormattedDate(date, appConfig.daysDataSaved)
                )
              ))
              continueRegistrationLink
            }
          case _ =>
            Future.successful(
              Seq(OverviewLink(
                id = "declare-racdac",
                url = appConfig.declareAsRACDACUrl,
                linkText = Message("messages__schemeOverview__declare_racdac")
              ))
            )
        }
      case _ =>
        Future(Nil)
      }
  }

  def nonRACDACSchemeLink(
           implicit request: OptionalDataRequest[AnyContent],
           hc: HeaderCarrier,
           ec: ExecutionContext
         ):Future[Seq[OverviewLink]] = {
    val nonRACDACSchemeName = request.userAnswers.flatMap(_.get(SchemeNameId))
    nonRACDACSchemeName match {
      case Some(schemeName) =>
        lastUpdatedAndDeleteDate(request.externalId) map { date =>
          val continueRegistrationLink = Seq(OverviewLink(
            id = "continue-registration",
            url = appConfig.canBeRegisteredUrl,
            linkText = Message(
              "messages__schemeOverview__scheme_subscription_continue",
              schemeName,
              createFormattedDate(date, appConfig.daysDataSaved)
            )
          ))
          continueRegistrationLink
        }
      case _ =>
        Future.successful(
          Seq(OverviewLink(
            id = "register-new-scheme",
            url = appConfig.canBeRegisteredUrl,
            linkText = Message("messages__schemeOverview__scheme_subscription")
          ))
        )
    }
  }

  private def deleteSchemeLink(
                   implicit request: OptionalDataRequest[AnyContent],
                   hc: HeaderCarrier,
                   ec: ExecutionContext
                 ):Future[Seq[OverviewLink]] = {
    val nonRACDACSchemeName = request.userAnswers.flatMap(_.get(SchemeNameId))
    val racDACSchemeName = request.userAnswers.flatMap(_.get(RACDACNameId))
    featureToggleService.get(RACDAC).map { toggleValue =>
      val includeDeleteLink = (toggleValue.isEnabled && racDACSchemeName.isDefined) || nonRACDACSchemeName.isDefined
      if (includeDeleteLink) {
        Seq(OverviewLink(
          id = "delete-registration",
          url = appConfig.deleteSubscriptionUrl,
          linkText = Message(
            "messages__schemeOverview__scheme_subscription_delete",
            contentForDeleteLink(racDACSchemeName, nonRACDACSchemeName)
          )
        ))
      } else {
        Nil
      }
    }
  }

  private def subscriptionLinks(
                                 implicit request: OptionalDataRequest[AnyContent],
                                 hc: HeaderCarrier,
                                 ec: ExecutionContext
                               ): Future[Seq[OverviewLink]] = {
    for {
      rdsl <- racDACSchemeLink
      nrdsl <- nonRACDACSchemeLink
      dsl <- deleteSchemeLink
    } yield {
      nrdsl ++ rdsl ++ dsl
    }
  }

  private def contentForDeleteLink(racDACSchemeName:Option[String], nonRACDACSchemeName:Option[String])(
    implicit request: OptionalDataRequest[AnyContent]
  ):String = {
    (racDACSchemeName, nonRACDACSchemeName) match {
      case (Some(racDAC), Some(nonRACDAC)) =>
        Messages("messages__schemeOverview__scheme_subscription_delete_both", racDAC, nonRACDAC)
      case (Some(sn), None) => sn
      case (None, Some(sn)) => sn
      case _ => ""
    }
  }

  private def variationsLinks(psaId: String)
                             (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[OverviewLink]] =
    pensionSchemeVarianceLockConnector.getLockByPsa(psaId).flatMap {
      case Some(schemeVariance) =>
        updateConnector.fetch(schemeVariance.srn).flatMap {
          case Some(data) => variationsDeleteDate(schemeVariance.srn).map { dateOfDeletion =>
            val schemeName = (data \ "schemeName").as[String]
            Seq(
              OverviewLink(
                id = "continue-variation",
                url = appConfig.viewUrl.format(schemeVariance.srn),
                linkText = Message("messages__schemeOverview__scheme_variations_continue", schemeName, dateOfDeletion)
              ),
              OverviewLink(
                id = "delete-variation",
                url = appConfig.deleteVariationsUrl.format(schemeVariance.srn),
                linkText = Message("messages__schemeOverview__scheme_variations_delete", schemeName)
              )
            )
          }
          case None =>
            Future.successful(Seq.empty[OverviewLink])
        }
      case None =>
        Future.successful(Seq.empty[OverviewLink])
    }

  def checkIfSchemeCanBeRegistered(psaId: String)
                                  (implicit request: OptionalDataRequest[AnyContent], hc: HeaderCarrier, ec: ExecutionContext): Future[Result] =
    for {
      minimalFlags <- minimalPsaConnector.getMinimalFlags(psaId)
      result <- retrieveResult(request.userAnswers, minimalFlags)
    } yield result

  private def retrieveResult(schemeDetailsCache: Option[UserAnswers], minimalFlags: PSAMinimalFlags)
                            (implicit request: OptionalDataRequest[AnyContent], hc: HeaderCarrier, ec: ExecutionContext): Future[Result] = {

    val schemeName = schemeDetailsCache.flatMap(_.get(SchemeNameId))
    val submissionRefNo = schemeDetailsCache.flatMap(_.get(SubmissionReferenceNumberId))

    (schemeName, submissionRefNo) match {
      case (Some(_), _) => Future.successful(redirectBasedOnMinimalFlags(appConfig.continueUrl, minimalFlags))
      case (None, None) => Future.successful(redirectBasedOnMinimalFlags(appConfig.registerUrl, minimalFlags))
      case (None, Some(_)) => dataCacheConnector.removeAll(request.externalId).map { _ =>
          logger.warn("Data cleared as scheme name is missing and srn number was found in mongo collection")
          redirectBasedOnMinimalFlags(appConfig.registerUrl, minimalFlags)
        }
    }
  }

  private def redirectBasedOnMinimalFlags(redirectUrl: String, minimalFlags: PSAMinimalFlags): Result =
    Redirect(
      minimalFlags match {
        case PSAMinimalFlags(true, _, _) => appConfig.cannotStartRegUrl
        case PSAMinimalFlags(_, true, _) => appConfig.youMustContactHMRCUrl
        case _ => redirectUrl
      }
    )

  //DATE FORMATIING HELPER METHODS
  private val formatter = DateTimeFormatter.ofPattern("dd MMMM YYYY")

  private def createFormattedDate(dt: LastUpdated, daysToAdd: Int): String =
    new Timestamp(dt.timestamp).toLocalDateTime.plusDays(daysToAdd).format(formatter)

  private def currentTimestamp: LastUpdated = LastUpdated(System.currentTimeMillis)

  private def parseDateElseCurrent(dateOpt: Option[JsValue]): LastUpdated =
    dateOpt.map(ts =>
      LastUpdated(
        ts.validate[Long] match {
          case JsSuccess(value, _) => value
          case JsError(errors) => throw JsResultException(errors)
        }
      )
    ).getOrElse(currentTimestamp)

  private def lastUpdatedAndDeleteDate(externalId: String)
                                      (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[LastUpdated] =
    dataCacheConnector.lastUpdated(externalId).map { dateOpt =>
      parseDateElseCurrent(dateOpt)
    }

  private def variationsDeleteDate(srn: String)
                                  (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[String] =
    updateConnector.lastUpdated(srn).map { dateOpt =>
      s"${createFormattedDate(parseDateElseCurrent(dateOpt), appConfig.daysDataSaved)}"
    }

}

case class OverviewLink(id: String, url: String, linkText: Message)
