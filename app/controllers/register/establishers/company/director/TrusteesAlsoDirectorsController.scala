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

package controllers.register.establishers.company.director

import controllers.Retrievals
import controllers.actions._
import forms.dataPrefill.{DataPrefillCheckboxFormProvider, DataPrefillRadioFormProvider}
import identifiers.SchemeNameId
import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.establishers.company.director.{TrusteeAlsoDirectorId, TrusteesAlsoDirectorsId}
import models._
import models.prefill.IndividualDetails
import models.requests.DataRequest
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
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
                                                val radioView: dataPrefillRadio
                                               )(implicit val executionContext: ExecutionContext) extends FrontendBaseController
  with I18nSupport with Retrievals with Enumerable.Implicits {


  private def renderView(status: Status,
                         seqTrustee: Seq[IndividualDetails],
                         formCheckBox: => Form[List[Int]],
                         formRadio: => Form[Int],
                         establisherIndex: Int,
                         companyName: CompanyDetails,
                         schemeName: String)(implicit request: DataRequest[AnyContent]): Future[Result] = {
    if (seqTrustee.size > 1) {
      val pageHeading = Messages("messages__directors__prefill__title")
      val titleMessage = Messages("messages__directors__prefill__heading", companyName.companyName)
      val options = DataPrefillCheckbox.checkboxes(seqTrustee)
      val postCall = controllers.register.establishers.company.director.routes.TrusteesAlsoDirectorsController.onSubmit(establisherIndex)
      Future.successful(status(checkBoxView(formCheckBox, Some(schemeName), pageHeading, titleMessage, options, postCall)))
    } else {
      val pageHeading = Messages("messages__directors__prefill__title")
      val titleMessage = Messages("messages__directors__prefill__heading", companyName.companyName)
      val options = DataPrefillRadio.radios(seqTrustee)
      val postCall = controllers.register.establishers.company.director.routes.TrusteesAlsoDirectorsController.onSubmit(establisherIndex)
      Future.successful(status(radioView(formRadio, Some(schemeName), pageHeading, titleMessage, options, postCall)))
    }
  }

  def onPageLoad(establisherIndex: Index): Action[AnyContent] =
    (authenticate() andThen getData(NormalMode, None) andThen allowAccess(None) andThen requireData).async {
      implicit request =>
        (CompanyDetailsId(establisherIndex) and SchemeNameId).retrieve.right.map { case companyName ~ schemeName =>
          val seqTrustee: Seq[IndividualDetails] = dataPrefillService.getListOfTrusteesToBeCopied(establisherIndex)(request.userAnswers)
          renderView(Ok,
            seqTrustee,
            formCheckBox(establisherIndex)(request.userAnswers, implicitly),
            formRadio(establisherIndex)(request.userAnswers, implicitly),
            establisherIndex,
            companyName,
            schemeName
          )
        }
    }

  //scalastyle:off method.length
  def onSubmit(establisherIndex: Index): Action[AnyContent] =
    (authenticate() andThen getData(NormalMode, None) andThen allowAccess(None) andThen requireData).async {
      implicit request =>
        val seqTrustee: Seq[IndividualDetails] = dataPrefillService.getListOfTrusteesToBeCopied(establisherIndex)(request.userAnswers)
        (CompanyDetailsId(establisherIndex) and SchemeNameId).retrieve.right.map { case companyName ~ schemeName =>
          if (seqTrustee.size > 1) {
            val boundForm: Form[List[Int]] = formCheckBox(establisherIndex)(request.userAnswers, implicitly).bindFromRequest()
            boundForm.value match {
              case Some(value) if boundForm.errors.isEmpty =>
                def uaAfterCopy: UserAnswers = (if (value.headOption.getOrElse(-1) < 0) {
                  request.userAnswers
                } else {
                  dataPrefillService.copyAllTrusteesToDirectors(request.userAnswers, value, establisherIndex)
                }).setOrException(TrusteesAlsoDirectorsId(establisherIndex))(value)
                userAnswersService.upsert(NormalMode, None, uaAfterCopy.json).map { _ =>
                  Redirect(navigator.nextPage(TrusteesAlsoDirectorsId(establisherIndex), NormalMode, uaAfterCopy, None))
                }
              case _ =>
                renderView(BadRequest,
                  seqTrustee,
                  boundForm,
                  formRadio(establisherIndex)(request.userAnswers, implicitly),
                  establisherIndex,
                  companyName,
                  schemeName
                )
            }
          } else {
            val boundForm = formRadio(establisherIndex)(request.userAnswers, implicitly).bindFromRequest()
            boundForm.value match {
              case Some(value) if boundForm.errors.isEmpty =>
                def uaAfterCopy: UserAnswers = (if (value < 0) {
                  request.userAnswers
                } else {
                  dataPrefillService.copyAllTrusteesToDirectors(request.userAnswers, Seq(value), establisherIndex)
                }).setOrException(TrusteeAlsoDirectorId(establisherIndex))(value)
                userAnswersService.upsert(NormalMode, None, uaAfterCopy.json).map { _ =>
                  Redirect(navigator.nextPage(TrusteeAlsoDirectorId(establisherIndex), NormalMode, uaAfterCopy, None))
                }
              case _ =>
                renderView(BadRequest,
                  seqTrustee,
                  formCheckBox(establisherIndex)(request.userAnswers, implicitly),
                  boundForm,
                  establisherIndex,
                  companyName,
                  schemeName
                )
            }
          }
        }
    }

  private def formCheckBox(index: Index)(implicit ua: UserAnswers, messages: Messages): Form[List[Int]] = {
    val existingDirCount = ua.allDirectorsAfterDelete(index).size
    formProviderCheckBox(
      existingDirCount,
      "messages__directors__prefill__multi__error__required",
      "messages__directors__prefill__multi__error__noneWithValue",
      messages("messages__directors__prefill__multi__error__moreThanTen",
        existingDirCount, /*config.maxDirectors*/ 10 - existingDirCount)
    )
  }

  private def formRadio(index: Index)(implicit ua: UserAnswers, messages: Messages): Form[Int] =
    formProviderRadio("messages__directors__prefill__single__error__required")
}
