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

import controllers.ControllerSpecBase
import controllers.actions._
import controllers.behaviours.ControllerAllowChangeBehaviour
import identifiers.register.trustees.individual.{TrusteeDetailsId, TrusteeNewNinoId}
import models._
import models.person.PersonDetails
import org.joda.time.LocalDate
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import services.FakeUserAnswersService
import utils._
import viewmodels.{AnswerRow, AnswerSection, Message}
import views.html.checkYourAnswers

class CheckYourAnswersControllerSpec extends ControllerSpecBase with ControllerAllowChangeBehaviour {

  import CheckYourAnswersControllerSpec._

  "Check Your Answers Controller" must {
    "return 200 and the correct view for a GET" in {
      val result = controller(getMandatoryTrusteeNonHnS).onPageLoad(NormalMode, firstIndex, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(answerSections, NormalMode, None)
    }

    "return OK and display Add link for UpdateMode pointing to new Nino page where no nino retrieved from ETMP" in {
      val expectedAnswerSections = {
        val expectedAnswerRowNino = AnswerRow("messages__common__nino", Seq("site.not_entered"), answerIsMessageKey = true,
          Some(Link("site.add",
            routes.TrusteeNinoNewController.onPageLoad(Mode.checkMode(UpdateMode), firstIndex, Some("srn")).url,
            Some(s"messages__visuallyhidden__trustee__nino"))))
        Seq(
          trusteeDetailsSectionUpdate(expectedAnswerRowNino),
          contactDetailsSection
        )
      }

      val result = controller(getMandatoryTrusteeNonHnS).onPageLoad(UpdateMode, firstIndex, Some("srn"))(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(expectedAnswerSections, UpdateMode, Some("srn"))
    }

    "return OK and display no Add link but do display old nino for UpdateMode where a nino retrieved from ETMP" in {
      val expectedAnswerSections = {
        val expectedAnswerRowNino = AnswerRow("messages__common__nino", Seq("CS121212C"), answerIsMessageKey = false, None)
        Seq(
          trusteeDetailsSectionUpdate(expectedAnswerRowNino),
          contactDetailsSection
        )
      }

      val result = controller(getMandatoryTrusteeWithNinoFromEtmp).onPageLoad(UpdateMode, firstIndex, Some("srn"))(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(expectedAnswerSections, UpdateMode, Some("srn"))
    }

    "return OK and display change link and display new nino for UpdateMode where" +
      "a new nino has already been entered" in {
      val expectedAnswerSections = {
        val expectedAnswerRowNino = AnswerRow("messages__common__nino", Seq("CS121212C"), answerIsMessageKey = false,
          Some(Link("site.change", routes.TrusteeNinoNewController.onPageLoad(CheckUpdateMode, 0, Some("srn")).url,
            Some("messages__visuallyhidden__trustee__nino"))))
        Seq(
          trusteeDetailsSectionUpdate(expectedAnswerRowNino),
          contactDetailsSection
        )
      }

      val result = controller(getTrusteeWithNewNino).onPageLoad(UpdateMode, firstIndex, Some("srn"))(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(expectedAnswerSections, UpdateMode, Some("srn"))
    }

    behave like changeableController(
      controller(getMandatoryTrusteeNonHnS, _: AllowChangeHelper).onPageLoad(NormalMode, firstIndex, None)(fakeRequest)
    )
  }
}

object CheckYourAnswersControllerSpec extends ControllerSpecBase with ControllerAllowChangeBehaviour {
  val schemeName = "Test Scheme Name"
  val trusteeName = "Test Trustee Name"
  val firstIndex = Index(0)
  lazy val trusteeDetailsRoute: String = routes.TrusteeDetailsController.onPageLoad(CheckMode, firstIndex, None).url

  def postUrl(mode: Mode, srn: Option[String]): Call = controllers.register.trustees.routes.AddTrusteeController.onPageLoad(mode, srn)

  lazy val trusteeDetailsSection = AnswerSection(None,
    Seq(
      AnswerRow(
        "messages__common__cya__name",
        Seq("Test Trustee Name"),
        answerIsMessageKey = false,
        Some(Link("site.change", trusteeDetailsRoute, Some(Message("messages__visuallyhidden__common__name", trusteeName))))
      ),
      AnswerRow(
        "messages__common__dob",
        Seq(s"${DateHelper.formatDate(LocalDate.now)}"),
        answerIsMessageKey = false,
        Some(Link("site.change", trusteeDetailsRoute, Some(Message("messages__visuallyhidden__common__dob", trusteeName))))
      )
    )
  )


  def trusteeDetailsSectionUpdate(ninoRow: AnswerRow) = AnswerSection(None,
    Seq(
      AnswerRow(
        "messages__common__cya__name",
        Seq("Test Trustee Name"),
        answerIsMessageKey = false,
        None
      ),
      AnswerRow(
        "messages__common__dob",
        Seq(s"${DateHelper.formatDate(LocalDate.now)}"),
        answerIsMessageKey = false,
        None
      ),
      ninoRow
    )
  )


  lazy val contactDetailsSection = AnswerSection(
    Some("messages__checkYourAnswers__section__contact_details"),
    Seq.empty[AnswerRow]
  )
  val onwardRoute = controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode, None)

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisher,
                 allowChangeHelper: AllowChangeHelper = ach): CheckYourAnswersController =
    new CheckYourAnswersController(
      frontendAppConfig,
      messagesApi,
      new FakeNavigator(onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      FakeUserAnswersService,
      new FakeCountryOptions,
      allowChangeHelper
    )

  val answerSections = Seq(
    trusteeDetailsSection,
    contactDetailsSection
  )

  def viewAsString(answerSections: Seq[AnswerSection], mode: Mode, srn: Option[String]): String = checkYourAnswers(
    frontendAppConfig,
    answerSections,
    postUrl(mode, srn),
    None,
    hideEditLinks = false,
    hideSaveAndContinueButton = false,
    srn = srn,
    mode = mode
  )(fakeRequest, messages).toString

  def getTrusteeWithNewNino: DataRetrievalAction = UserAnswers().set(
    TrusteeDetailsId(0))(PersonDetails("Test", Some("Trustee"), "Name", LocalDate.now)).flatMap(
    _.set(TrusteeNewNinoId(0))(ReferenceValue("CS121212C", true))
  ).asOpt.value.dataRetrievalAction

  def getMandatoryTrusteeWithNinoFromEtmp: FakeDataRetrievalAction = new FakeDataRetrievalAction(Some(
    Json.obj(
      "trustees" -> Json.arr(
        Json.obj(
          TrusteeDetailsId.toString ->
            PersonDetails("Test", Some("Trustee"), "Name", LocalDate.now),
          TrusteeNewNinoId.toString -> Json.obj(
            "value" -> "CS121212C"
          )
        )
      )
    )))
}
