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

package controllers.register.establishers

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import forms.register.establishers.ConfirmDeleteEstablisherFormProvider
import identifiers.register.establishers.ConfirmDeleteEstablisherId
import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.establishers.individual.EstablisherNameId
import identifiers.register.establishers.partnership.PartnershipDetailsId
import javax.inject.Inject
import models._
import models.person.{PersonDetails, PersonName}
import models.register.establishers.EstablisherKind
import models.register.establishers.EstablisherKind._
import models.requests.DataRequest
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, Result}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.UserAnswers
import utils.annotations.Establishers
import views.html.register.establishers.confirmDeleteEstablisher

import scala.concurrent.{ExecutionContext, Future}

class ConfirmDeleteEstablisherController @Inject()(
                                                    appConfig: FrontendAppConfig,
                                                    override val messagesApi: MessagesApi,
                                                    val userAnswersService: UserAnswersService,
                                                    @Establishers navigator: Navigator,
                                                    authenticate: AuthAction,
                                                    getData: DataRetrievalAction,
                                                    allowAccess: AllowAccessActionProvider,
                                                    requireData: DataRequiredAction,
                                                    formProvider: ConfirmDeleteEstablisherFormProvider
                                                  )(implicit val ec: ExecutionContext) extends FrontendController with I18nSupport with Retrievals {

  private val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode, index: Index, establisherKind: EstablisherKind, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        getDeletableEstablisher(index, establisherKind, request.userAnswers) map {
          establisher =>
            if (establisher.isDeleted) {
              Future.successful(Redirect(routes.AlreadyDeletedController.onPageLoad(mode, index, establisherKind, srn)))
            } else {
              Future.successful(
                Ok(
                  confirmDeleteEstablisher(
                    appConfig,
                    form,
                    establisher.name,
                    getHintText(establisherKind),
                    routes.ConfirmDeleteEstablisherController.onSubmit(mode, index, establisherKind, srn),
                    existingSchemeName,
                    srn
                  )
                )
              )
            }
        } getOrElse Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
    }

  private def getHintText(establisherKind: EstablisherKind): Option[String] = {
    establisherKind match {
      case EstablisherKind.Company =>
        Some(Messages(s"messages__confirmDeleteEstablisher__companyHint"))
      case EstablisherKind.Partnership =>
        Some(Messages(s"messages__confirmDeleteEstablisher__partnershipHint"))
      case _ => None
    }
  }

  private def getDeletableEstablisher(index: Index, establisherKind: EstablisherKind, userAnswers: UserAnswers): Option[DeletableEstablisher] = {
    establisherKind match {
      case Indivdual => userAnswers.get(EstablisherNameId(index)).map(details => DeletableEstablisher(details.fullName, details.isDeleted))
      case Company => userAnswers.get(CompanyDetailsId(index)).map(details => DeletableEstablisher(details.companyName, details.isDeleted))
      case Partnership => userAnswers.get(PartnershipDetailsId(index)).map(details => DeletableEstablisher(details.name, details.isDeleted))
    }
  }

  def onSubmit(mode: Mode, establisherIndex: Index, establisherKind: EstablisherKind, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async {
      implicit request =>

        establisherKind match {
          case Company =>
            CompanyDetailsId(establisherIndex).retrieve.right.map { companyDetails =>
              updateEstablisherKind(companyDetails.companyName, establisherKind, establisherIndex,
                Some(companyDetails), None, None, None, mode, srn)
            }
          case Indivdual =>
            EstablisherNameId(establisherIndex).retrieve.right.map { trusteeDetails =>
              updateEstablisherKind(trusteeDetails.fullName, establisherKind, establisherIndex,
                None, None, Some(trusteeDetails), None, mode, srn)
            }
          case Partnership =>
            PartnershipDetailsId(establisherIndex).retrieve.right.map { partnershipDetails =>
              updateEstablisherKind(partnershipDetails.name, establisherKind, establisherIndex,
                None, None, None, Some(partnershipDetails), mode, srn)
            }
        }
    }

  private def updateEstablisherKind(name: String,
                                    establisherKind: EstablisherKind,
                                    establisherIndex: Index,
                                    companyDetails: Option[CompanyDetails],
                                    establisherDetails: Option[PersonDetails],
                                    establisherName: Option[PersonName],
                                    partnershipDetails: Option[PartnershipDetails],
                                    mode: Mode,
                                    srn: Option[String])(implicit dataRequest: DataRequest[AnyContent]): Future[Result] = {
    form.bindFromRequest().fold(
      (formWithErrors: Form[_]) =>
        Future.successful(BadRequest(confirmDeleteEstablisher(
          appConfig,
          formWithErrors,
          name,
          getHintText(establisherKind),
          routes.ConfirmDeleteEstablisherController.onSubmit(mode, establisherIndex, establisherKind, srn),
          existingSchemeName,
          srn
        ))),
      value => {
        val deletionResult = if (value) {
          establisherKind match {
            case Company => companyDetails.fold(Future.successful(dataRequest.userAnswers.json))(
              company => userAnswersService.save(mode, srn, CompanyDetailsId(establisherIndex), company.copy(isDeleted = true)))
            case Indivdual => establisherName.fold(Future.successful(dataRequest.userAnswers.json))(
              individual => userAnswersService.save(mode, srn, EstablisherNameId(establisherIndex), individual.copy(isDeleted = true)))
            case Partnership => partnershipDetails.fold(Future.successful(dataRequest.userAnswers.json))(
              partnership => userAnswersService.save(mode, srn, PartnershipDetailsId(establisherIndex), partnership.copy(isDeleted = true)))
          }
        } else {
          Future.successful(dataRequest.userAnswers.json)
        }
        deletionResult.flatMap { answers =>
          Future.successful(Redirect(navigator.nextPage(ConfirmDeleteEstablisherId, mode, UserAnswers(answers), srn)))
        }
      }
    )
  }

  private case class DeletableEstablisher(name: String, isDeleted: Boolean)

}
