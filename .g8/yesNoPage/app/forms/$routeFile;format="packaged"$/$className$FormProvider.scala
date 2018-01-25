package forms.$routeFile;format="packaged"$

import javax.inject.Inject
import forms.mappings.Mappings
import play.api.data.Form

class $className$FormProvider @Inject() extends Mappings {

  def apply(): Form[Boolean] =
    Form(
      "value" -> boolean("messages__$className;format="decap"$__error__required")
    )
}
