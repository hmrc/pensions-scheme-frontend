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

import config.{FeatureSwitchManagementService, FrontendAppConfig}
import controllers.Retrievals
import controllers.actions._
import forms.register.establishers.company.AddCompanyDirectorsFormProvider
import identifiers.register.establishers.company.AddCompanyDirectorsId
import javax.inject.Inject
import models.register.{Director, DirectorEntityNonHnS}
import models.{Index, Mode}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.Toggles
import utils.annotations.EstablishersCompany
import views.html.register.establishers.company.addCompanyDirectors

import scala.concurrent.{ExecutionContext, Future}

class AddCompanyDirectorsController @Inject()(
                                               appConfig: FrontendAppConfig,
                                               override val messagesApi: MessagesApi,
                                               @EstablishersCompany navigator: Navigator,
                                               authenticate: AuthAction,
                                               getData: DataRetrievalAction,
                                               requireData: DataRequiredAction,
                                               formProvider: AddCompanyDirectorsFormProvider,
                                               fs: FeatureSwitchManagementService
                                             )(implicit val ec: ExecutionContext) extends FrontendController with I18nSupport with Retrievals {

  private val form: Form[Boolean] = formProvider()

  private def postCall: (Mode, Option[String], Index) => Call = routes.AddCompanyDirectorsController.onSubmit _

  private val isHnSEnabled = fs.get(Toggles.isEstablisherCompanyHnSEnabled)

  def onPageLoad(mode: Mode, srn: Option[String], index: Int): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      val directors = request.userAnswers.allDirectorsAfterDelete(index, isHnSEnabled)
      val enableSubmission = checkForEnableSubmission(fs.get(Toggles.isEstablisherCompanyHnSEnabled), directors)
      Future.successful(Ok(addCompanyDirectors(appConfig, form, directors, existingSchemeName, postCall(mode, srn, index), request.viewOnly, mode, srn, enableSubmission)))
  }

  def onSubmit(mode: Mode, srn: Option[String], index: Int): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      val directors = request.userAnswers.allDirectorsAfterDelete(index, isHnSEnabled)
      val enableSubmission = checkForEnableSubmission(fs.get(Toggles.isEstablisherCompanyHnSEnabled), directors)

      if (directors.isEmpty || directors.lengthCompare(appConfig.maxDirectors) >= 0) {
        Future.successful(Redirect(navigator.nextPage(AddCompanyDirectorsId(index), mode, request.userAnswers, srn)))
      }
      else {
        form.bindFromRequest().fold(
          (formWithErrors: Form[_]) =>
            Future.successful(
              BadRequest(
                addCompanyDirectors(
                  appConfig,
                  formWithErrors,
                  directors,
                  existingSchemeName,
                  postCall(mode, srn, index),
                  request.viewOnly,
                  mode,
                  srn,
                  enableSubmission
                )
              )
            ),
          value => {
            val ua = request.userAnswers.set(AddCompanyDirectorsId(index))(value).asOpt.getOrElse(request.userAnswers)
            Future.successful(Redirect(navigator.nextPage(AddCompanyDirectorsId(index), mode, ua, srn)))
          }
        )
      }
  }

  private def checkForEnableSubmission(toggled: Boolean, directors: Seq[Director[_]]): Boolean = toggled || directors.forall(_.isCompleted)

}
