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

package controllers.register.trustees

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import controllers.register.trustees.individual.routes.TrusteeNameController
import controllers.register.trustees.routes.AddTrusteeController
import forms.dataPrefill.{DataPrefillCheckboxFormProvider, DataPrefillRadioFormProvider}
import identifiers.SchemeNameId
import models._
import models.prefill.IndividualDetails
import models.requests.DataRequest
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.DataPrefillService.DirectorIdentifier
import services.{DataPrefillService, UserAnswersService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{Enumerable, UserAnswers}
import views.html.{dataPrefillCheckbox, dataPrefillRadio}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DirectorsAlsoTrusteesController @Inject()(override val messagesApi: MessagesApi,
                                                userAnswersService: UserAnswersService,
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
  with I18nSupport with Retrievals with Enumerable.Implicits {

  private def renderView(status: Status,
                         candidateDirectors: Seq[IndividualDetails],
                         eitherForm: Either[Form[List[Int]], Form[Int]],
                         schemeName: String,
                         index: Int)(implicit request: DataRequest[AnyContent]): Future[Result] = {
    eitherForm match {
      case Left(form) =>
        val pageHeading = Messages("messages__trustees__prefill__title")
        val titleMessage = Messages("messages__trustees__prefill__heading")
        val options = DataPrefillCheckbox.checkboxes(candidateDirectors)
        val postCall = controllers.register.trustees.routes.DirectorsAlsoTrusteesController.onSubmit(index)
        Future.successful(status(checkBoxView(form, Some(schemeName), pageHeading, titleMessage, options, postCall)))
      case Right(form) =>
        val pageHeading = Messages("messages__trustees__prefill__title")
        val titleMessage = Messages("messages__trustees__prefill__heading")
        val options = DataPrefillRadio.radios(candidateDirectors)
        val postCall = controllers.register.trustees.routes.DirectorsAlsoTrusteesController.onSubmit(index)
        Future.successful(status(radioView(form, Some(schemeName), pageHeading, titleMessage, options, postCall)))
    }
  }

  private def getFormAsEither(candidateDirectors: Seq[IndividualDetails])(implicit request: DataRequest[AnyContent]): Either[Form[List[Int]], Form[Int]] =
    if (candidateDirectors.size > 1) {
      Left(formCheckBox(request.userAnswers, implicitly))
    } else {
      Right(formRadio)
    }

  def onPageLoad(index: Index): Action[AnyContent] =
    (authenticate() andThen getData(NormalMode, None) andThen allowAccess(None) andThen requireData).async {
      implicit request =>
        SchemeNameId.retrieve.map { schemeName =>
            val candidateDirectors: Seq[IndividualDetails] = dataPrefillService.getListOfDirectorsToBeCopied(request.userAnswers)
            if (candidateDirectors.isEmpty) {
              Future.successful(Redirect(controllers.register.trustees.individual.routes.TrusteeNameController
                .onPageLoad(NormalMode, index, None)))
            } else {
              renderView(Ok,
                candidateDirectors,
                getFormAsEither(candidateDirectors),
                schemeName,
                index
              )
            }
        }
    }

  private def appendSelectedDirectors(selectedDirectors: List[Int],
                                      candidateDirectors: Seq[IndividualDetails]
                                     )(implicit request: DataRequest[AnyContent]): UserAnswers = {
    val seqDirectorIdentifier: Seq[DirectorIdentifier] = selectedDirectors.flatMap { i =>
      val foundItem = candidateDirectors(i)
      (foundItem.mainIndex, foundItem.index) match {
        case (Some(establisherIndex), directorIndex) => Seq(DirectorIdentifier(establisherIndex, directorIndex))
        case _ => Nil
      }
    }
    dataPrefillService.copySelectedDirectorsToTrustees(request.userAnswers, seqDirectorIdentifier)
  }

  def onSubmit(index: Index): Action[AnyContent] =
    (authenticate() andThen getData(NormalMode, None) andThen allowAccess(None) andThen requireData).async {
      implicit request =>
        val candidateDirectors: Seq[IndividualDetails] = dataPrefillService.getListOfDirectorsToBeCopied(request.userAnswers)
        SchemeNameId.retrieve.map { schemeName =>
          if (candidateDirectors.size > 1) {
            val boundForm: Form[List[Int]] = formCheckBox(request.userAnswers, implicitly).bindFromRequest()
            boundForm.value match {
              case Some(value) if boundForm.errors.isEmpty =>
                def uaAfterCopy: UserAnswers = if (value.headOption.getOrElse(-1) < 0) {
                  request.userAnswers
                } else {
                  appendSelectedDirectors(value, candidateDirectors)
                }
                userAnswersService.upsert(NormalMode, None, uaAfterCopy.json).map { _ =>
                  nav(value.headOption.getOrElse(-1) < 0, uaAfterCopy, index)
                }
              case _ =>
                renderView(BadRequest,
                  candidateDirectors,
                  Left(boundForm),
                  schemeName,
                  index
                )
            }
          } else {
            val boundForm: Form[Int] = formRadio.bindFromRequest()
            boundForm.value match {
              case Some(value) if boundForm.errors.isEmpty =>
                val uaAfterCopy: UserAnswers = if (value < 0) {
                  request.userAnswers
                } else {
                  appendSelectedDirectors(List(0), candidateDirectors)
                }
                userAnswersService.upsert(NormalMode, None, uaAfterCopy.json).map { _ =>
                  nav(value < 0, uaAfterCopy, index)
                }
              case _ =>
                renderView(BadRequest,
                  candidateDirectors,
                  Right(boundForm),
                  schemeName,
                  index
                )
            }
          }
        }
    }

  private def nav(noneSelected:Boolean, uaAfterCopy: UserAnswers, index:Index): Result = {
    if (noneSelected) {
      Redirect(TrusteeNameController.onPageLoad(NormalMode, index, None))
    } else {
      Redirect(AddTrusteeController.onPageLoad(NormalMode, None))
    }
  }

  private def formCheckBox(implicit ua: UserAnswers, messages: Messages): Form[List[Int]] = {
    val existingDirCount = ua.allTrusteesAfterDelete.size
    formProviderCheckBox(
      existingDirCount,
      "messages__trustees__prefill__multi__error__required",
      "messages__trustees__prefill__multi__error__noneWithValue",
      messages("messages__trustees__prefill__multi__error__moreThanTen",
        existingDirCount, config.maxDirectors - existingDirCount)
    )
  }

  private def formRadio: Form[Int] =
    formProviderRadio("messages__trustees__prefill__single__error__required")
}
