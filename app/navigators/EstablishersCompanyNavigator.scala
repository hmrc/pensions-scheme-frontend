/*
 * Copyright 2024 HM Revenue & Customs
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

package navigators

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.register.establishers.company.routes._
import controllers.register.establishers.company.{routes => establisherCompanyRoutes}
import controllers.routes._
import identifiers.register.establishers.IsEstablisherNewId
import identifiers.register.establishers.company._
import identifiers.register.establishers.company.director.{TrusteeAlsoDirectorId, TrusteesAlsoDirectorsId}
import models.FeatureToggleName.SchemeRegistration
import models.Mode._
import models._
import utils.UserAnswers

//scalastyle:off cyclomatic.complexity
class EstablishersCompanyNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector,
                                             appConfig: FrontendAppConfig) extends AbstractNavigator {

  private def exitMiniJourney(index: Int,
                              mode: Mode,
                              srn: SchemeReferenceNumber,
                              answers: UserAnswers,
                              cyaPage: (Int, mode, SchemeReferenceNumber) => Option[NavigateTo]): Option[NavigateTo] = {
    if (mode == CheckMode || mode == NormalMode)
      cyaPage(index, journeyMode(mode), srn)
    else if (answers.get(IsEstablisherNewId(index)).getOrElse(false))
      cyaPage(index, journeyMode(mode), srn)
    else
      anyMoreChanges(srn)
  }


  private def cyaCompanyDetails(index: Int, mode: Mode, srn: SchemeReferenceNumber): Option[NavigateTo] =
    NavigateTo.dontSave(establisherCompanyRoutes.CheckYourAnswersCompanyDetailsController.onPageLoad(mode, srn, index))

  private def cyaContactDetails(index: Int, mode: Mode, srn: SchemeReferenceNumber): Option[NavigateTo] =
    NavigateTo.dontSave(establisherCompanyRoutes.CheckYourAnswersCompanyContactDetailsController.onPageLoad(mode, srn, index))

  private def cyaAddressDetails(index: Int, mode: Mode, srn: SchemeReferenceNumber): Option[NavigateTo] =
    NavigateTo.dontSave(establisherCompanyRoutes.CheckYourAnswersCompanyAddressController.onPageLoad(mode, srn, index))

  private def anyMoreChanges(srn: SchemeReferenceNumber): Option[NavigateTo] =
    NavigateTo.dontSave(AnyMoreChangesController.onPageLoad(srn))

  //scalastyle:off method.length
  protected def routes(from: NavigateFrom, mode: Mode, srn: SchemeReferenceNumber): Option[NavigateTo] =
    from.id match {
      case CompanyDetailsId(index) =>
        NavigateTo.dontSave(
          controllers.register.establishers.routes.PsaSchemeTaskListRegistrationEstablisherController.onPageLoad(index)
        )
      case HasCompanyCRNId(index) =>
        confirmHasCompanyNumber(index, mode, srn)(from.userAnswers)
      case CompanyEnterCRNId(index) =>
        NavigateTo.dontSave(establisherCompanyRoutes.HasCompanyUTRController.onPageLoad(mode, srn, index))
      case HasCompanyVATId(index) =>
        confirmHasCompanyVat(index, mode, srn)(from.userAnswers)
      case CompanyEnterVATId(index) =>
        navigateOrSessionExpired(from.userAnswers, CompanyEnterVATId(index), (_: ReferenceValue) =>
          establisherCompanyRoutes.HasCompanyPAYEController.onPageLoad(mode, srn, index))
      case HasCompanyPAYEId(index) => confirmHasCompanyPAYE(index, mode, srn)(from.userAnswers)
      case CompanyNoCRNReasonId(index) =>
        NavigateTo.dontSave(establisherCompanyRoutes.HasCompanyUTRController.onPageLoad(mode, srn, index))
      case HasCompanyUTRId(index) =>
        confirmHasCompanyUtr(index, mode, srn)(from.userAnswers)
      case CompanyEnterUTRId(index) =>
        NavigateTo.dontSave(establisherCompanyRoutes.HasCompanyVATController.onPageLoad(mode, srn, index))
      case CompanyNoUTRReasonId(index) =>
        NavigateTo.dontSave(establisherCompanyRoutes.HasCompanyVATController.onPageLoad(mode, srn, index))
      case CompanyEnterPAYEId(index) =>
        payeRoutes(index, mode, srn)(from.userAnswers)
      case CompanyPostCodeLookupId(index) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CompanyAddressListController.onPageLoad(mode, srn, index))
      case CompanyAddressListId(index) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CompanyAddressYearsController.onPageLoad(mode, srn, index))
      case CompanyAddressId(index) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CompanyAddressYearsController.onPageLoad(mode, srn, index))
      case CompanyAddressYearsId(index) =>
        addressYearsRoutes(index, from.userAnswers, mode, srn)
      case CompanyPreviousAddressPostcodeLookupId(index) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CompanyPreviousAddressListController.onPageLoad(mode, srn, index))
      case CompanyPreviousAddressListId(index) =>
        previousAddressRoutes(index, mode, srn)
      case CompanyPreviousAddressId(index) =>
        previousAddressRoutes(index, mode, srn)
      case CompanyPhoneId(index) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CheckYourAnswersCompanyContactDetailsController.onPageLoad(mode, srn, index))
      case HasBeenTradingCompanyId(index) =>
        confirmHasBeenTrading(index, mode, srn)(from.userAnswers)
      case CompanyEmailId(index) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CompanyPhoneController.onPageLoad(mode, srn, index))
      case AddCompanyDirectorsId(index) =>
        addDirectors(mode, index, from.userAnswers, srn)
      case TrusteeAlsoDirectorId(index) => trusteeAlsoDirectorNav(from.userAnswers, index, NormalMode, srn)
      case TrusteesAlsoDirectorsId(index) => trusteesAlsoDirectorsNav(from.userAnswers, index, NormalMode, srn)
      case OtherDirectorsId(_) =>
        if (mode == CheckMode || mode == NormalMode) {
          NavigateTo.dontSave(PsaSchemeTaskListController.onPageLoad(mode, srn))
        } else {
          NavigateTo.dontSave(AnyMoreChangesController.onPageLoad(srn))
        }
      case CheckYourAnswersId(index) =>
        listOrAnyMoreChange(index, mode, srn)(from.userAnswers)
      case _ => None
    }

  def previousAddressRoutes(index: Int, mode: Mode, srn: SchemeReferenceNumber): Option[NavigateTo] = {
    NavigateTo.dontSave(establisherCompanyRoutes.CheckYourAnswersCompanyAddressController.onPageLoad(mode, srn, index))
  }

  def previousAddressEditRoutes(index: Int, mode: Mode, srn: SchemeReferenceNumber, userAnswers: UserAnswers): Option[NavigateTo] = {
    exitMiniJourney(index, mode, srn, userAnswers, cyaAddressDetails)
  }


  protected def editRoutes(from: NavigateFrom, mode: Mode, srn: SchemeReferenceNumber): Option[NavigateTo] =
    from.id match {
      case CompanyDetailsId(index) => exitMiniJourney(index, mode, srn, from.userAnswers, cyaCompanyDetails)
      case HasCompanyCRNId(index) => confirmHasCompanyNumber(index, mode, srn)(from.userAnswers)
      case HasCompanyVATId(index) => confirmHasCompanyVat(index, mode, srn)(from.userAnswers)
      case HasCompanyPAYEId(index) => confirmHasCompanyPAYE(index, mode, srn)(from.userAnswers)
      case CompanyEnterVATId(index) => exitMiniJourney(index, mode, srn, from.userAnswers, cyaCompanyDetails)
      case CompanyEnterPAYEId(index) => exitMiniJourney(index, mode, srn, from.userAnswers, cyaCompanyDetails)
      case CompanyNoCRNReasonId(index) => exitMiniJourney(index, mode, srn, from.userAnswers, cyaCompanyDetails)
      case HasCompanyUTRId(index) => confirmHasCompanyUtr(index, mode, srn)(from.userAnswers)
      case CompanyEnterCRNId(index) => exitMiniJourney(index, mode, srn, from.userAnswers, cyaCompanyDetails)
      case CompanyEnterUTRId(index) => exitMiniJourney(index, mode, srn, from.userAnswers, cyaCompanyDetails)
      case CompanyNoUTRReasonId(index) => exitMiniJourney(index, mode, srn, from.userAnswers, cyaCompanyDetails)

      case CompanyPostCodeLookupId(index) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CompanyAddressListController.onPageLoad(mode, srn, index))

      case CompanyAddressListId(index) =>
        addressRoutes(index, from.userAnswers, mode, srn)
      case CompanyAddressId(index) =>
        addressRoutes(index, from.userAnswers, mode, srn)
      case CompanyAddressYearsId(index) =>

        editAddressYearsRoutes(index, from.userAnswers, mode, srn)

      case CompanyConfirmPreviousAddressId(index) => confirmPreviousAddressRoutes(index, mode, srn)(from.userAnswers)

      case CompanyPreviousAddressPostcodeLookupId(index) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CompanyPreviousAddressListController.onPageLoad(mode, srn, index))

      case CompanyPreviousAddressListId(index) =>
        previousAddressEditRoutes(index, mode, srn, from.userAnswers)
      case CompanyPreviousAddressId(index) => previousAddressEditRoutes(index, mode, srn, from.userAnswers)
      case CompanyPhoneId(index) => exitMiniJourney(index, mode, srn, from.userAnswers, cyaContactDetails)
      case CompanyEmailId(index) => exitMiniJourney(index, mode, srn, from.userAnswers, cyaContactDetails)
      case IsCompanyDormantId(index) => exitMiniJourney(index, mode, srn, from.userAnswers, cyaCompanyDetails)
      case OtherDirectorsId(index) =>
        NavigateTo.dontSave(PsaSchemeTaskListController.onPageLoad(journeyMode(mode), srn))
      case HasBeenTradingCompanyId(index) => confirmHasBeenTrading(index, mode, srn)(from.userAnswers)
      case _ => None
    }

  override protected def routeMap(from: NavigateFrom, srn: SchemeReferenceNumber): Option[NavigateTo] = from.id match {
    case IsCompanyDormantId(index) =>
      NavigateTo.dontSave(establisherCompanyRoutes.CheckYourAnswersCompanyDetailsController.onPageLoad(NormalMode, srn, index))
    case _ => routes(from, NormalMode, srn)
  }

  override protected def updateRouteMap(from: NavigateFrom, srn: SchemeReferenceNumber): Option[NavigateTo] =
    routes(from, UpdateMode, srn)

  override protected def editrouteMap(from: NavigateFrom, srn: SchemeReferenceNumber): Option[NavigateTo] = editRoutes(from, CheckMode, None)

  override protected def checkUpdateRouteMap(from: NavigateFrom, srn: SchemeReferenceNumber): Option[NavigateTo] = editRoutes(from, CheckUpdateMode, srn)

  private def addressYearsRoutes(index: Int, answers: UserAnswers, mode: Mode, srn: SchemeReferenceNumber): Option[NavigateTo] = {
    answers.get(CompanyAddressYearsId(index)) match {
      case Some(AddressYears.UnderAYear) =>
        NavigateTo.dontSave(establisherCompanyRoutes.HasBeenTradingCompanyController.onPageLoad(mode, srn, index))
      case Some(AddressYears.OverAYear) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CheckYourAnswersCompanyAddressController.onPageLoad(mode, srn, index))
      case _ =>
        NavigateTo.dontSave(SessionExpiredController.onPageLoad)
    }
  }

  private def addressRoutes(index: Int, answers: UserAnswers, mode: Mode, srn: SchemeReferenceNumber): Option[NavigateTo] = {
    (mode, answers.get(IsEstablisherNewId(index))) match {
      case (CheckMode, _) => cyaAddressDetails(index, journeyMode(mode), srn)
      case (_, Some(true)) => cyaAddressDetails(index, journeyMode(mode), srn)
      case (CheckUpdateMode, Some(false)) =>
        NavigateTo.dontSave(CompanyConfirmPreviousAddressController.onPageLoad(index, srn))
      case (CheckUpdateMode, None) =>
        NavigateTo.dontSave(CompanyConfirmPreviousAddressController.onPageLoad(index, srn))
      case _ => NavigateTo.dontSave(establisherCompanyRoutes.CompanyAddressYearsController.onPageLoad(mode, srn, index))

    }
  }

  private def editAddressYearsRoutes(index: Int, answers: UserAnswers, mode: Mode, srn: SchemeReferenceNumber): Option[NavigateTo] = {
    (
      answers.get(CompanyAddressYearsId(index)),
      answers.get(IsEstablisherNewId(index)).getOrElse(false)
    ) match {
      case (Some(AddressYears.UnderAYear), false) =>
        NavigateTo.dontSave(CompanyConfirmPreviousAddressController.onPageLoad(index, srn))
      case (Some(AddressYears.UnderAYear), _) =>
        NavigateTo.dontSave(HasBeenTradingCompanyController.onPageLoad(mode, srn, index))
      case (Some(AddressYears.OverAYear), _) =>
        exitMiniJourney(index, mode, srn, answers, cyaAddressDetails)
      case _ =>
        NavigateTo.dontSave(SessionExpiredController.onPageLoad)
    }
  }

  private def trusteeAlsoDirectorNav(userAnswers: UserAnswers, index: Int, mode: Mode, srn: SchemeReferenceNumber) = {
    NavigateTo.dontSave(
      userAnswers.get(TrusteeAlsoDirectorId(index)) match {
        case Some(v) if v > -1 =>
          controllers.register.establishers.company.routes.AddCompanyDirectorsController
            .onPageLoad(mode, srn, index)
        case _ => controllers.register.establishers.company.director.routes.DirectorNameController
          .onPageLoad(mode, index, userAnswers.allDirectors(index).size, srn)
      }
    )
  }

  private def trusteesAlsoDirectorsNav(userAnswers: UserAnswers, index: Int, mode: Mode, srn: SchemeReferenceNumber) = {
    NavigateTo.dontSave(
      userAnswers.get(TrusteesAlsoDirectorsId(index)) match {
        case Some(v) if v.contains(-1) =>
          controllers.register.establishers.company.director.routes.DirectorNameController
            .onPageLoad(mode, index, userAnswers.allDirectors(index).size, srn)

        case _ => controllers.register.establishers.company.routes.AddCompanyDirectorsController
          .onPageLoad(mode, srn, index)
      }
    )
  }

  private def addDirectors(mode: Mode, index: Int, answers: UserAnswers, srn: SchemeReferenceNumber): Option[NavigateTo] = {
    NavigateTo.dontSave(
      if (answers.allDirectorsAfterDelete(index).isEmpty) {
        controllers.register.establishers.company.director.routes.DirectorNameController
          .onPageLoad(mode, index, answers.allDirectors(index).size, srn)
      } else if (answers.allDirectorsAfterDelete(index).length < appConfig.maxDirectors) {
        answers.get(AddCompanyDirectorsId(index)).map { addCompanyDirectors =>
          if (addCompanyDirectors) {
            mode match {
              case NormalMode | CheckMode =>
                controllers.register.establishers.company.director.routes.TrusteesAlsoDirectorsController
                  .onPageLoad(index)
              case _ => controllers.register.establishers.company.director.routes.DirectorNameController
                .onPageLoad(mode, index, answers.allDirectors(index).size, srn)
            }
          } else {
            if (mode == CheckMode || mode == NormalMode) { // TODO: Remove Json code below when SchemeRegistration toggle is removed
              (answers.json \ SchemeRegistration.asString).asOpt[Boolean] match {
                case Some(true) =>
                  controllers.register.establishers.routes.PsaSchemeTaskListRegistrationEstablisherController.onPageLoad(index)
                case _ =>
                  PsaSchemeTaskListController.onPageLoad(mode, srn)
              }
            } else {
              AnyMoreChangesController.onPageLoad(srn)
            }
          }
        }.getOrElse(controllers.routes.SessionExpiredController.onPageLoad)
      } else {
        establisherCompanyRoutes.OtherDirectorsController.onPageLoad(mode, srn, index)
      }
    )
  }

  private def listOrAnyMoreChange(index: Int, mode: Mode, srn: SchemeReferenceNumber)(answers: UserAnswers): Option[NavigateTo] = {
    mode match {
      case CheckMode | NormalMode =>
        NavigateTo.dontSave(establisherCompanyRoutes.AddCompanyDirectorsController.onPageLoad(mode, srn, index))
      case _ => answers.get(IsEstablisherNewId(index)) match {
        case Some(true) =>
          NavigateTo.dontSave(establisherCompanyRoutes.AddCompanyDirectorsController.onPageLoad(mode, srn, index))
        case _ =>
          anyMoreChanges(srn)
      }
    }
  }

  private def confirmPreviousAddressRoutes(index: Int, mode: Mode, srn: SchemeReferenceNumber)(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(CompanyConfirmPreviousAddressId(index)) match {
      case Some(false) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CompanyPreviousAddressPostcodeLookupController.onPageLoad(mode, srn, index))
      case Some(true) =>
        anyMoreChanges(srn)
      case None =>
        NavigateTo.dontSave(SessionExpiredController.onPageLoad)
    }
  }

  private def confirmHasCompanyNumber(index: Int, mode: Mode, srn: SchemeReferenceNumber)(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(HasCompanyCRNId(index)) match {
      case Some(true) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CompanyEnterCRNController.onPageLoad(mode, srn, index))
      case Some(false) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CompanyNoCRNReasonController.onPageLoad(mode, srn, index))
      case None =>
        NavigateTo.dontSave(SessionExpiredController.onPageLoad)
    }
  }

  private def confirmHasCompanyUtr(index: Int, mode: Mode, srn: SchemeReferenceNumber)(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(HasCompanyUTRId(index)) match {
      case Some(true) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CompanyEnterUTRController.onPageLoad(mode, srn, index))
      case Some(false) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CompanyNoUTRReasonController.onPageLoad(mode, srn, index))
      case None =>
        NavigateTo.dontSave(SessionExpiredController.onPageLoad)
    }
  }

  private def confirmHasCompanyVat(index: Int, mode: Mode, srn: SchemeReferenceNumber)(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(HasCompanyVATId(index)) match {
      case Some(true) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CompanyEnterVATController.onPageLoad(mode, index, srn))
      case Some(false) if Seq(NormalMode, UpdateMode).contains(mode) =>
        NavigateTo.dontSave(establisherCompanyRoutes.HasCompanyPAYEController.onPageLoad(mode, srn, index))
      case Some(false) =>
        cyaCompanyDetails(index, journeyMode(mode), srn)
      case None =>
        NavigateTo.dontSave(SessionExpiredController.onPageLoad)
    }
  }

  private def confirmHasCompanyPAYE(index: Int, mode: Mode, srn: SchemeReferenceNumber)(answers: UserAnswers): Option[NavigateTo] = {
    (answers.get(HasCompanyPAYEId(index)), mode) match {
      case (Some(true), _) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CompanyEnterPAYEController.onPageLoad(mode, index, srn))
      case (Some(false), NormalMode) =>
        NavigateTo.dontSave(establisherCompanyRoutes.IsCompanyDormantController.onPageLoad(mode, srn, index))
      case (Some(false), UpdateMode) =>
        cyaCompanyDetails(index, mode, srn)
      case (Some(false), CheckMode | CheckUpdateMode) =>
        exitMiniJourney(index, mode, srn, answers, cyaCompanyDetails)
      case _ =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad)
    }
  }

  private def confirmHasBeenTrading(index: Int, mode: Mode, srn: SchemeReferenceNumber)(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(HasBeenTradingCompanyId(index)) match {
      case Some(true) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CompanyPreviousAddressPostcodeLookupController.onPageLoad(mode, srn, index))
      case Some(false) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CheckYourAnswersCompanyAddressController.onPageLoad(journeyMode(mode), srn, index))
      case None =>
        NavigateTo.dontSave(SessionExpiredController.onPageLoad)
    }
  }

  private def payeRoutes(index: Int, mode: Mode, srn: SchemeReferenceNumber)(answers: UserAnswers): Option[NavigateTo] = {
    (mode, answers.get(IsEstablisherNewId(index))) match {
      case (NormalMode, _) =>
        NavigateTo.dontSave(establisherCompanyRoutes.IsCompanyDormantController.onPageLoad(mode, srn, index))
      case _ =>
        NavigateTo.dontSave(establisherCompanyRoutes.CheckYourAnswersCompanyDetailsController.onPageLoad(mode, srn, index))
    }
  }
}
