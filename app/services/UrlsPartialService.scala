/*
 * Copyright 2023 HM Revenue & Customs
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
import connectors.{MinimalPsaConnector, PensionSchemeVarianceLockConnector, UserAnswersCacheConnector, UpdateSchemeCacheConnector}
import identifiers.SchemeNameId
import identifiers.racdac.RACDACNameId
import identifiers.register.SubmissionReferenceNumberId
import models.requests.OptionalDataRequest
import models.{LastUpdated, PSAMinimalFlags}
import play.api.Logger
import play.api.i18n.{MessagesApi, I18nSupport}
import play.api.libs.json.{JsResultException, JsError, JsSuccess, JsValue}
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContent, Result}
import uk.gov.hmrc.http.HeaderCarrier
import utils.UserAnswers
import utils.annotations.Racdac
import viewmodels.Message

import java.sql.Timestamp
import java.time.format.DateTimeFormatter
import scala.concurrent.{ExecutionContext, Future}

class UrlsPartialService @Inject()(
                                    override val messagesApi: MessagesApi,
                                    appConfig: FrontendAppConfig,
                                    dataCacheConnector: UserAnswersCacheConnector,
                                    @Racdac racdacDataCacheConnector: UserAnswersCacheConnector,
                                    pensionSchemeVarianceLockConnector: PensionSchemeVarianceLockConnector,
                                    updateConnector: UpdateSchemeCacheConnector,
                                    minimalPsaConnector: MinimalPsaConnector
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
    racdacDataCacheConnector.fetch(request.externalId).flatMap {
      f =>
        val racDACSchemeName = f.map(UserAnswers).flatMap(_.get(RACDACNameId))
        racDACSchemeName match {
          case Some(racDacName) =>
            lastUpdatedAndDeleteDate(request.externalId, racdacDataCacheConnector.lastUpdated) map { date =>
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
        lastUpdatedAndDeleteDate(request.externalId, dataCacheConnector.lastUpdated) map { date =>
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

  private def deleteSchemeLink(implicit request: OptionalDataRequest[AnyContent]):Seq[OverviewLink] = {
    val nonRACDACSchemeName = request.userAnswers.flatMap(_.get(SchemeNameId))
      val includeDeleteLink = nonRACDACSchemeName.isDefined
      if (includeDeleteLink) {
        Seq(OverviewLink(
          id = "delete-registration",
          url = appConfig.deleteSubscriptionUrl,
          linkText = Message(
            "messages__schemeOverview__scheme_subscription_delete",
            nonRACDACSchemeName.getOrElse("")
          )
        ))
      } else {
        Nil
      }

  }

  private def racDACDeleteSchemeLink(
                                implicit request: OptionalDataRequest[AnyContent],
                                hc: HeaderCarrier,
                                ec: ExecutionContext
                              ):Future[Seq[OverviewLink]] = {
    racdacDataCacheConnector.fetch(request.externalId).map{
      f =>
        val userAnswers = f.map(UserAnswers)
        val racDACSchemeName = userAnswers.flatMap(_.get(RACDACNameId))
          if (racDACSchemeName.isDefined) {
            Seq(OverviewLink(
              id = "delete-racdac-registration",
              url = appConfig.deleteSubscriptionRacdacUrl,
              linkText = Message(
                "messages__schemeOverview__scheme_subscription_delete",
                racDACSchemeName.getOrElse(""))
              )
            )
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
      rddsl <- racDACDeleteSchemeLink
    } yield {
      nrdsl ++ deleteSchemeLink ++ rdsl  ++ rddsl
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

  private def parseDateElseException(dateOpt: Option[JsValue]): LastUpdated =
    dateOpt.map(ts =>
      LastUpdated(
        ts.validate[Long] match {
          case JsSuccess(value, _) => value
          case JsError(errors) => throw JsResultException(errors)
        }
      )
    ).getOrElse(throw new RuntimeException("No last updated date found"))

  private def lastUpdatedAndDeleteDate(externalId: String, dateRetriever: String => Future[Option[JsValue]])
                                      (implicit ec: ExecutionContext): Future[LastUpdated] = {
    dateRetriever(externalId).map { dateOpt =>
      parseDateElseException(dateOpt)
    }
  }

  private def variationsDeleteDate(srn: String)
                                  (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[String] =
    updateConnector.lastUpdated(srn).map { dateOpt =>
      s"${createFormattedDate(parseDateElseException(dateOpt), appConfig.daysDataSaved)}"
    }

}

case class OverviewLink(id: String, url: String, linkText: Message)
