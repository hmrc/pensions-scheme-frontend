package forms.$routeFile;format="packaged"$

import forms.FormSpec

class $className$FormProviderSpec extends FormSpec {

  val requiredKey = "messages__$className;format="decap"$__error__required"

  "$className$ Form" must {

    val formProvider = new $className$FormProvider()

    "bind a string" in {
      val form = formProvider().bind(Map("value" -> "answer"))
      form.get shouldBe "answer"
    }

    "fail to bind a blank value" in {
      val expectedError = error("value", requiredKey)
      checkForError(formProvider(), Map("value" -> ""), expectedError)
    }

    "fail to bind when value is omitted" in {
      val expectedError = error("value", requiredKey)
      checkForError(formProvider(), emptyForm, expectedError)
    }
  }
}
