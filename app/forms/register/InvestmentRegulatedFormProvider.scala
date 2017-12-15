package forms.register

import javax.inject.Inject
import forms.FormErrorHelper
import forms.mappings.Mappings
import play.api.data.Form

class InvestmentRegulatedFormProvider @Inject() extends FormErrorHelper with Mappings {

  def apply(): Form[Boolean] =
    Form(
      "value" -> boolean("investmentRegulated.error.required")
    )
}
