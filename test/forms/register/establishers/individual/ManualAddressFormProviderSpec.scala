package forms.register.establishers.individual

import forms.behaviours.FormBehaviours
import models.{Field, Required}
import models.register.establishers.individual.ManualAddress

class ManualAddressFormProviderSpec extends FormBehaviours {

  val validData: Map[String, String] = Map(
    "field1" -> "value 1",
    "field2" -> "value 2"
  )

  val form = new ManualAddressFormProvider()()

  "ManualAddress form" must {
    behave like questionForm(ManualAddress("value 1", "value 2"))

    behave like formWithMandatoryTextFields(
      Field("field1", Required -> "manualAddress.error.field1.required"),
      Field("field2", Required -> "manualAddress.error.field2.required")
    )
  }
}
