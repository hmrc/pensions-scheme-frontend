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

///*
// * Copyright 2022 HM Revenue & Customs
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package controllers.establishers.company.director
//
//import config.AppConfig
//import connectors.cache.UserAnswersCacheConnector
//import controllers.Retrievals
//import controllers.actions._
//import forms.dataPrefill.DataPrefillCheckboxFormProvider
//import identifiers.beforeYouStart.SchemeNameId
//import identifiers.establishers.company.CompanyDetailsId
//import identifiers.establishers.company.director.TrusteesAlsoDirectorsId
//import models.{DataPrefillCheckbox, Index}
//import navigators.CompoundNavigator
//import play.api.data.Form
//import play.api.i18n.{I18nSupport, Messages, MessagesApi}
//import play.api.libs.json.Json
//import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
//import renderer.Renderer
//import services.DataPrefillService
//import uk.gov.hmrc.nunjucks.NunjucksSupport
//import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
//import uk.gov.hmrc.viewmodels.MessageInterpolators
//import utils.{Enumerable, UserAnswers}
//
//import javax.inject.Inject
//import scala.concurrent.{ExecutionContext, Future}
//
//class TrusteesAlsoDirectorsController @Inject()(override val messagesApi: MessagesApi,
//                                                userAnswersCacheConnector: UserAnswersCacheConnector,
//                                                navigator: CompoundNavigator,
//                                                authenticate: AuthAction,
//                                                getData: DataRetrievalAction,
//                                                requireData: DataRequiredAction,
//                                                formProvider: DataPrefillCheckboxFormProvider,
//                                                dataPrefillService: DataPrefillService,
//                                                val controllerComponents: MessagesControllerComponents,
//                                                config: AppConfig,
//                                                renderer: Renderer
//                                               )(implicit val executionContext: ExecutionContext) extends FrontendBaseController
//  with I18nSupport with Retrievals with Enumerable.Implicits with NunjucksSupport {
//
//  def onPageLoad(establisherIndex: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData()).async {
//    implicit request =>
//      (CompanyDetailsId(establisherIndex) and SchemeNameId).retrieve.right.map { case companyName ~ schemeName =>
//        implicit val ua: UserAnswers = request.userAnswers
//        val seqTrustee = dataPrefillService.getListOfTrusteesToBeCopied(establisherIndex)
//        if (seqTrustee.nonEmpty) {
//          val json = Json.obj(
//            "form" -> form(establisherIndex),
//            "schemeName" -> schemeName,
//            "pageHeading" -> msg"messages__directors__prefill__title",
//            "titleMessage" -> msg"messages__directors__prefill__heading".withArgs(companyName.companyName).resolve,
//            "dataPrefillCheckboxes" -> DataPrefillCheckbox.checkboxes(form(establisherIndex), seqTrustee)
//          )
//
//          renderer.render("dataPrefillCheckbox.njk", json).map(Ok(_))
//        } else {
//          Future(Redirect(controllers.establishers.company.routes.SpokeTaskListController.onPageLoad(establisherIndex)))
//        }
//      }
//  }
//
//  def onSubmit(establisherIndex: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData()).async {
//    implicit request =>
//      implicit val ua: UserAnswers = request.userAnswers
//      (CompanyDetailsId(establisherIndex) and SchemeNameId).retrieve.right.map { case companyName ~ schemeName =>
//        val seqTrustee = dataPrefillService.getListOfTrusteesToBeCopied(establisherIndex)
//        form(establisherIndex).bindFromRequest().fold(
//          (formWithErrors: Form[_]) => {
//            val json = Json.obj(
//              "form" -> formWithErrors,
//              "schemeName" -> schemeName,
//              "pageHeading" -> msg"messages__directors__prefill__title",
//              "titleMessage" -> msg"messages__directors__prefill__heading".withArgs(companyName.companyName).resolve,
//              "dataPrefillCheckboxes" -> DataPrefillCheckbox.checkboxes(form(establisherIndex), seqTrustee)
//            )
//            renderer.render("dataPrefillCheckbox.njk", json).map(BadRequest(_))
//          },
//          value => {
//            val uaAfterCopy: UserAnswers = if (value.headOption.getOrElse(-1) < 0) ua else
//              dataPrefillService.copyAllTrusteesToDirectors(ua, value, establisherIndex)
//            val updatedUa = uaAfterCopy.setOrException(TrusteesAlsoDirectorsId(establisherIndex), value)
//            userAnswersCacheConnector.save(request.lock, uaAfterCopy.data).map { _ =>
//              Redirect(navigator.nextPage(TrusteesAlsoDirectorsId(establisherIndex), updatedUa))
//            }
//          }
//        )
//      }
//  }
//
//  private def form(index: Index)(implicit ua: UserAnswers, messages: Messages): Form[List[Int]] = {
//    val existingDirCount = ua.allDirectorsAfterDelete(index).size
//    formProvider(
//      existingDirCount,
//      "messages__directors__prefill__multi__error__required",
//      "messages__directors__prefill__multi__error__noneWithValue",
//      messages("messages__directors__prefill__multi__error__moreThanTen", existingDirCount, config.maxDirectors - existingDirCount)
//    )
//  }
//}
