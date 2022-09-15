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
import forms.dataPrefill.DataPrefillCheckboxFormProvider
import identifiers.SchemeNameId
import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.establishers.company.director.TrusteesAlsoDirectorsId
import models.{DataPrefillCheckbox, DataPrefillRadio, Index, NormalMode}
import navigators.CompoundNavigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{DataPrefillService, UserAnswersService}
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{Enumerable, UserAnswers}
import views.html.{dataPrefillCheckbox, dataPrefillRadio}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TrusteesAlsoDirectorsController @Inject()(override val messagesApi: MessagesApi,
                                              userAnswersService: UserAnswersService,
                                              navigator: CompoundNavigator,
                                              authenticate: AuthAction,
                                              getData: DataRetrievalAction,
                                              allowAccess: AllowAccessActionProvider,
                                              requireData: DataRequiredAction,
                                              formProvider: DataPrefillCheckboxFormProvider,
                                              dataPrefillService: DataPrefillService,
                                              val controllerComponents: MessagesControllerComponents,
                                              val view: dataPrefillCheckbox
                                             )(implicit val executionContext: ExecutionContext) extends FrontendBaseController
  with I18nSupport with Retrievals with Enumerable.Implicits {

  def onPageLoad(establisherIndex: Index): Action[AnyContent] =
    (authenticate() andThen getData(NormalMode, None) andThen allowAccess(None) andThen requireData).async {
      implicit request =>

        (CompanyDetailsId(establisherIndex) and SchemeNameId).retrieve.right.map { case companyName ~ schemeName =>
          implicit val ua: UserAnswers = request.userAnswers
          val seqTrustee = dataPrefillService.getListOfTrusteesToBeCopied(establisherIndex)
          println("\nseq of trustees=" + seqTrustee)
          if (seqTrustee.nonEmpty) {
            val pageHeading = Messages("messages__directors__prefill__title")
            val titleMessage = Messages("messages__directors__prefill__heading", companyName.companyName)
            val options = DataPrefillCheckbox.checkboxes(seqTrustee)
            val postCall = controllers.register.establishers.company.director.routes.TrusteesAlsoDirectorsController.onSubmit(establisherIndex)
            Future.successful(BadRequest(view(form(establisherIndex), Some(schemeName), pageHeading, titleMessage, options, postCall)))
          } else {
            Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad))
          }
        }
    }

  def onSubmit(establisherIndex: Index): Action[AnyContent] =
    (authenticate() andThen getData(NormalMode, None) andThen allowAccess(None) andThen requireData).async {
      implicit request =>
        println("\n>R:" + request.body)
        implicit val ua: UserAnswers = request.userAnswers
        (CompanyDetailsId(establisherIndex) and SchemeNameId).retrieve.right.map { case companyName ~ schemeName =>
          form(establisherIndex).bindFromRequest().fold(
            (formWithErrors: Form[_]) => {
              println("\nERR:" + formWithErrors)
              val seqTrustee = dataPrefillService.getListOfTrusteesToBeCopied(establisherIndex)
              val pageHeading = Messages.apply("messages__directors__prefill__title")
              val titleMessage = Messages("messages__directors__prefill__heading", companyName.companyName)
              val options = DataPrefillCheckbox.checkboxes(seqTrustee)
              val postCall = controllers.register.establishers.company.director.routes.TrusteesAlsoDirectorsController.onSubmit(establisherIndex)
              Future.successful(BadRequest(view(formWithErrors, Some(schemeName), pageHeading, titleMessage, options, postCall)))
            },
            value => {
              def uaAfterCopy: UserAnswers = (if (value.headOption.getOrElse(-1) < 0) {
                ua
              } else {
                dataPrefillService.copyAllTrusteesToDirectors(ua, value, establisherIndex)
              }).setOrException(TrusteesAlsoDirectorsId(establisherIndex))(value)

              Future.successful( true ).map { _ =>
                //userAnswersService.upsert(NormalMode, None, uaAfterCopy.json).map { _ =>
                Redirect(navigator.nextPage(TrusteesAlsoDirectorsId(establisherIndex), NormalMode, uaAfterCopy, None))
              }

            }
          )
        }
    }

  private def form(index: Index)(implicit ua: UserAnswers, messages: Messages): Form[List[Int]] = {
      val existingDirCount = ua.allDirectorsAfterDelete(index).size
      formProvider(
        existingDirCount,
        "messages__directors__prefill__multi__error__required",
        "messages__directors__prefill__multi__error__noneWithValue",
        messages("messages__directors__prefill__multi__error__moreThanTen", existingDirCount, /*config.maxDirectors*/ 10 - existingDirCount)
      )
  }
}
