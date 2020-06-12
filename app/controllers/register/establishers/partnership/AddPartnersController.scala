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

package controllers.register.establishers.partnership

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import forms.register.AddPartnersFormProvider
import identifiers.register.establishers.partnership.AddPartnersId
import javax.inject.Inject
import models.Mode
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.register.addPartners

import scala.concurrent.{ExecutionContext, Future}

class AddPartnersController @Inject()(
                                       appConfig: FrontendAppConfig,
                                       override val messagesApi: MessagesApi,
                                       navigator: Navigator,
                                       authenticate: AuthAction,
                                       getData: DataRetrievalAction,
                                       allowAccess: AllowAccessActionProvider,
                                       requireData: DataRequiredAction,
                                       formProvider: AddPartnersFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       val view: addPartners
                                     )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController with I18nSupport with Retrievals {

  private val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode, index: Int, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        retrievePartnershipName(index) { _ =>
          val partners = request.userAnswers.allPartnersAfterDelete(index)
          Future.successful(Ok(view(form, partners, postUrl(index, mode, srn), existingSchemeName, request.viewOnly,
            mode, srn)))
        }
    }

  def onSubmit(mode: Mode, index: Int, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode,
    srn) andThen requireData).async {
    implicit request =>
      val partners = request.userAnswers.allPartnersAfterDelete(index)
      if (partners.isEmpty || partners.lengthCompare(appConfig.maxPartners) >= 0) {
        Future.successful(Redirect(navigator.nextPage(AddPartnersId(index), mode, request.userAnswers, srn)))
      }
      else {

        form.bindFromRequest().fold(
          (formWithErrors: Form[_]) =>
            retrievePartnershipName(index) {
              _ =>
                Future.successful(
                  BadRequest(
                    view(
                      formWithErrors,
                      partners,
                      postUrl(index, mode, srn),
                      existingSchemeName,
                      request.viewOnly,
                      mode,
                      srn
                    )
                  )
                )
            },
          value => {
            val ua = request.userAnswers.set(AddPartnersId(index))(value).asOpt.getOrElse(request.userAnswers)
            Future.successful(Redirect(navigator.nextPage(AddPartnersId(index), mode, ua, srn)))
          }
        )
      }
  }

  private def postUrl(index: Int, mode: Mode, srn: Option[String]): Call =
    routes.AddPartnersController.onSubmit(mode, index, srn)

}
