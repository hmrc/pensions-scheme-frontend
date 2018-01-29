package forms.$routeFile;format="packaged"$

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form

class $className$FormProvider @Inject() extends Mappings {

  def apply(): Form[String] =
    Form(
      "value" -> text("messages__$className;format="decap"$__error__required")
    )
}
