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

package controllers.register.establishers.company.director

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions.*
import forms.dataPrefill.{DataPrefillCheckboxFormProvider, DataPrefillRadioFormProvider}
import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.establishers.company.director.{TrusteeAlsoDirectorId, TrusteesAlsoDirectorsId}
import models.*
import models.prefill.IndividualDetails
import models.requests.DataRequest
import navigators.Navigator
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.Results.Redirect
import play.api.mvc.*
import services.{DataPrefillService, UserAnswersService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.EstablishersCompany
import utils.{Enumerable, UserAnswers}
import views.html.{dataPrefillCheckbox, dataPrefillRadio}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TrusteesAlsoDirectorsController @Inject()(override val messagesApi: MessagesApi,
                                                userAnswersService: UserAnswersService,
                                                @EstablishersCompany val navigator: Navigator,
                                                authenticate: AuthAction,
                                                getData: DataRetrievalAction,
                                                allowAccess: AllowAccessActionProvider,
                                                requireData: DataRequiredAction,
                                                formProviderCheckBox: DataPrefillCheckboxFormProvider,
                                                formProviderRadio: DataPrefillRadioFormProvider,
                                                dataPrefillService: DataPrefillService,
                                                val controllerComponents: MessagesControllerComponents,
                                                val checkBoxView: dataPrefillCheckbox,
                                                val radioView: dataPrefillRadio,
                                                config: FrontendAppConfig
                                               )(implicit val executionContext: ExecutionContext) extends FrontendBaseController
  with I18nSupport with Retrievals with Enumerable.Implicits with Logging {
  
  private def renderView(
                          status: Status,
                          seqTrustee: Seq[IndividualDetails],
                          eitherForm: Either[Form[List[Int]], Form[Int]],
                          establisherIndex: Int,
                          companyName: CompanyDetails
                        )(implicit request: DataRequest[AnyContent]): Future[Result] = {
    eitherForm match {
      case Left(form) =>
        val pageHeading = Messages("messages__directors__prefill__title")
        val titleMessage = Messages("messages__directors__prefill__heading", companyName.companyName)
        val options = DataPrefillCheckboxOptions(seqTrustee)
        val postCall = controllers.register.establishers.company.director.routes.TrusteesAlsoDirectorsController.onSubmit(establisherIndex)
        Future.successful(status(checkBoxView(form, pageHeading, titleMessage, options, postCall)))
      case Right(form) =>
        val pageHeading = Messages("messages__directors__prefill__title")
        val titleMessage = Messages("messages__directors__prefill__heading", companyName.companyName)
        val options = DataPrefillRadioOptions(seqTrustee)
        val postCall = controllers.register.establishers.company.director.routes.TrusteesAlsoDirectorsController.onSubmit(establisherIndex)
        Future.successful(status(radioView(form, pageHeading, titleMessage, options, postCall)))
    }
  }

  private def getFormAsEither(seqTrustee: Seq[IndividualDetails],
                              establisherIndex: Int)(implicit request: DataRequest[AnyContent]): Either[Form[List[Int]], Form[Int]] =
    if (seqTrustee.size > 1) {
      Left(formCheckBox(establisherIndex)(request.userAnswers, implicitly))
    } else {
      Right(formRadio)
    }

  def onPageLoad(mode: Mode, srn: OptionalSchemeReferenceNumber, establisherIndex: Index): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(EmptyOptionalSchemeReferenceNumber) andThen requireData).async {
      implicit request =>
        CompanyDetailsId(establisherIndex).retrieve.map { companyName =>
          logger.info(s"onPageLoad - successfully retrieved CompanyDetailsId($establisherIndex) sessionId: ${hc.sessionId.getOrElse("No session Id")}")
          val seqTrustee: Seq[IndividualDetails] =
            dataPrefillService.getListOfTrusteesToBeCopied(establisherIndex)(request.userAnswers)
          
          if (seqTrustee.isEmpty) {
            Future.successful(Redirect(controllers.register.establishers.company.director.routes.DirectorNameController
              .onPageLoad(mode, establisherIndex, request.userAnswers.allDirectors(establisherIndex).size, srn)))
          } else {
            renderView(
              status           = Ok,
              seqTrustee       = seqTrustee,
              eitherForm       = getFormAsEither(seqTrustee, establisherIndex),
              establisherIndex = establisherIndex,
              companyName      = companyName
            )
          }
        }
    }

  //scalastyle:off method.length
  def onSubmit(establisherIndex: Index): Action[AnyContent] =
    (authenticate() andThen getData(NormalMode, EmptyOptionalSchemeReferenceNumber) andThen allowAccess(EmptyOptionalSchemeReferenceNumber) andThen requireData).async {
      implicit request =>
        val seqTrustee: Seq[IndividualDetails] = dataPrefillService.getListOfTrusteesToBeCopied(establisherIndex)(request.userAnswers)

        request.userAnswers.get(CompanyDetailsId(establisherIndex)) match {
          case Some(companyName) =>
            logger.info(
              s"onSubmit - successfully retrieved CompanyDetailsId($establisherIndex) " +
              s"sessionId: ${hc.sessionId.getOrElse("No session Id")}" +
              s"seqTrustee.size = ${seqTrustee.size}"
            )
            if (seqTrustee.size > 1) {
              val boundForm: Form[List[Int]] = formCheckBox(establisherIndex)(request.userAnswers, implicitly).bindFromRequest()
              boundForm.value match {
                case Some(value) if boundForm.errors.isEmpty =>
                  def uaAfterCopy: UserAnswers = (if (value.headOption.getOrElse(-1) < 0) {
                    request.userAnswers
                  } else {
                    dataPrefillService.copyAllTrusteesToDirectors(request.userAnswers, value, establisherIndex)
                  }).setOrException(TrusteesAlsoDirectorsId(establisherIndex))(value)

                  userAnswersService.upsert(NormalMode, EmptyOptionalSchemeReferenceNumber, uaAfterCopy.json).map { _ =>
                    Redirect(navigator.nextPage(TrusteesAlsoDirectorsId(establisherIndex), NormalMode, uaAfterCopy, EmptyOptionalSchemeReferenceNumber))
                  }
                case _ =>
                  renderView(
                    BadRequest,
                    seqTrustee,
                    Left(boundForm),
                    establisherIndex,
                    companyName
                  )
              }
            } else {
              val boundForm: Form[Int] = formRadio.bindFromRequest()
              boundForm.value match {
                case Some(value) if boundForm.errors.isEmpty =>
                  def uaAfterCopy: UserAnswers = (if (value < 0) {
                    request.userAnswers
                  } else {
                    dataPrefillService.copyAllTrusteesToDirectors(request.userAnswers, Seq(value), establisherIndex)
                  }).setOrException(TrusteeAlsoDirectorId(establisherIndex))(value)

                  userAnswersService.upsert(NormalMode, EmptyOptionalSchemeReferenceNumber, uaAfterCopy.json).map { _ =>
                    Redirect(navigator.nextPage(TrusteeAlsoDirectorId(establisherIndex), NormalMode, uaAfterCopy, EmptyOptionalSchemeReferenceNumber))
                  }
                case _ =>
                  renderView(
                    BadRequest,
                    seqTrustee,
                    Right(boundForm),
                    establisherIndex,
                    companyName
                  )
              }
            }
          case _ =>
            logger.info(s"onSubmit - failed to retrieve CompanyDetailsId($establisherIndex) sessionId: ${hc.sessionId.getOrElse("No session Id")}")
            Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad))
        }

    }

  private def formCheckBox(index: Index)(implicit ua: UserAnswers, messages: Messages): Form[List[Int]] = {
    val existingDirCount = ua.allDirectorsAfterDelete(index).size
    formProviderCheckBox(
      existingDirCount,
      "messages__directors__prefill__multi__error__required",
      "messages__directors__prefill__multi__error__noneWithValue",
      messages("messages__directors__prefill__multi__error__moreThanTen",
        existingDirCount, config.maxDirectors - existingDirCount)
    )
  }
  
  def formRadio: Form[Int] =
    formProviderRadio("messages__directors__prefill__single__error__required")
}
