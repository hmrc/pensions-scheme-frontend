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

package controllers.register.trustees.individual

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import forms.DOBFormProvider
import identifiers.register.trustees.individual.{TrusteeDOBId, TrusteeNameId}
import javax.inject.Inject
import models.{Index, Mode}
import navigators.Navigator
import org.joda.time.LocalDate
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{Enumerable, UserAnswers}
import viewmodels.Message
import views.html.register.DOB

import scala.concurrent.{ExecutionContext, Future}

class TrusteeDOBController @Inject()(appConfig: FrontendAppConfig,
                                     override val messagesApi: MessagesApi,
                                     userAnswersService: UserAnswersService,
                                     navigator: Navigator,
                                     authenticate: AuthAction,
                                     getData: DataRetrievalAction,
                                     allowAccess: AllowAccessActionProvider,
                                     requireData: DataRequiredAction,
                                     formProvider: DOBFormProvider
                                    )(implicit val ec: ExecutionContext)
  extends FrontendController with Retrievals with I18nSupport with Enumerable.Implicits {

  private val form = formProvider()

  private def postCall: (Mode, Index, Option[String]) => Call = routes.TrusteeDOBController.onSubmit

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        val preparedForm = request.userAnswers.get[LocalDate](TrusteeDOBId(index)) match {
          case Some(value) => form.fill(value)
          case None => form
        }

        TrusteeNameId(index).retrieve.right.map(
          personName =>
            Future.successful(Ok(
              DOB(appConfig, preparedForm, mode, existingSchemeName, postCall(mode, index, srn), srn, personName.fullName, Message("messages__theTrustee").resolve))
            ))
    }


  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        form.bindFromRequest().fold(
          formWithErrors =>
            TrusteeNameId(index).retrieve.right.map(
              personName =>
                Future.successful(BadRequest(
                  DOB(appConfig, formWithErrors, mode, existingSchemeName, postCall(mode, index, srn), srn, personName.fullName, Message("messages__theTrustee").resolve))
                )),

          value =>
            userAnswersService.save(mode, srn, TrusteeDOBId(index), value).map {
              cacheMap =>
                Redirect(navigator.nextPage(TrusteeDOBId(index), mode, UserAnswers(cacheMap), srn))
            }
        )
    }
}
