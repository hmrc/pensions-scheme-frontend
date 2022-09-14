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
import forms.dataPrefill.DataPrefillRadioFormProvider
import identifiers.SchemeNameId
import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.establishers.company.director.TrusteeAlsoDirectorId
import models.{DataPrefillRadio, Index, NormalMode}
import navigators.CompoundNavigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{DataPrefillService, UserAnswersService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{Enumerable, UserAnswers}
import views.html.dataPrefillRadio

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TrusteeAlsoDirectorController @Inject()(override val messagesApi: MessagesApi,
                                              userAnswersService: UserAnswersService,
                                              navigator: CompoundNavigator,
                                              authenticate: AuthAction,
                                              getData: DataRetrievalAction,
                                              allowAccess: AllowAccessActionProvider,
                                              requireData: DataRequiredAction,
                                              formProvider: DataPrefillRadioFormProvider,
                                              dataPrefillService: DataPrefillService,
                                              val controllerComponents: MessagesControllerComponents,
                                              val view: dataPrefillRadio
                                             )(implicit val executionContext: ExecutionContext) extends FrontendBaseController
  with I18nSupport with Retrievals with Enumerable.Implicits {

  def onPageLoad(establisherIndex: Index): Action[AnyContent] =
    (authenticate() andThen getData(NormalMode, None) andThen allowAccess(None) andThen requireData).async {
      implicit request =>
        println("\nMMM")
        (CompanyDetailsId(establisherIndex) and SchemeNameId).retrieve.right.map { case companyName ~ schemeName =>
          implicit val ua: UserAnswers = request.userAnswers
          val seqTrustee = dataPrefillService.getListOfTrusteesToBeCopied(establisherIndex)
          // Returning empty list!!!!
          if (seqTrustee.nonEmpty) {
            val pageHeading = Messages("messages__directors__prefill__title")
            val titleMessage = Messages("messages__directors__prefill__heading", companyName.companyName)
            val options = DataPrefillRadio.radios(seqTrustee)
            val postCall = controllers.register.establishers.company.director.routes.TrusteeAlsoDirectorController.onSubmit(establisherIndex)
            Future.successful(BadRequest(view(form, Some(schemeName), pageHeading, titleMessage, options, postCall)))
          } else {
            Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad))
          }
        }
    }

  def onSubmit(establisherIndex: Index): Action[AnyContent] =
    (authenticate() andThen getData(NormalMode, None) andThen allowAccess(None) andThen requireData).async {
      implicit request =>
        implicit val ua: UserAnswers = request.userAnswers
        (CompanyDetailsId(establisherIndex) and SchemeNameId).retrieve.right.map { case companyName ~ schemeName =>
          val seqTrustee = dataPrefillService.getListOfTrusteesToBeCopied(establisherIndex)
          form.bindFromRequest().fold(
            (formWithErrors: Form[_]) => {
              val pageHeading = Messages.apply("messages__directors__prefill__title")
              val titleMessage = Messages("messages__directors__prefill__heading", companyName.companyName)
              val options = DataPrefillRadio.radios(seqTrustee)
              val postCall = controllers.register.establishers.company.director.routes.TrusteeAlsoDirectorController.onSubmit(establisherIndex)
              Future.successful(BadRequest(view(formWithErrors, Some(schemeName), pageHeading, titleMessage, options, postCall)))
            },
            value => {
              def uaAfterCopy: UserAnswers = (if (value < 0) {
                ua
              } else {
                dataPrefillService.copyAllTrusteesToDirectors(ua, Seq(value), establisherIndex)
              }).setOrException(TrusteeAlsoDirectorId(establisherIndex))(value)

              userAnswersService.upsert(NormalMode, None, uaAfterCopy.json).map { _ =>
                Redirect(navigator.nextPage(TrusteeAlsoDirectorId(establisherIndex), NormalMode, uaAfterCopy, None))
              }
            }
          )
        }
    }

  private def form: Form[Int] =
    formProvider("messages__directors__prefill__single__error__required")
}
