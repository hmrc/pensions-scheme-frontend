/*
 * Copyright 2019 HM Revenue & Customs
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

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import forms.register.establishers.company.HasCompanyNumberFormProvider
import identifiers.register.establishers.company.{CompanyDetailsId, HasCompanyNumberId}
import javax.inject.Inject
import models.{Index, Mode, UpdateMode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{Navigator, UserAnswers}
import utils.annotations.EstablishersCompany
import views.html.register.establishers.company.hasCompanyNumber

import scala.concurrent.{ExecutionContext, Future}

class HasCompanyNumberController @Inject()(appConfig: FrontendAppConfig,
                                           override val messagesApi: MessagesApi,
                                           userAnswersService: UserAnswersService,
                                           @EstablishersCompany navigator: Navigator,
                                           authenticate: AuthAction,
                                           allowAccess: AllowAccessActionProvider,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           formProvider: HasCompanyNumberFormProvider)(implicit val ec: ExecutionContext)
  extends FrontendController with Retrievals with I18nSupport {

  private def form(companyName:String): Form[Boolean] = formProvider.apply(companyName)
  private def postCall = controllers.register.establishers.company.routes.HasCompanyNumberController.onSubmit _

  def onPageLoad(mode: Mode, srn: Option[String] = None, index: Index): Action[AnyContent] = (authenticate andThen
    getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
    implicit request =>
      CompanyDetailsId(index.id).retrieve.right.map {
        details =>
          val preparedForm = request.userAnswers.get(HasCompanyNumberId(index)) match {
            case None => form(details.companyName)
            case Some(value) => form(details.companyName).fill(value)
          }
          Future.successful(Ok(hasCompanyNumber(appConfig, preparedForm, details.companyName, existingSchemeName, postCall(mode, srn, index), srn)))
      }
  }

  def onSubmit(mode: Mode, srn: Option[String] = None, index: Index): Action[AnyContent] = (authenticate andThen
    getData(mode, srn) andThen requireData).async {
    implicit request => CompanyDetailsId(index.id).retrieve.right.map {
      details =>
        form(details.companyName).bindFromRequest().fold(
          (formWithErrors: Form[_]) =>
            Future.successful(BadRequest(hasCompanyNumber(appConfig, formWithErrors, details.companyName, existingSchemeName, postCall(mode, srn, index), srn))),
          value => {
            userAnswersService.save(mode, srn, HasCompanyNumberId(index), value).map(cacheMap =>
              Redirect(navigator.nextPage(HasCompanyNumberId(index), mode, UserAnswers(cacheMap), srn))
            )
          }
        )
    }
  }

}
