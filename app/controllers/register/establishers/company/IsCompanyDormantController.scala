/*
 * Copyright 2020 HM Revenue & Customs
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

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import forms.register.establishers.IsDormantFormProvider
import identifiers.register.establishers.company.IsCompanyDormantId
import models.register.DeclarationDormant
import models.register.DeclarationDormant._
import models.{Mode, NormalMode}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.EstablishersCompany
import utils.{Enumerable, UserAnswers}
import views.html.register.establishers.isDormant

import scala.concurrent.{ExecutionContext, Future}

class IsCompanyDormantController @Inject()(appConfig: FrontendAppConfig,
                                 override val messagesApi: MessagesApi,
                                 userAnswersService: UserAnswersService,
                                 @EstablishersCompany navigator: Navigator,
                                 authenticate: AuthAction,
                                 getData: DataRetrievalAction,
                                 requireData: DataRequiredAction,
                                 formProvider: IsDormantFormProvider
                                 )(implicit val ec: ExecutionContext) extends FrontendController with Enumerable.Implicits with I18nSupport with Retrievals {

  private val form: Form[DeclarationDormant] = formProvider()
  private def postCall(mode: Mode, srn: Option[String], index: Int): Call = routes.IsCompanyDormantController.onSubmit(mode, srn, index )

  def onPageLoad(mode: Mode, srn: Option[String], index: Int): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      retrieveCompanyName(index) {
        companyName =>
          val preparedForm = request.userAnswers.get(IsCompanyDormantId(index)).fold(form)(v=> form.fill(v))
          Future.successful(Ok(isDormant(appConfig, preparedForm, companyName, postCall(mode, srn, index), existingSchemeName)))
      }
  }

  def onSubmit(mode: Mode, srn: Option[String], index: Int): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      retrieveCompanyName(index) { companyName =>
        form.bindFromRequest().fold(
          (formWithErrors: Form[_]) =>
            Future.successful(BadRequest(isDormant(appConfig, formWithErrors, companyName, postCall(mode, srn, index), existingSchemeName))),
          {
            case Yes =>
              userAnswersService.save(mode, srn, IsCompanyDormantId(index), DeclarationDormant.values(0)).map { cacheMap =>
                 Redirect(navigator.nextPage(IsCompanyDormantId(index), mode, UserAnswers(cacheMap), srn))
             }
            case No =>
              userAnswersService.save(mode, srn, IsCompanyDormantId(index), DeclarationDormant.values(1)).map(cacheMap =>
                Redirect(navigator.nextPage(IsCompanyDormantId(index), mode, UserAnswers(cacheMap), srn)))

          }

        )
      }
  }

}
