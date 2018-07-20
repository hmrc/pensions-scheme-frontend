package forms.app

import forms.behaviours.FormBehaviours
import models.{Field, Required}
import models.app.UserResearchDetails

class UserResearchDetailsFormProviderSpec extends FormBehaviours {

  val validData: Map[String, String] = Map(
    "field1" -> "value 1",
    "field2" -> "value 2"
  )

  val form = new UserResearchDetailsFormProvider()()

  "UserResearchDetails form" must {
    behave like questionForm(UserResearchDetails("value 1", "value 2"))

    behave like formWithMandatoryTextFields(
      Field("field1", Required -> "messages__userResearchDetails__error__field1_required"),
      Field("field2", Required -> "messages__userResearchDetails__error__field2_required")
    )
  }
}
