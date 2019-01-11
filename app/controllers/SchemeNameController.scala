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

package controllers

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.actions._
import forms.register.SchemeNameFormProvider
import identifiers.{IsBeforeYouStartCompleteId, SchemeNameId}
import identifiers.register.IsAboutSchemeCompleteId
import javax.inject.Inject
import models.Mode
import models.PSAName._
import play.api.Logger
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.BeforeYouStart
import utils.{NameMatchingFactory, Navigator, SectionComplete, UserAnswers}
import views.html.schemeName

import scala.concurrent.{ExecutionContext, Future}

class SchemeNameController @Inject()(appConfig: FrontendAppConfig,
                                     override val messagesApi: MessagesApi,
                                     dataCacheConnector: UserAnswersCacheConnector,
                                     @BeforeYouStart navigator: Navigator,
                                     authenticate: AuthAction,
                                     getData: DataRetrievalAction,
                                     requireData: DataRequiredAction,
                                     formProvider: SchemeNameFormProvider,
                                     nameMatchingFactory: NameMatchingFactory,
                                     sectionComplete: SectionComplete)(implicit val ec: ExecutionContext) extends FrontendController with I18nSupport {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData) {
    implicit request =>
      val preparedForm = request.userAnswers.flatMap(_.get(SchemeNameId)).fold(form)(v=> form.fill(v))
      Ok(schemeName(appConfig, preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(schemeName(appConfig, formWithErrors, mode))),
        value =>
          nameMatchingFactory.nameMatching(value).flatMap { nameMatching =>
            if (nameMatching.isMatch) {
              Future.successful(BadRequest(schemeName(appConfig, form.withError(
                "schemeName",
                "messages__error__scheme_name_psa_name_match"
              ), mode)))
            } else {
              dataCacheConnector.save(request.externalId, SchemeNameId, value).flatMap { cacheMap =>
                sectionComplete.setCompleteFlag(request.externalId, IsBeforeYouStartCompleteId, UserAnswers(cacheMap), value = false).map { json =>
                  Redirect(navigator.nextPage(SchemeNameId, mode, json))
                }
              }
            } recoverWith {
              case e: NotFoundException =>
                Logger.error(e.message)
                Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
            }
          }
      )
  }


}
