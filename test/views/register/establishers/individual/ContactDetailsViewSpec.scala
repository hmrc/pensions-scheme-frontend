package views.register.establishers.individual

import play.api.data.Form
import controllers.register.establishers.individual.routes
import forms.register.establishers.individual.ContactDetailsFormProvider
import models.NormalMode
import models.ContactDetails
import views.behaviours.QuestionViewBehaviours
import views.html.register.establishers.individual.contactDetails

class ContactDetailsViewSpec extends QuestionViewBehaviours[ContactDetails] {

  val messageKeyPrefix = "contactDetails"

  override val form = new ContactDetailsFormProvider()()

  def createView = () => contactDetails(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  def createViewUsingForm = (form: Form[_]) => contactDetails(frontendAppConfig, form, NormalMode)(fakeRequest, messages)


  "ContactDetails view" must {

    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithTextFields(createViewUsingForm, messageKeyPrefix, routes.ContactDetailsController.onSubmit(NormalMode).url, "field1", "field2")
  }
}
