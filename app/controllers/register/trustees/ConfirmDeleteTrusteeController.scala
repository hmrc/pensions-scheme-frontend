/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.register.trustees

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import forms.register.trustees.ConfirmDeleteTrusteeFormProvider
import identifiers.register.trustees.ConfirmDeleteTrusteeId
import identifiers.register.trustees.company.CompanyDetailsId
import identifiers.register.trustees.individual.TrusteeNameId
import identifiers.register.trustees.partnership.PartnershipDetailsId
import javax.inject.Inject
import models._
import models.person.PersonName
import models.register.trustees.TrusteeKind
import models.register.trustees.TrusteeKind.{Company, Individual, Partnership}
import models.requests.DataRequest
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.UserAnswers
import utils.annotations.Trustees
import views.html.register.trustees.confirmDeleteTrustee

import scala.concurrent.{ExecutionContext, Future}

class ConfirmDeleteTrusteeController @Inject()(appConfig: FrontendAppConfig,
                                               override val messagesApi: MessagesApi,
                                               authenticate: AuthAction,
                                               getData: DataRetrievalAction,
                                               allowAccess: AllowAccessActionProvider,
                                               requireData: DataRequiredAction,
                                               @Trustees navigator: Navigator,
                                               userAnswersService: UserAnswersService,
                                               formProvider: ConfirmDeleteTrusteeFormProvider,
                                               val controllerComponents: MessagesControllerComponents,
                                               val view: confirmDeleteTrustee
                                              )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController
  with I18nSupport with Retrievals {

  def onPageLoad(mode: Mode, index: Index, trusteeKind: TrusteeKind, srn: Option[String]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        getDeletableTrustee(index, trusteeKind, request.userAnswers) map {
          trustee =>
            if (trustee.isDeleted) {
              Future.successful(Redirect(routes.AlreadyDeletedController.onPageLoad(mode, index, trusteeKind, srn)))
            } else {
              Future.successful(
                Ok(
                  view(form(trustee.name),
                    trustee.name,
                    routes.ConfirmDeleteTrusteeController.onSubmit(mode, index, trusteeKind, srn),
                    existingSchemeName,
                    mode,
                    srn
                  )
                )
              )
            }
        } getOrElse Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad))
    }

  private def getDeletableTrustee(index: Index, trusteeKind: TrusteeKind, userAnswers: UserAnswers)
  : Option[DeletableTrustee] = {
    trusteeKind match {
      case Individual => userAnswers.get(TrusteeNameId(index)).map(details => DeletableTrustee(details.fullName,
        details.isDeleted))
      case Company => userAnswers.get(CompanyDetailsId(index)).map(details => DeletableTrustee(details.companyName,
        details.isDeleted))
      case Partnership => userAnswers.get(PartnershipDetailsId(index)).map(details => DeletableTrustee(details.name,
        details.isDeleted))
    }
  }

  def onSubmit(mode: Mode, index: Index, trusteeKind: TrusteeKind, srn: Option[String]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        trusteeKind match {
          case Company =>
            CompanyDetailsId(index).retrieve.right.map { companyDetails =>
              updateTrusteeKind(companyDetails.companyName, trusteeKind, index, Some(companyDetails), None, None,
                srn, mode)
            }
          case Individual =>
            TrusteeNameId(index).retrieve.right.map { trusteeName =>
              updateTrusteeKind(trusteeName.fullName, trusteeKind, index, None, None, Some(trusteeName), srn, mode)
            }
          case Partnership =>
            PartnershipDetailsId(index).retrieve.right.map { partnershipDetails =>
              updateTrusteeKind(partnershipDetails.name, trusteeKind, index, None, Some(partnershipDetails), None,
                srn, mode)
            }
        }
    }

  private def updateTrusteeKind(name: String,
                                trusteeKind: TrusteeKind,
                                trusteeIndex: Index,
                                companyDetails: Option[CompanyDetails],
                                partnershipDetails: Option[PartnershipDetails],
                                trusteeDetails: Option[PersonName],
                                srn: Option[String],
                                mode: Mode)(implicit dataRequest: DataRequest[AnyContent]): Future[Result] = {
    form(name).bindFromRequest().fold(
      (formWithErrors: Form[_]) =>
        Future.successful(BadRequest(view(
          formWithErrors,
          name,
          routes.ConfirmDeleteTrusteeController.onSubmit(mode, trusteeIndex, trusteeKind, srn),
          existingSchemeName,
          mode,
          srn
        ))),
      value => {
        val deletionResult = if (value) {
          trusteeKind match {
            case Company => companyDetails.fold(Future.successful(dataRequest.userAnswers.json))(
              company => userAnswersService.save(mode, srn, CompanyDetailsId(trusteeIndex), company.copy(isDeleted =
                true)))
            case Individual => trusteeDetails.fold(Future.successful(dataRequest.userAnswers.json))(
              trustee => userAnswersService.save(mode, srn, TrusteeNameId(trusteeIndex), trustee.copy(isDeleted =
                true)))
            case Partnership => partnershipDetails.fold(Future.successful(dataRequest.userAnswers.json))(
              partnership => userAnswersService.save(mode, srn, PartnershipDetailsId(trusteeIndex), partnership.copy
              (isDeleted = true)))
          }
        } else {
          Future.successful(dataRequest.userAnswers.json)
        }
        deletionResult.flatMap { answers =>
          Future.successful(Redirect(navigator.nextPage(ConfirmDeleteTrusteeId, mode, UserAnswers(answers), srn)))
        }
      }
    )
  }

  private def form(name: String)(implicit messages: Messages): Form[Boolean] = formProvider(name)

  private case class DeletableTrustee(name: String, isDeleted: Boolean)

}
