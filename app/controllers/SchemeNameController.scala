/*
 * Copyright 2021 HM Revenue & Customs
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

import connectors.{PensionAdministratorConnector, UserAnswersCacheConnector}
import controllers.actions._
import forms.register.SchemeNameFormProvider
import identifiers.SchemeNameId
import models.Mode
import navigators.Navigator
import play.api.Logger
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils._
import utils.annotations.BeforeYouStart
import views.html.schemeName

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SchemeNameController @Inject()(
                                      override val messagesApi: MessagesApi,
                                      dataCacheConnector: UserAnswersCacheConnector,
                                      @BeforeYouStart navigator: Navigator,
                                      allowAccess: AllowAccessActionProvider,
                                      authenticate: AuthAction,
                                      getData: DataRetrievalAction,
                                      formProvider: SchemeNameFormProvider,
                                      nameMatchingFactory: NameMatchingFactory,
                                      pensionAdministratorConnector: PensionAdministratorConnector,
                                      val controllerComponents: MessagesControllerComponents,
                                      val view: schemeName
                                    )(implicit val executionContext: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport
    with Retrievals {

  private val logger  = Logger(classOf[SchemeNameController])

  private val form = formProvider()

  def onPageLoad(mode: Mode, srn: Option[String]): Action[AnyContent] = (authenticate() andThen getData() andThen allowAccess(srn)) {
    implicit request =>
      val preparedForm = request.userAnswers.flatMap(_.get(SchemeNameId)).fold(form)(v => form.fill(v))
      Ok(view(preparedForm, mode, existingSchemeName.getOrElse("")))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate() andThen getData()).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, mode, existingSchemeName.getOrElse("")))),
        value =>
          nameMatchingFactory.nameMatching(value).flatMap { nameMatching =>
            if (nameMatching.isMatch) {
              pensionAdministratorConnector.getPSAName.map { psaName =>
                BadRequest(view(form.withError(
                  "schemeName",
                  "messages__error__scheme_name_psa_name_match", psaName
                ), mode, existingSchemeName.getOrElse("")))
              }
            } else {
              dataCacheConnector.save(request.externalId, SchemeNameId, value).map { cacheMap =>
                Redirect(navigator.nextPage(SchemeNameId, mode, UserAnswers(cacheMap)))
              }
            } recoverWith {
              case e: NotFoundException =>
                logger.error(e.message)
                Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
            }
          }
      )
  }
}
