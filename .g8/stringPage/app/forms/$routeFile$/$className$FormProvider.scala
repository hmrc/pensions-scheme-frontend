package forms.$routeFile$

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form
import forms.FormErrorHelper

class $className$FormProvider @Inject() extends FormErrorHelper with Mappings {

  def apply(): Form[String] =
    Form(
      "value" -> text("$className;format="decap"$.error.required")
    )
}
