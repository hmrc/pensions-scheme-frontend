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
import controllers.actions.*
import forms.dataPrefill.{DataPrefillCheckboxFormProvider, DataPrefillRadioFormProvider}
import models.*
import models.prefill.IndividualDetails
import models.requests.DataRequest
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.*
import play.twirl.api.Html
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
                                               )(implicit val executionContext: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport
    with Enumerable.Implicits {

  private def renderView(
                          candidateDirectors: Seq[IndividualDetails],
                          eitherForm: Either[Form[List[Int]], Form[Int]],
                          index: Int,
                          mode: Mode,
                          srn: OptionalSchemeReferenceNumber
                        )(implicit request: DataRequest[AnyContent]): Html =
    eitherForm match {
      case Left(formListInt) =>
        checkBoxView(
          formListInt,
          Messages("messages__trustees__prefill__title"),
          Messages("messages__trustees__prefill__heading"),
          DataPrefillCheckboxOptions(candidateDirectors),
          controllers.register.trustees.routes.DirectorsAlsoTrusteesController.onSubmit(index, mode, srn)
        )
      case Right(formInt) =>
        radioView(
          formInt,
          Messages("messages__trustees__prefill__title"),
          Messages("messages__trustees__prefill__heading"),
          DataPrefillRadioOptions(candidateDirectors),
          controllers.register.trustees.routes.DirectorsAlsoTrusteesController.onSubmit(index, mode, srn)
        )
    }

  private def getFormAsEither(candidateDirectors: Seq[IndividualDetails])
                             (implicit request: DataRequest[AnyContent]): Either[Form[List[Int]], Form[Int]] =
    if (candidateDirectors.size > 1) {
      Left(formCheckBox(request.userAnswers, implicitly))
    } else {
      Right(formRadio)
    }

  def onPageLoad(index: Index, mode: Mode, srn: OptionalSchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        val candidateDirectors: Seq[IndividualDetails] =
          dataPrefillService.getListOfDirectorsToBeCopied(request.userAnswers)
        
        if (candidateDirectors.isEmpty) {
          Future.successful(Redirect(controllers.register.trustees.individual.routes.TrusteeNameController.onPageLoad(mode, index, srn)))
        } else {
          Future.successful(Ok(renderView(
            candidateDirectors,
            getFormAsEither(candidateDirectors),
            index,
            mode,
            srn
          )))
        }
    }

  private def appendSelectedDirectors(selectedDirectors: List[Int], candidateDirectors: Seq[IndividualDetails])
                                     (implicit request: DataRequest[AnyContent]): UserAnswers = {
    val seqDirectorIdentifier: Seq[DirectorIdentifier] =
      selectedDirectors.flatMap { int =>
        candidateDirectors(int).mainIndex match {
          case Some(establisherIndex) =>
            Some(DirectorIdentifier(establisherIndex, candidateDirectors(int).index))
          case _ =>
            None
        }
      }
    
    dataPrefillService.copySelectedDirectorsToTrustees(
      ua         = request.userAnswers,
      seqIndexes = seqDirectorIdentifier
    )
  }

  def onSubmit(index: Index, mode: Mode, srn: OptionalSchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        val candidateDirectors: Seq[IndividualDetails] =
          dataPrefillService.getListOfDirectorsToBeCopied(request.userAnswers)
        
        if (candidateDirectors.size > 1) {
          val boundForm: Form[List[Int]] =
            formCheckBox(request.userAnswers, implicitly).bindFromRequest()
          
          boundForm.value match {
            case Some(value) if boundForm.errors.isEmpty =>
              def uaAfterCopy: UserAnswers =
                if (value.headOption.getOrElse(-1) < 0) {
                  request.userAnswers
                } else {
                  appendSelectedDirectors(value, candidateDirectors)
                }

              userAnswersService.upsert(mode, srn, uaAfterCopy.json).map { _ =>
                Redirect(nav(value.headOption.getOrElse(-1) < 0, index, mode, srn))
              }
            case _ =>
              Future.successful(BadRequest(renderView(
                candidateDirectors,
                Left(boundForm),
                index,
                mode,
                srn
              )))
          }
        } else {
          val boundForm: Form[Int] =
            formRadio.bindFromRequest()
          
          boundForm.value match {
            case Some(value) if boundForm.errors.isEmpty =>
              val uaAfterCopy: UserAnswers =
                if (value < 0) {
                  request.userAnswers
                } else {
                  appendSelectedDirectors(List(0), candidateDirectors)
                }
              
              userAnswersService.upsert(mode, srn, uaAfterCopy.json).map { _ =>
                Redirect(nav(value < 0, index, mode, srn))
              }
            case _ =>
              Future.successful(BadRequest(renderView(
                candidateDirectors,
                Right(boundForm),
                index,
                mode,
                srn
              )))
          }
        }
    }

  private def nav(noneSelected: Boolean, index: Index, mode: Mode, srn: OptionalSchemeReferenceNumber): Call =
    if (noneSelected) {
      controllers.register.trustees.individual.routes.TrusteeNameController.onPageLoad(mode, index, srn)
    } else {
      controllers.register.trustees.routes.AddTrusteeController.onPageLoad(mode, srn)
    }

  private def formCheckBox(implicit ua: UserAnswers, messages: Messages): Form[List[Int]] = {
    val existingDirCount = ua.allTrusteesAfterDelete.size
    formProviderCheckBox(
      existingDirCount,
      "messages__trustees__prefill__multi__error__required",
      "messages__trustees__prefill__multi__error__noneWithValue",
      messages(
        "messages__trustees__prefill__multi__error__moreThanTen",
        existingDirCount,
        config.maxDirectors - existingDirCount
      )
    )
  }

  private def formRadio: Form[Int] =
    formProviderRadio("messages__trustees__prefill__single__error__required")
}
