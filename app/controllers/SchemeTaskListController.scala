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

package controllers

import config.{FeatureSwitchManagementService, FrontendAppConfig}
import connectors._
import controllers.actions._
import handlers.ErrorHandler
import identifiers.register.establishers.company.director.{DirectorAddressId, ExistingCurrentAddressId => DirectorExistingCurrentAddressId}
import identifiers.register.establishers.company.{CompanyAddressId, ExistingCurrentAddressId => CompanyExistingCurrentAddressId}
import identifiers.register.establishers.individual.{AddressId, ExistingCurrentAddressId}
import identifiers.register.establishers.partnership.partner.{PartnerAddressId, ExistingCurrentAddressId => PartnerExistingCurrentAddressId}
import identifiers.register.establishers.partnership.{PartnershipAddressId, ExistingCurrentAddressId => PartnershipExistingCurrentAddressId}
import identifiers.register.trustees.company.{CompanyAddressId => TrusteeCompanyAddressId, ExistingCurrentAddressId => TrusteeCompanyExistingCurrentAddressId}
import identifiers.register.trustees.individual.{TrusteeAddressId, ExistingCurrentAddressId => TrusteeExistingCurrentAddressId}
import identifiers.register.trustees.partnership.{ExistingCurrentAddressId => TrusteePartnershipExistingCurrentAddressId, PartnershipAddressId => TrusteePartnershipAddressId}
import identifiers.{IsPsaSuspendedId, TypedIdentifier}
import javax.inject.Inject
import models.address.Address
import models.details.transformation.SchemeDetailsMasterSection
import models.register._
import models.requests.OptionalDataRequest
import models.{Mode, VarianceLock}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.hstasklisthelper.{HsTaskListHelperRegistration, HsTaskListHelperVariations}
import utils.{Toggles, UserAnswers}
import viewmodels.SchemeDetailsTaskList
import views.html.{psa_scheme_details, schemeDetailsTaskList}

import scala.concurrent.{ExecutionContext, Future}

class SchemeTaskListController @Inject()(appConfig: FrontendAppConfig,
                                         override val messagesApi: MessagesApi,
                                         authenticate: AuthAction,
                                         getData: DataRetrievalAction,
                                         schemeDetailsConnector: SchemeDetailsConnector,
                                         schemeTransformer: SchemeDetailsMasterSection,
                                         errorHandler: ErrorHandler,
                                         featureSwitchManagementService: FeatureSwitchManagementService,
                                         lockConnector: PensionSchemeVarianceLockConnector,
                                         viewConnector: SchemeDetailsReadOnlyCacheConnector,
                                         updateConnector: UpdateSchemeCacheConnector,
                                         minimalPsaConnector: MinimalPsaConnector
                                        )(implicit val ec: ExecutionContext) extends FrontendController with I18nSupport with Retrievals {

  def onPageLoad(mode: Mode, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode, srn)).async {
    implicit request =>
      (srn, request.userAnswers) match {

        case (None, Some(userAnswers)) =>
          Future.successful(Ok(schemeDetailsTaskList(appConfig, new HsTaskListHelperRegistration(userAnswers).taskList)))

        case (Some(srnValue), _) if !featureSwitchManagementService.get(Toggles.isVariationsEnabled) =>
          onPageLoadVariationsToggledOff(srnValue)

        case (Some(srnValue), optionUserAnswers) =>
          onPageLoadVariationsToggledOn(srnValue, optionUserAnswers)
        case _ => Future.successful(Redirect(appConfig.managePensionsSchemeOverviewUrl))
      }
  }

  private def onPageLoadVariationsToggledOff(srn: String)(implicit
                                                          request: OptionalDataRequest[AnyContent],
                                                          hc: HeaderCarrier): Future[Result] = {

    schemeDetailsConnector.getSchemeDetails(request.psaId.id, schemeIdType = "srn", srn).flatMap { scheme =>

      val schemeDetailMasterSection = schemeTransformer.transformMasterSection(scheme)
      Future.successful(Ok(psa_scheme_details(appConfig, schemeDetailMasterSection, scheme.schemeDetails.name, srn)))

    }
  }

  private def onPageLoadVariationsToggledOn(srn: String,
                                            ua: Option[UserAnswers])(implicit request: OptionalDataRequest[AnyContent],
                                                                     hc: HeaderCarrier): Future[Result] = {
    lockConnector.isLockByPsaIdOrSchemeId(request.psaId.id, srn).flatMap {
      case Some(VarianceLock) =>
        ua match {
          case Some(userAnswers) =>
            createViewWithSuspensionFlag(srn, userAnswers, updateConnector.upsert(srn, _))
          case _ =>
            Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
        }

      case _ =>
        schemeDetailsConnector.getSchemeDetailsVariations(request.psaId.id, schemeIdType = "srn", srn)
          .flatMap { userAnswers =>
            createViewWithSuspensionFlag(srn, userAnswers, viewConnector.upsert(request.externalId, _))
          }
    }
  }

  private def createViewWithSuspensionFlag(srn: String, userAnswers: UserAnswers,
                                           upsertUserAnswers: JsValue => Future[JsValue])(implicit request: OptionalDataRequest[AnyContent],
                                                                                          hc: HeaderCarrier): Future[Result] =
    minimalPsaConnector.isPsaSuspended(request.psaId.id).flatMap { isSuspended =>

      val establishersAddresssIdsMap = userAnswers.allEstablishersAfterDelete.flatMap(getSeqOfEstAddressIds(_, userAnswers)).toMap
      val trusteeAddressIdsMap = userAnswers.allTrusteesAfterDelete.flatMap(getSeqOfTrusteeAddressIds(_, userAnswers))

      val updatedUserAnswers = userAnswers.set(IsPsaSuspendedId)(isSuspended).flatMap(
        _.setAllExistingAddress(establishersAddresssIdsMap ++ trusteeAddressIdsMap)
      ).asOpt.getOrElse(userAnswers)

      val taskList: SchemeDetailsTaskList = new HsTaskListHelperVariations(updatedUserAnswers, request.viewOnly, Some(srn)).taskList

      upsertUserAnswers(updatedUserAnswers.json).flatMap { _ =>

        Future.successful(Ok(schemeDetailsTaskList(appConfig, taskList)))
      }
    }

  private def getSeqOfEstAddressIds(establishers: Establisher[_], userAnswers: UserAnswers): Seq[(TypedIdentifier[Address], TypedIdentifier[Address])] = {
    establishers match {
      case models.register.EstablisherIndividualEntity(id, _, _, _, _, _) =>
        Seq(AddressId(id.index) -> ExistingCurrentAddressId(id.index))
      case EstablisherCompanyEntity(id, _, _, _, _, _) =>
        val allDirectors = userAnswers.allDirectorsAfterDelete(id.index)
        val allDirectorsAddressIdMap = allDirectors.map { director =>
          val index = allDirectors.indexOf(director)
          (DirectorAddressId(id.index, index), DirectorExistingCurrentAddressId(id.index, index))
        }
        allDirectorsAddressIdMap :+ (CompanyAddressId(id.index) -> CompanyExistingCurrentAddressId(id.index))
      case EstablisherPartnershipEntity(id, _, _, _, _, _) =>
        val allPartners = userAnswers.allPartnersAfterDelete(id.index)
        val allPartnersAddressIdMap = allPartners.map { director =>
          val index = allPartners.indexOf(director)
          (PartnerAddressId(id.index, index), PartnerExistingCurrentAddressId(id.index, index))
        }
        allPartnersAddressIdMap :+ PartnershipAddressId(id.index) -> PartnershipExistingCurrentAddressId(id.index)
      case _ =>
        Seq.empty
    }
  }

  private def getSeqOfTrusteeAddressIds(trustee: Trustee[_], userAnswers: UserAnswers): Seq[(TypedIdentifier[Address], TypedIdentifier[Address])] = {
    trustee match {
      case models.register.TrusteeIndividualEntity(id, _, _, _, _, _, _) =>
        Seq(TrusteeAddressId(id.index) -> TrusteeExistingCurrentAddressId(id.index))
      case TrusteeCompanyEntity(id, _, _, _, _, _, _) =>
        Seq(TrusteeCompanyAddressId(id.index) -> TrusteeCompanyExistingCurrentAddressId(id.index))
      case TrusteePartnershipEntity(id, _, _, _, _, _, _) =>
        Seq(TrusteePartnershipAddressId(id.index) -> TrusteePartnershipExistingCurrentAddressId(id.index))
      case _ =>
        Seq.empty
    }
  }
  case class TaskListDetails(userAnswers: UserAnswers, taskList: SchemeDetailsTaskList)
}
