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
import forms.dataPrefill.{DataPrefillCheckboxFormProvider, DataPrefillRadioFormProvider}
import identifiers.SchemeNameId
import identifiers.register.trustees.{DirectorAlsoTrusteeId, DirectorsAlsoTrusteesId}
import models._
import models.prefill.IndividualDetails
import models.requests.DataRequest
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.{DataPrefillService, UserAnswersService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.Trustees
import utils.{Enumerable, UserAnswers}
import views.html.{dataPrefillCheckbox, dataPrefillRadio}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DirectorsAlsoTrusteesController @Inject()(override val messagesApi: MessagesApi,
                                                userAnswersService: UserAnswersService,
                                                @Trustees val navigator: Navigator,
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
                         seqEstablishers: Seq[IndividualDetails],
                         eitherForm: Either[Form[List[Int]], Form[Int]],
                         schemeName: String)(implicit request: DataRequest[AnyContent]): Future[Result] = {
    eitherForm match {
      case Left(form) =>
        val pageHeading = Messages("messages__trustees__prefill__title")
        val titleMessage = Messages("messages__trustees__prefill__heading")
        val options = DataPrefillCheckbox.checkboxes(seqEstablishers)
        val postCall = controllers.register.trustees.routes.DirectorsAlsoTrusteesController.onSubmit


        println( "\nOPTIONS=" + options)

        Future.successful(status(checkBoxView(form, Some(schemeName), pageHeading, titleMessage, options, postCall)))
      case Right(form) =>
        val pageHeading = Messages("messages__trustees__prefill__title")
        val titleMessage = Messages("messages__trustees__prefill__heading")
        val options = DataPrefillRadio.radios(seqEstablishers)
        val postCall = controllers.register.trustees.routes.DirectorsAlsoTrusteesController.onSubmit
        Future.successful(status(radioView(form, Some(schemeName), pageHeading, titleMessage, options, postCall)))
    }
  }

  private def getFormAsEither(seqEstablishers: Seq[IndividualDetails])(implicit request: DataRequest[AnyContent]): Either[Form[List[Int]], Form[Int]] =
    if (seqEstablishers.size > 1) {
      Left(formCheckBox(request.userAnswers, implicitly))
    } else {
      Right(formRadio)
    }

  def onPageLoad: Action[AnyContent] =
    (authenticate() andThen getData(NormalMode, None) andThen allowAccess(None) andThen requireData).async {
      implicit request =>
        SchemeNameId.retrieve.right.map { schemeName =>
          val seqEstablishers: Seq[IndividualDetails] = dataPrefillService.getListOfDirectorsToBeCopied(request.userAnswers)
          println("\n>>>>SSSS= " + seqEstablishers)
          if (seqEstablishers.isEmpty) {
            Future.successful(Redirect(controllers.register.trustees.individual.routes.TrusteeNameController
              .onPageLoad(NormalMode, request.userAnswers.trusteesCount, None)))
          } else {
            renderView(Ok,
              seqEstablishers,
              getFormAsEither(seqEstablishers),
              schemeName
            )
          }
        }
    }

  //scalastyle:off method.length
  def onSubmit: Action[AnyContent] =
    (authenticate() andThen getData(NormalMode, None) andThen allowAccess(None) andThen requireData).async {
      implicit request =>
        val seqEstablishers: Seq[IndividualDetails] = dataPrefillService.getListOfDirectorsToBeCopied(request.userAnswers)
        SchemeNameId.retrieve.right.map { schemeName =>
          if (seqEstablishers.size > 1) {
            println("\n>>>>" + request.request.body)
            val boundForm: Form[List[Int]] = formCheckBox(request.userAnswers, implicitly).bindFromRequest()
            boundForm.value match {
              case Some(value) if boundForm.errors.isEmpty =>
                println("\n>>>>>BBBBBBBBBBBBBBBB=" + value)
                def uaAfterCopy: UserAnswers = (if (value.headOption.getOrElse(-1) < 0) {
                  request.userAnswers
                } else {
                  /*
                  TO DO:
                  1. Change checkboxes method above to generate sequential index no across all establishers not just per establisher. Means will have to change TrusteesAlsoDirectorsController too to work as per 2 and 3 below
                  2. In here use this sequential int (in value) to pull out from seqEstablishers which ones affected
                  3. Change copyAllDirectorsToTrustees below to take on Seq[Tuple2[Int]], where first int is trustee no in establisher and second is establisher no
                   */

                  // TODO: dataPrefillService.copyAllDirectorsToTrustees(request.userAnswers, value)
                  dataPrefillService.copyAllDirectorsToTrustees(request.userAnswers, value, seqEstablishers.headOption.flatMap(_.mainIndex).getOrElse(0))
                }).setOrException(DirectorsAlsoTrusteesId)(value)

                userAnswersService.upsert(NormalMode, None, uaAfterCopy.json).map { _ =>
                  Redirect(navigator.nextPage(DirectorsAlsoTrusteesId, NormalMode, uaAfterCopy, None))
                }
              case _ =>
                renderView(BadRequest,
                  seqEstablishers,
                  Left(boundForm),
                  schemeName
                )
            }
          } else {
            val boundForm: Form[Int] = formRadio.bindFromRequest()
            boundForm.value match {
              case Some(value) if boundForm.errors.isEmpty =>
                def uaAfterCopy: UserAnswers = (if (value < 0) {
                  request.userAnswers
                } else {
                  dataPrefillService.copyAllDirectorsToTrustees(request.userAnswers, Seq(value), seqEstablishers.headOption.flatMap(_.mainIndex).getOrElse(0))
                }).setOrException(DirectorAlsoTrusteeId)(value)

                userAnswersService.upsert(NormalMode, None, uaAfterCopy.json).map { _ =>
                  Redirect(navigator.nextPage(DirectorAlsoTrusteeId, NormalMode, uaAfterCopy, None))
                }
              case _ =>
                renderView(BadRequest,
                  seqEstablishers,
                  Right(boundForm),
                  schemeName
                )
            }
          }
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
