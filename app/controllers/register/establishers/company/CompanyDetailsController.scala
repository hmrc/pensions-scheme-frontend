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

package controllers.register.establishers.company

import controllers.Retrievals
import controllers.actions._
import forms.CompanyDetailsFormProvider
import identifiers.register.establishers.company.CompanyDetailsId
import models.{Index, Mode, NormalMode, OptionalSchemeReferenceNumber}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.{EstablishersCompany, OldEstablishersCompany}
import utils.{Enumerable, UserAnswers}
import views.html.register.establishers.company.companyDetails

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CompanyDetailsController @Inject()(
                                          override val messagesApi: MessagesApi,
                                          userAnswersService: UserAnswersService,
                                          @EstablishersCompany navigator: Navigator,
                                          @OldEstablishersCompany oldNavigator: Navigator,
                                          authenticate: AuthAction,
                                          getData: DataRetrievalAction,
                                          allowAccess: AllowAccessActionProvider,
                                          requireData: DataRequiredAction,
                                          formProvider: CompanyDetailsFormProvider,
                                          val controllerComponents: MessagesControllerComponents,
                                          val view: companyDetails
                                        )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController with Retrievals with I18nSupport with Enumerable.Implicits {

  private val form = formProvider()

  def onPageLoad(mode: Mode, srn: OptionalSchemeReferenceNumber, index: Index): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        val formWithData = request.userAnswers.get(CompanyDetailsId(index)).fold(form)(form.fill)
        Future.successful(Ok(view(formWithData, mode, index, existingSchemeName, postCall(mode, srn, index), srn)))
    }

  def onSubmit(mode: Mode, srn: OptionalSchemeReferenceNumber, index: Index): Action[AnyContent] = (authenticate() andThen getData
  (mode, srn) andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[?]) =>
          Future.successful(BadRequest(view(formWithErrors, mode, index, existingSchemeName, postCall(mode, srn,
            index), srn))),
        value =>
          userAnswersService.save(mode, srn, CompanyDetailsId(index), value).flatMap {
            json =>
              mode match {
                case NormalMode => Future.successful(Redirect(navigator.nextPage(CompanyDetailsId(index), mode, UserAnswers(json), srn)))
                case _ => Future.successful(Redirect(oldNavigator.nextPage(CompanyDetailsId(index), mode, UserAnswers(json), srn)))
              }
          }
      )
  }

  private def postCall: (Mode, OptionalSchemeReferenceNumber, Index) => Call = routes.CompanyDetailsController.onSubmit

}
