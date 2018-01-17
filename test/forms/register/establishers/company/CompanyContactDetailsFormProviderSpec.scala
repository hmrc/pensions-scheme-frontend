package forms.register.establishers.company

import forms.behaviours.FormBehaviours
import models.{Field, Required}
import models.CompanyContactDetails

class CompanyContactDetailsFormProviderSpec extends FormBehaviours {

  val validData: Map[String, String] = Map(
    "field1" -> "value 1",
    "field2" -> "value 2"
  )

  val form = new CompanyContactDetailsFormProvider()()

  "CompanyContactDetails form" must {
    behave like questionForm(CompanyContactDetails("value 1", "value 2"))

    behave like formWithMandatoryTextFields(
      Field("field1", Required -> "companyContactDetails.error.field1.required"),
      Field("field2", Required -> "companyContactDetails.error.field2.required")
    )
  }
}
