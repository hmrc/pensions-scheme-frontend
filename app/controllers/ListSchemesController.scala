/*
 * Copyright 2018 HM Revenue & Customs
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

import com.google.inject.Inject
import config.FrontendAppConfig
import models.SchemeDetail
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Action
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.list_schemes

class ListSchemesController @Inject()(val appConfig: FrontendAppConfig, val messagesApi: MessagesApi) extends FrontendController with I18nSupport {

  def onPageLoad = Action {
    implicit request =>

      val schemes: List[SchemeDetail] = List(
        SchemeDetail(
          "scheme-name-0",
          "S8888888888",
          "Pending",
          None,
          None,
          None,
          None
        ),
        SchemeDetail(
          "scheme-name-1",
          "S0000000000",
          "Pending Info Required",
          None,
          None,
          None,
          None
        ),
        SchemeDetail(
          "scheme-name-2",
          "S1111111111",
          "Pending Info Received",
          None,
          None,
          None,
          None
        ),
        SchemeDetail(
          "scheme-name-3",
          "S2222222222",
          "Rejected",
          None,
          None,
          None,
          None
        ),
        SchemeDetail(
          "scheme-name-4",
          "S3333333333",
          "Open",
          None,
          Some("pstr-5"),
          None,
          None
        ),
        SchemeDetail(
          "scheme-name-5",
          "S4444444444",
          "Deregistered",
          None,
          Some("44444444WW"),
          None,
          None
        ),
        SchemeDetail(
          "scheme-name-6",
          "S5555555555",
          "Wound-up",
          None,
          Some("11111111II"),
          None,
          None
        ),
        SchemeDetail(
          "scheme-name-7",
          "S6666666666",
          "Rejected Under Appeal",
          None,
          None,
          None,
          None
        )
      )

      Ok(list_schemes(appConfig, schemes))

  }
}
