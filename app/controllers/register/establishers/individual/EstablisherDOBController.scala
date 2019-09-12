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

package controllers.register.establishers.individual

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import forms.DOBFormProvider
import identifiers.register.establishers.individual.{EstablisherDOBId, EstablisherNameId}
import javax.inject.Inject
import models.{Index, Mode}
import navigators.Navigator
import org.joda.time.LocalDate
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{Enumerable, UserAnswers}
import views.html.register.establishers.individual.establisherDOB

import scala.concurrent.{ExecutionContext, Future}

class EstablisherDOBController @Inject()(appConfig: FrontendAppConfig,
                                         override val messagesApi: MessagesApi,
                                         userAnswersService: UserAnswersService,
                                         navigator: Navigator,
                                         authenticate: AuthAction,
                                         getData: DataRetrievalAction,
                                         allowAccess: AllowAccessActionProvider,
                                         requireData: DataRequiredAction,
                                         formProvider: DOBFormProvider
                                        )(implicit val ec: ExecutionContext)
  extends FrontendController
    with Retrievals
    with I18nSupport
    with Enumerable.Implicits {

  private val form = formProvider()

  private def postCall: (Mode, Index, Option[String]) => Call = routes.EstablisherDOBController.onSubmit

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        val preparedForm = request.userAnswers.get[LocalDate](EstablisherDOBId(index)) match {
          case Some(value) => form.fill(value)
          case None => form
        }

        EstablisherNameId(index).retrieve.right.map {
          establisherName =>
            Future.successful(Ok(
              establisherDOB(appConfig, preparedForm, mode, existingSchemeName, postCall(mode, index, srn), srn, establisherName.fullName)
            ))
        }
    }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        form.bindFromRequest().fold(
          formWithErrors =>
            EstablisherNameId(index).retrieve.right.map {
              establisherName =>
                Future.successful(
                  BadRequest(
                    establisherDOB(appConfig, formWithErrors, mode, existingSchemeName, postCall(mode, index, srn), srn, establisherName.fullName)
                  )
                )
            },
          value =>
            userAnswersService.save(mode, srn, EstablisherDOBId(index), value).map {
              cacheMap =>
                Redirect(navigator.nextPage(EstablisherDOBId(index), mode, UserAnswers(cacheMap), srn))
            }
        )
    }
}
