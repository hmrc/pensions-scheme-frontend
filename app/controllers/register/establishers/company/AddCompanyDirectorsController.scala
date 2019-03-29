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
import connectors.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions._
import forms.register.establishers.company.AddCompanyDirectorsFormProvider
import identifiers.register.establishers.company.AddCompanyDirectorsId
import javax.inject.Inject
import models.{Index, Mode}
import play.api.Logger
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.JsResultException
import play.api.mvc.{Action, AnyContent, Call}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.Navigator
import utils.annotations.EstablishersCompany
import views.html.register.establishers.company.addCompanyDirectors

import scala.concurrent.{ExecutionContext, Future}

class AddCompanyDirectorsController @Inject()(
                                               appConfig: FrontendAppConfig,
                                               override val messagesApi: MessagesApi,
                                               dataCacheConnector: UserAnswersCacheConnector,
                                               @EstablishersCompany navigator: Navigator,
                                               authenticate: AuthAction,
                                               getData: DataRetrievalAction,
                                               requireData: DataRequiredAction,
                                               formProvider: AddCompanyDirectorsFormProvider
                                             )(implicit val ec: ExecutionContext) extends FrontendController with I18nSupport with Retrievals {

  private val form: Form[Boolean] = formProvider()
  private def postCall: (Mode, Option[String], Index) => Call = routes.AddCompanyDirectorsController.onSubmit _

  def onPageLoad(mode: Mode, srn: Option[String], index: Int): Action[AnyContent] = (authenticate andThen getData() andThen requireData).async {
    implicit request =>
      retrieveCompanyName(index) {
        companyName =>
          val directors = request.userAnswers.allDirectorsAfterDelete(index)
          Future.successful(Ok(addCompanyDirectors(appConfig, form, mode, index, directors, existingSchemeName, postCall(mode, srn, index))))
      }
  }

  def onSubmit(mode: Mode, srn: Option[String], index: Int): Action[AnyContent] = (authenticate andThen getData() andThen requireData).async {
    implicit request =>
      val directors = request.userAnswers.allDirectorsAfterDelete(index)
      if (directors.isEmpty || directors.lengthCompare(appConfig.maxDirectors) >= 0) {
        Future.successful(Redirect(navigator.nextPage(AddCompanyDirectorsId(index), mode, request.userAnswers)))
      }
      else {

        form.bindFromRequest().fold(
          (formWithErrors: Form[_]) =>
            retrieveCompanyName(index) {
              companyName =>
                Future.successful(
                  BadRequest(
                    addCompanyDirectors(
                      appConfig,
                      formWithErrors,
                      mode,
                      index,
                      directors,
                      existingSchemeName,
                      postCall(mode, srn, index)
                    )
                  )
                )
            },
          value =>
            request.userAnswers.set(AddCompanyDirectorsId(index))(value).fold(
              errors => {
                Logger.error("Unable to set user answer", JsResultException(errors))
                Future.successful(InternalServerError)
              },
              userAnswers => {
                Future.successful(Redirect(navigator.nextPage(AddCompanyDirectorsId(index), mode, userAnswers)))
              }
            )
        )
      }
  }

}
