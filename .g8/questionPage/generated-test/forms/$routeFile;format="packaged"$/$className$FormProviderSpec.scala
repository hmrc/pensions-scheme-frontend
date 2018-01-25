package forms.$routeFile;format="packaged"$

import forms.behaviours.FormBehaviours
import models.{Field, Required}
import models.$routeFile$.$className$

class $className$FormProviderSpec extends FormBehaviours {

  val validData: Map[String, String] = Map(
    "field1" -> "value 1",
    "field2" -> "value 2"
  )

  val form = new $className$FormProvider()()

  "$className$ form" must {
    behave like questionForm($className$("value 1", "value 2"))

    behave like formWithMandatoryTextFields(
      Field("field1", Required -> "messages__$className;format="decap"$__error__field1_required"),
      Field("field2", Required -> "messages__$className;format="decap"$__error__field2_required")
    )
  }
}
