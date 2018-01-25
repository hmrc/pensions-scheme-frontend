package views.register.establishers.individual

import play.api.data.Form
import controllers.register.establishers.individual.routes
import forms.register.establishers.individual.ManualAddressFormProvider
import models.NormalMode
import models.register.establishers.individual.ManualAddress
import views.behaviours.QuestionViewBehaviours
import views.html.register.establishers.individual.manualAddress

class ManualAddressViewSpec extends QuestionViewBehaviours[ManualAddress] {

  val messageKeyPrefix = "manualAddress"

  override val form = new ManualAddressFormProvider()()

  def createView = () => manualAddress(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  def createViewUsingForm = (form: Form[_]) => manualAddress(frontendAppConfig, form, NormalMode)(fakeRequest, messages)


  "ManualAddress view" must {

    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"))

    behave like pageWithTextFields(createViewUsingForm, messageKeyPrefix, routes.ManualAddressController.onSubmit(NormalMode).url, "field1", "field2")
  }
}
