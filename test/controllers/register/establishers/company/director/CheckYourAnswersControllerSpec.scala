/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.register.establishers.company.director

import base.SpecBase
import controllers.ControllerSpecBase
import controllers.actions._
import controllers.behaviours.ControllerAllowChangeBehaviour
import controllers.register.establishers.company.routes.AddCompanyDirectorsController
import identifiers.register.establishers.company.director._
import models._
import models.address.Address
import models.person.PersonName
import models.requests.DataRequest
import play.api.mvc.{AnyContent, Call}
import play.api.test.Helpers._
import services.FakeUserAnswersService
import utils._
import utils.checkyouranswers.Ops._
import viewmodels.{AnswerRow, AnswerSection, CYAViewModel, Message}
import views.html.checkYourAnswers

import java.time.LocalDate

class CheckYourAnswersControllerSpec extends ControllerSpecBase with ControllerAllowChangeBehaviour {

  import CheckYourAnswersControllerSpec._

  implicit val countryOptions: FakeCountryOptions = new FakeCountryOptions()

  private val view = injector.instanceOf[checkYourAnswers]

  private def controller(dataRetrievalAction: DataRetrievalAction,
                         allowChangeHelper: AllowChangeHelper = ach): CheckYourAnswersController =
    new CheckYourAnswersController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      FakeUserAnswersService,
      countryOptions,
      allowChangeHelper,
      controllerComponents,
      view
    )

  private def viewAsString(mode: Mode,
                           answerSection: (Mode, OptionalSchemeReferenceNumber) => Seq[AnswerSection],
                           href: Call,
                           srn: OptionalSchemeReferenceNumber,
                           title: Message,
                           h1: Message): String =
    viewAsString(mode, answerSection(NormalMode, OptionalSchemeReferenceNumber(srn)), href, OptionalSchemeReferenceNumber(srn), title, h1)

  private def viewAsString(mode: Mode,
                           answerSection: Seq[AnswerSection],
                           href: Call,
                           srn: OptionalSchemeReferenceNumber,
                           title: Message,
                           h1: Message): String =
    view(
      CYAViewModel(
        answerSections = answerSection,
        href = href,
        schemeName = None,
        returnOverview = false,
        hideEditLinks = false,
        srn = srn,
        hideSaveAndContinueButton = false,
        title = title,
        h1 = h1
      )
    )(fakeRequest, messages).toString

  "having set up answer sections" when {
    val request: DataRequest[AnyContent]  = FakeDataRequest(directorAnswers)
    def answerSectionDirector(mode: Mode, srn: OptionalSchemeReferenceNumber): Seq[AnswerSection] =
      Seq(
        AnswerSection(
          None,
          Seq(
            DirectorNameId(Index(0), index)
              .row(routes.DirectorNameController.onPageLoad(Mode.checkMode(mode), Index(0), Index(0), OptionalSchemeReferenceNumber(srn)).url, mode)(request, implicitly),
            DirectorDOBId(Index(0), index)
              .row(routes.DirectorDOBController.onPageLoad(Mode.checkMode(mode), Index(0), Index(0), OptionalSchemeReferenceNumber(srn)).url, mode)(request, implicitly),
            DirectorHasNINOId(Index(0), index).row(routes.DirectorHasNINOController.onPageLoad(Mode.checkMode(mode), Index(0), Index(0), OptionalSchemeReferenceNumber(srn)).url,
                                                mode)(request, implicitly),
            DirectorEnterNINOId(Index(0), index)
              .row(routes.DirectorEnterNINOController.onPageLoad(Mode.checkMode(mode), Index(0), Index(0), OptionalSchemeReferenceNumber(srn)).url, mode)(request, implicitly),
            DirectorNoNINOReasonId(Index(0), index)
              .row(routes.DirectorNoNINOReasonController.onPageLoad(Mode.checkMode(mode), Index(0), Index(0), OptionalSchemeReferenceNumber(srn)).url, mode)(request,
                                                                                                                        implicitly),
            DirectorHasUTRId(Index(0), index).row(routes.DirectorHasUTRController.onPageLoad(Mode.checkMode(mode), Index(0), Index(0), OptionalSchemeReferenceNumber(srn)).url,
                                               mode)(request, implicitly),
            DirectorEnterUTRId(Index(0), index).row(routes.DirectorEnterUTRController.onPageLoad(Mode.checkMode(mode), Index(0), Index(0), OptionalSchemeReferenceNumber(srn)).url,
                                                 mode)(request, implicitly),
            DirectorNoUTRReasonId(Index(0), index)
              .row(routes.DirectorNoUTRReasonController.onPageLoad(Mode.checkMode(mode), Index(0), Index(0), OptionalSchemeReferenceNumber(srn)).url, mode)(request, implicitly),
            DirectorAddressId(Index(0), index).row(routes.DirectorAddressController.onPageLoad(Mode.checkMode(mode), Index(0), Index(0), OptionalSchemeReferenceNumber(srn)).url,
                                                mode)(request, implicitly),
            DirectorAddressYearsId(Index(0), index)
              .row(routes.DirectorAddressYearsController.onPageLoad(Mode.checkMode(mode), Index(0), Index(0), OptionalSchemeReferenceNumber(srn)).url, mode)(request,
                                                                                                                        implicitly),
            DirectorPreviousAddressId(Index(0), index)
              .row(routes.DirectorPreviousAddressController.onPageLoad(Mode.checkMode(mode), Index(0), Index(0), OptionalSchemeReferenceNumber(srn)).url, mode)(request,
                                                                                                                           implicitly),
            DirectorEmailId(Index(0), index)
              .row(routes.DirectorEmailController.onPageLoad(Mode.checkMode(mode), Index(0), Index(0), OptionalSchemeReferenceNumber(srn)).url, mode)(request, implicitly),
            DirectorPhoneNumberId(Index(0), index)
              .row(routes.DirectorPhoneNumberController.onPageLoad(Mode.checkMode(mode), Index(0), Index(0), OptionalSchemeReferenceNumber(srn)).url, mode)(request, implicitly)
          ).flatten
        )
      )

    "onPageLoad" must {

      "return OK and display all the answers" in {
        val result = controller(directorAnswers.dataRetrievalAction).onPageLoad(Index(0), Index(0), NormalMode, EmptyOptionalSchemeReferenceNumber)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(NormalMode,
                                                    answerSectionDirector _,
                                                    href(NormalMode, EmptyOptionalSchemeReferenceNumber, 0),
          EmptyOptionalSchemeReferenceNumber,
                                                    title = Message("checkYourAnswers.hs.heading"),
                                                    h1 = Message("checkYourAnswers.hs.heading"))
      }

      "return OK and display all given answers for UpdateMode" in {
        val result = controller(directorAnswers.dataRetrievalAction).onPageLoad(Index(0), Index(0), UpdateMode, OptionalSchemeReferenceNumber(Some(SchemeReferenceNumber("srn"))))(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(
          UpdateMode,
          answerSectionDirector(UpdateMode, OptionalSchemeReferenceNumber(Some(SchemeReferenceNumber("srn")))),
          href(UpdateMode, OptionalSchemeReferenceNumber(Some(SchemeReferenceNumber("srn"))), 0),
          OptionalSchemeReferenceNumber(Some(SchemeReferenceNumber("srn"))),
          title = Message("messages__detailsFor", Message("messages__theDirector")),
          h1 = Message("messages__detailsFor", "First Last")
        )
      }

      behave like changeableController(
        controller(directorAnswers.dataRetrievalAction, _: AllowChangeHelper)
          .onPageLoad(Index(0), Index(0), NormalMode, EmptyOptionalSchemeReferenceNumber)(request)
      )
    }
  }
}

object CheckYourAnswersControllerSpec extends SpecBase {
  val index                                                          = Index(0)
  val schemeName                                                     = "test scheme name"
  def href(mode: Mode, srn: OptionalSchemeReferenceNumber, companyIndex: Int): Call = AddCompanyDirectorsController.onPageLoad(mode, OptionalSchemeReferenceNumber(srn), companyIndex)
  val name                                                           = "First Name"

  val directorPersonDetails = PersonName("first name", "last name", false)

  val directorAnswers: UserAnswers = UserAnswers()
    .set(DirectorNameId(Index(0), index))(PersonName("First", "Last"))
    .flatMap(
      _.set(DirectorEnterNINOId(Index(0), index))(ReferenceValue("AB100100A")).flatMap(
        _.set(DirectorEnterUTRId(Index(0), index))(ReferenceValue("1234567890"))
          .flatMap(_.set(DirectorAddressId(Index(0), index))(Address("Address 1", "Address 2", None, None, None, "GB")))
          .flatMap(_.set(DirectorAddressYearsId(Index(0), index))(AddressYears.UnderAYear))
          .flatMap(
            _.set(DirectorPreviousAddressId(Index(0), index))(Address("Previous Address 1", "Previous Address 2", None, None, None, "GB")))
          .flatMap(
            _.set(DirectorEmailId(Index(0), index))("test@test.com").flatMap(
              _.set(DirectorPhoneNumberId(Index(0), index))("123456789").flatMap(
                _.set(DirectorDOBId(Index(0), index))(LocalDate.now())
              )
            )
          )
      )
    )
    .asOpt
    .value

  val displayNewNinoAnswerRowWithAdd = Seq(
    AnswerSection(
      Some("messages__director__cya__details_heading"),
      Seq(
        AnswerRow("messages__director__cya__name", Seq("first name last name"), false, None),
        AnswerRow(
          messages("messages__director__cya__dob", directorPersonDetails.fullName),
          Seq(DateHelper.formatDate(LocalDate.now())),
          answerIsMessageKey = false,
          None
        ),
        AnswerRow(
          messages("messages__common__nino", directorPersonDetails.fullName),
          Seq("site.not_entered"),
          answerIsMessageKey = true,
          Some(
            Link(
              "site.add",
              routes.DirectorEnterNINOController.onPageLoad(Mode.checkMode(UpdateMode), Index(0), Index(0), OptionalSchemeReferenceNumber(Some(SchemeReferenceNumber("srn")))).url,
              Some(messages("messages__visuallyhidden__dynamic_nino", directorPersonDetails.fullName))
            ))
        )
      )
    ),
    AnswerSection(Some("messages__director__cya__contact__details_heading"), Seq())
  )
}
