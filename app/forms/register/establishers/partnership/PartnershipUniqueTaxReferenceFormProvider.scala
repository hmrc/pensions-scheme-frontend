package forms.register.establishers.partnership

import javax.inject.Inject

import forms.mappings.UtrMapping
import models.UniqueTaxReference
import play.api.data.Form

class PartnershipUniqueTaxReferenceFormProvider @Inject()() extends UtrMapping {

  def apply(): Form[UniqueTaxReference] = Form(
    "uniqueTaxReference" -> uniqueTaxReferenceMapping(
      requiredKey = "messages__error__has_ct_utr_establisher",
      requiredUtrKey = "messages__error__ct_utr",
      requiredReasonKey = "messages__error__no_ct_utr_establisher",
      invalidUtrKey = "messages__error__ct_utr_invalid"
    )
  )
}
