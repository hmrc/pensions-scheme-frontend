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
import forms.register.establishers.company.CompanyUniqueTaxReferenceFormProvider
import identifiers.register.establishers.company.CompanyUniqueTaxReferenceId
import javax.inject.Inject
import models.{Index, Mode, UniqueTaxReference}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.EstablishersCompany
import utils.{Enumerable, Navigator, UserAnswers}
import views.html.register.establishers.company.companyUniqueTaxReference

import scala.concurrent.{ExecutionContext, Future}

class CompanyUniqueTaxReferenceController @Inject()(
                                                     appConfig: FrontendAppConfig,
                                                     override val messagesApi: MessagesApi,
                                                     userAnswersService: UserAnswersService,
                                                     @EstablishersCompany navigator: Navigator,
                                                     authenticate: AuthAction,
                                                     getData: DataRetrievalAction,
                                                     allowAccess: AllowAccessActionProvider,
                                                     requireData: DataRequiredAction,
                                                     formProvider: CompanyUniqueTaxReferenceFormProvider
                                                   )(implicit val ec: ExecutionContext) extends FrontendController with Retrievals with I18nSupport with Enumerable.Implicits {

  private val form: Form[UniqueTaxReference] = formProvider()

  def onPageLoad(mode: Mode, srn: Option[String], index: Index): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        val redirectResult = request.userAnswers.get(CompanyUniqueTaxReferenceId(index)) match {
          case None =>
            Ok(companyUniqueTaxReference(appConfig, form, mode, index, existingSchemeName, postCall(mode, srn, index), srn))
          case Some(value) =>
            Ok(companyUniqueTaxReference(appConfig, form.fill(value), mode, index, existingSchemeName, postCall(mode, srn, index), srn))
        }
        Future.successful(redirectResult)
    }

  def onSubmit(mode: Mode, srn: Option[String], index: Index): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(companyUniqueTaxReference(appConfig, formWithErrors, mode, index, existingSchemeName, postCall(mode, srn, index), srn))),
        value =>
          userAnswersService.save(
            mode,
            srn,
            CompanyUniqueTaxReferenceId(index),
            value
          ).map {
            json =>
              Redirect(navigator.nextPage(CompanyUniqueTaxReferenceId(index), mode, UserAnswers(json), srn))
          }
      )
  }

  private def postCall: (Mode, Option[String], Index) => Call = routes.CompanyUniqueTaxReferenceController.onSubmit _

}
