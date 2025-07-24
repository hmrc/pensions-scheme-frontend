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
import controllers.actions.*
import forms.register.trustees.AddTrusteeFormProvider
import identifiers.register.trustees.{AddTrusteeId, IsTrusteeNewId, TrusteeKindId, TrusteesId}
import models.register.Trustee
import models.requests.DataRequest
import models.{Mode, NormalMode, OptionalSchemeReferenceNumber}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.{NoSuspendedCheck, Trustees}
import utils.UserAnswers
import views.html.register.trustees.*

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class AddTrusteeController @Inject()(
                                      appConfig: FrontendAppConfig,
                                      override val messagesApi: MessagesApi,
                                      @Trustees navigator: Navigator,
                                      authenticate: AuthAction,
                                      getData: DataRetrievalAction,
                                      @NoSuspendedCheck allowAccess: AllowAccessActionProvider,
                                      requireData: DataRequiredAction,
                                      formProvider: AddTrusteeFormProvider,
                                      val controllerComponents: MessagesControllerComponents,
                                      val view: addTrustee,
                                      val addTrusteeOldView: addTrusteeOld,
                                      userAnswersService: UserAnswersService
                                    )(implicit val executionContext: ExecutionContext)
  extends FrontendBaseController with I18nSupport with Retrievals {

  private val form = formProvider()

  private def renderPage(
                          trustees: Seq[Trustee[?]],
                          mode: Mode,
                          srn: OptionalSchemeReferenceNumber,
                          form: Form[Boolean],
                          status: Status
                        )(implicit request: DataRequest[AnyContent]): Result =
      mode match {
        case NormalMode =>
          val completeTrustees = trustees.filter(_.isCompleted)
          val incompleteTrustees = trustees.filterNot(_.isCompleted)
          status(view(form, mode, completeTrustees, incompleteTrustees, existingSchemeName, srn))
        case _ =>
          status(addTrusteeOldView(form, mode, trustees, existingSchemeName, srn))
      }


  def onPageLoad(mode: Mode, srn: OptionalSchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        val userAnswersWithCleanedTrustees: JsValue =
          userAnswersService.removeEmptyObjectsAndIncompleteEntities(
            json          = request.userAnswers.json,
            collectionKey = TrusteesId.toString,
            keySet        = Set(IsTrusteeNewId.toString, TrusteeKindId.toString),
            externalId    = request.externalId
          )

        userAnswersService.upsert(mode, srn, userAnswersWithCleanedTrustees).map { jsValue =>
          renderPage(UserAnswers(jsValue).allTrusteesAfterDelete, mode, srn, form, Ok)
        }
    }

  def onSubmit(mode: Mode, srn: OptionalSchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen requireData) {
      implicit request =>

        val trustees = request.userAnswers.allTrusteesAfterDelete

        if (trustees.isEmpty || trustees.lengthCompare(appConfig.maxTrustees) >= 0) {
          Redirect(navigator.nextPage(AddTrusteeId, mode, request.userAnswers, srn))
        } else {
          form.bindFromRequest().fold(
            formWithErrors =>
              renderPage(trustees, mode, srn, formWithErrors, BadRequest),
            value =>
              Redirect(navigator.nextPage(AddTrusteeId, mode, request.userAnswers.setOrException(AddTrusteeId)(value), srn))
          )
        }
    }

}
