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

package controllers.register.trustees

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import forms.register.trustees.ConfirmDeleteTrusteeFormProvider
import identifiers.register.trustees.ConfirmDeleteTrusteeId
import identifiers.register.trustees.company.CompanyDetailsId
import identifiers.register.trustees.individual.TrusteeDetailsId
import identifiers.register.trustees.partnership.PartnershipDetailsId
import javax.inject.Inject
import models._
import models.person.PersonDetails
import models.register.trustees.TrusteeKind
import models.register.trustees.TrusteeKind.{Company, Individual, Partnership}
import models.requests.DataRequest
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Result}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.Trustees
import utils.{Navigator, UserAnswers}
import views.html.register.trustees.confirmDeleteTrustee

import scala.concurrent.{ExecutionContext, Future}

class ConfirmDeleteTrusteeController @Inject()(appConfig: FrontendAppConfig,
                                               override val messagesApi: MessagesApi,
                                               authenticate: AuthAction,
                                               getData: DataRetrievalAction,
                                               requireData: DataRequiredAction,
                                               @Trustees navigator: Navigator,
                                               userAnswersService: UserAnswersService,
                                               formProvider: ConfirmDeleteTrusteeFormProvider)(implicit val ec: ExecutionContext)
  extends FrontendController with I18nSupport with Retrievals {

  private val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode, index: Index, trusteeKind: TrusteeKind, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData andThen requireData).async {
    implicit request =>
      getDeletableTrustee(index, trusteeKind, request.userAnswers) map {
        trustee =>
          if (trustee.isDeleted) {
            Future.successful(Redirect(routes.AlreadyDeletedController.onPageLoad(mode, index, trusteeKind, srn)))
          } else {
            Future.successful(
              Ok(
                confirmDeleteTrustee(
                  appConfig,
                  form,
                  trustee.name,
                  routes.ConfirmDeleteTrusteeController.onSubmit(mode, index, trusteeKind, srn),
                  existingSchemeName
                )
              )
            )
          }
      } getOrElse Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
  }

  def onSubmit(mode: Mode, index: Index, trusteeKind: TrusteeKind, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData andThen requireData).async {
    implicit request =>
      trusteeKind match {
        case Company =>
          CompanyDetailsId(index).retrieve.right.map { companyDetails =>
            updateTrusteeKind(companyDetails.companyName, trusteeKind, index, Some(companyDetails), None, None, srn, mode)
          }
        case Individual =>
          TrusteeDetailsId(index).retrieve.right.map { trusteeDetails =>
            updateTrusteeKind(trusteeDetails.fullName, trusteeKind, index, None, Some(trusteeDetails), None, srn, mode)
          }
        case Partnership =>
          PartnershipDetailsId(index).retrieve.right.map { partnershipDetails =>
            updateTrusteeKind(partnershipDetails.name, trusteeKind, index, None, None, Some(partnershipDetails), srn, mode)
          }
        case _ =>
          Left(Future.successful(SeeOther(controllers.routes.SessionExpiredController.onPageLoad().url)))
      }
  }

  private def updateTrusteeKind(name: String,
                                trusteeKind: TrusteeKind,
                                trusteeIndex: Index,
                                companyDetails: Option[CompanyDetails],
                                trusteeDetails: Option[PersonDetails],
                                partnershipDetails: Option[PartnershipDetails],
                                srn: Option[String],
                                mode: Mode)(implicit dataRequest: DataRequest[AnyContent]): Future[Result] = {
    form.bindFromRequest().fold(
      (formWithErrors: Form[_]) =>
        Future.successful(BadRequest(confirmDeleteTrustee(
          appConfig,
          formWithErrors,
          name,
          routes.ConfirmDeleteTrusteeController.onSubmit(mode, trusteeIndex, trusteeKind, srn),
          existingSchemeName
        ))),
      value => {
        val deletionResult = if (value) {
          trusteeKind match {
            case Company => companyDetails.fold(Future.successful(dataRequest.userAnswers.json))(
              company => userAnswersService.save(mode, srn, CompanyDetailsId(trusteeIndex), company.copy(isDeleted = true)))
            case Individual => trusteeDetails.fold(Future.successful(dataRequest.userAnswers.json))(
              trustee => userAnswersService.save(mode, srn, TrusteeDetailsId(trusteeIndex), trustee.copy(isDeleted = true)))
            case Partnership => partnershipDetails.fold(Future.successful(dataRequest.userAnswers.json))(
              partnership => userAnswersService.save(mode, srn, PartnershipDetailsId(trusteeIndex), partnership.copy(isDeleted = true)))
          }
        } else {
          Future.successful(dataRequest.userAnswers.json)
        }
        deletionResult.flatMap { answers =>
          Future.successful(Redirect(navigator.nextPage(ConfirmDeleteTrusteeId, NormalMode, UserAnswers(answers))))
        }
      }
    )
  }

  private case class DeletableTrustee(name: String, isDeleted: Boolean)

  private def getDeletableTrustee(index: Index, trusteeKind: TrusteeKind, userAnswers: UserAnswers): Option[DeletableTrustee] = {
    trusteeKind match {
      case Individual => userAnswers.get(TrusteeDetailsId(index)).map(details => DeletableTrustee(details.fullName, details.isDeleted))
      case Company => userAnswers.get(CompanyDetailsId(index)).map(details => DeletableTrustee(details.companyName, details.isDeleted))
      case Partnership => userAnswers.get(PartnershipDetailsId(index)).map(details => DeletableTrustee(details.name, details.isDeleted))
      case _ => None
    }
  }
}
