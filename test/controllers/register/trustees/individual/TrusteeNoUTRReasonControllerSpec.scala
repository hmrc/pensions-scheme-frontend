package controllers.register.trustees.individual

import controllers.ControllerSpecBase
import controllers.register.establishers.company.director.DirectorNoUTRReasonControllerSpec.formProvider
import forms.ReasonFormProvider
import models.{Index, NormalMode}
import play.api.data.Form
import play.api.test.Helpers._
import viewmodels.{Message, ReasonViewModel}
import views.html.reason

class TrusteeNoUTRReasonControllerSpec extends ControllerSpecBase {

  private val formProvider = new ReasonFormProvider()
  private val form = formProvider("messages__reason__error_utrRequired", "test director name")

  private val postCall = controllers.register.trustees.individual.routes.TrusteeNoUTRReasonController.onSubmit(NormalMode, Index(0), None)
  private val viewModel = ReasonViewModel(
    postCall = postCall,
    title = Message("messages__noGenericUtr__title", Message("messages__theTrustee")),
    heading = Message("messages__noGenericUtr__heading", "first last"),
    srn = None
  )

  private def viewAsString(form: Form[_] = form) = reason(frontendAppConfig, form, viewModel, None)(fakeRequest, messages).toString

  "TrusteeNoUTRReasonController" must {
    "return OK and the correct view for a GET" in {
      val app = applicationBuilder(getEmptyData).build()

      val controller = app.injector.instanceOf[TrusteeNoUTRReasonController]

      val result = controller.onPageLoad(NormalMode, Index(0), None)(fakeRequest)

      status(result) mustBe OK

      contentAsString(result) mustBe viewAsString()

      app.stop()
    }

    "return OK and the correct view for a GET where valid reason given" in {
      val validData =

      val app = applicationBuilder(getMandatoryTrustee).build()

      val controller = app.injector.instanceOf[TrusteeNoUTRReasonController]

      val result = controller.onPageLoad(NormalMode, Index(0), None)(fakeRequest)

      status(result) mustBe OK

      contentAsString(result) mustBe viewAsString()

      app.stop()
    }

    "redirect to the next page when valid data is submitted" in {


    }

    "return a Bad Request when invalid data is submitted" in {


    }
  }

}
