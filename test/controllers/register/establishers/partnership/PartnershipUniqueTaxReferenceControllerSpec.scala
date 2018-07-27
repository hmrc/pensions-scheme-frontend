package controllers.register.establishers.partnership

import connectors.FakeDataCacheConnector
import controllers.ControllerSpecBase
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAuthAction}
import forms.register.establishers.partnership.PartnershipUniqueTaxReferenceFormProvider
import models.{Index, NormalMode, UniqueTaxReference}
import play.api.data.Form
import play.api.mvc.Call
import play.api.test.Helpers.{contentAsString, status}
import utils.FakeNavigator
import views.html.partnershipUniqueTaxReference

class PartnershipUniqueTaxReferenceControllerSpec extends ControllerSpecBase {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val firstIndex = Index(0)
  val formProvider = new PartnershipUniqueTaxReferenceFormProvider()
  val form: Form[UniqueTaxReference] = formProvider()
  val partnershipName = "test partnership name"

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisherPartnership): PartnershipUniqueTaxReferenceController =
    new PartnershipUniqueTaxReferenceController(
      frontendAppConfig,
      messagesApi,
      FakeDataCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider
    )

  def viewAsString(form: Form[_] = form): String =
    partnershipUniqueTaxReference(
      frontendAppConfig,
      form,
      NormalMode,
      firstIndex,
      partnershipName
    )(fakeRequest, messages).toString

  "PartnershipUniqueTaxReference Controller" must {

    "return OK and the correct view for a GET when company name is present" in {
      val result = controller().onPageLoad(NormalMode, firstIndex)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }
  }
}