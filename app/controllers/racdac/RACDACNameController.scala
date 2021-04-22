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

package controllers.racdac

import connectors.{PensionAdministratorConnector, UserAnswersCacheConnector}
import controllers.actions._
import forms.racdac.RACDACNameFormProvider
import identifiers.racdac.RACDACNameId
import models.Mode
import navigators.Navigator
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils._
import utils.annotations.BeforeYouStart
import views.html.racdac.racDACName

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RACDACNameController @Inject()(
                                      override val messagesApi: MessagesApi,
                                      dataCacheConnector: UserAnswersCacheConnector,
                                      @BeforeYouStart navigator: Navigator,
                                      authenticate: AuthAction,
                                      getData: DataRetrievalAction,
                                      formProvider: RACDACNameFormProvider,
                                      nameMatchingFactory: NameMatchingFactory,
                                      pensionAdministratorConnector: PensionAdministratorConnector,
                                      val controllerComponents: MessagesControllerComponents,
                                      val view: racDACName
                                    )(implicit val executionContext: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport
    with Retrievals {

  private val logger  = Logger(classOf[RACDACNameController])

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate() andThen getData()).async {
    implicit request => {
      val preparedForm = request.userAnswers.flatMap(_.get(RACDACNameId)).fold(form)(v => form.fill(v))
      pensionAdministratorConnector.getPSAName.flatMap { psaName =>
        Future.successful(Ok(view(preparedForm, mode, psaName)))
      }
    }

  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate() andThen getData()).async {
    implicit request =>
      Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
//      form.bindFromRequest().fold(
//        (formWithErrors: Form[_]) =>
//          Future.successful(BadRequest(view(formWithErrors, mode, existingRACDACName.getOrElse("")))),
//        value =>
//          nameMatchingFactory.nameMatching(value).flatMap { nameMatching =>
//            if (nameMatching.isMatch) {
//              pensionAdministratorConnector.getPSAName.map { psaName =>
//                BadRequest(view(form.withError(
//                  "racDACName",
//                  "messages__error__scheme_name_psa_name_match", psaName
//                ), mode, existingRACDACName.getOrElse("")))
//              }
//            } else {
//              dataCacheConnector.save(request.externalId, RACDACNameId, value).map { cacheMap =>
//                Redirect(navigator.nextPage(RACDACNameId, mode, UserAnswers(cacheMap)))
//              }
//            } recoverWith {
//              case e: NotFoundException =>
//                logger.error(e.message)
//                Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
//            }
//          }
//      )
  }
}
