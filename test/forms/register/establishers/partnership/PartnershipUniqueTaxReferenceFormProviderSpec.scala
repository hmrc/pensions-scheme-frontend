package forms.register.establishers.partnership

import forms.behaviours.UtrBehaviour

class PartnershipUniqueTaxReferenceFormProviderSpec extends UtrBehaviour {

  val requiredKey = "messages__error__has_ct_utr_establisher"
  val requiredUtrKey = "messages__error__ct_utr"
  val requiredReasonKey = "messages__error__no_ct_utr_establisher"
  val invalidUtrKey = "messages__error__ct_utr_invalid"
  val maxLengthReasonKey = "messages__error__no_sautr_length"
  val invalidReasonKey = "messages__error__no_sautr_invalid"

  val formProvider = new PartnershipUniqueTaxReferenceFormProvider()

  "CompanyUniqueTaxReference form" must {

    behave like formWithUtr(
      formProvider(),
      requiredKey,
      requiredUtrKey,
      requiredReasonKey,
      invalidUtrKey,
      maxLengthReasonKey,
      invalidReasonKey
    )
  }
}

