package forms.register.establishers.individual

import forms.behaviours.FormBehaviours
import models.{Field, Required}
import models.ContactDetails

class ContactDetailsFormProviderSpec extends FormBehaviours {

  val validData: Map[String, String] = Map(
    "field1" -> "value 1",
    "field2" -> "value 2"
  )

  val form = new ContactDetailsFormProvider()()

  "ContactDetails form" must {
    behave like questionForm(ContactDetails("value 1", "value 2"))

    behave like formWithMandatoryTextFields(
      Field("field1", Required -> "contactDetails.error.field1.required"),
      Field("field2", Required -> "contactDetails.error.field2.required")
    )
  }
}
