package controllers.register.trustees.company

import play.api.data.Form
import play.api.libs.json.JsBoolean
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.FakeNavigator
import connectors.FakeDataCacheConnector
import controllers.actions._
import play.api.test.Helpers._
import models.NormalMode
import views.html.register.trustees.company.addressList
import controllers.ControllerSpecBase

class CompanyAddressListControllerSpec extends ControllerSpecBase {

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new AddressListController(frontendAppConfig, messagesApi, FakeAuthAction,
      dataRetrievalAction, new DataRequiredActionImpl)

  def viewAsString() = addressList(frontendAppConfig)(fakeRequest, messages).toString

  "AddressList Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }
  }
}




