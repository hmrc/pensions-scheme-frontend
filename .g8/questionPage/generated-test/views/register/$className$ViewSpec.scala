package views.$routeFile$

import play.api.data.Form
import controllers.$routeFile$.routes
import forms.$routeFile$.$className$FormProvider
import models.NormalMode
import models.$className$
import views.behaviours.QuestionViewBehaviours
import views.html.$routeFile$.$className;format="decap"$

class $className$ViewSpec extends QuestionViewBehaviours[$className$] {

  val messageKeyPrefix = "$className;format="decap"$"

  override val form = new $className$FormProvider()()

  def createView = () => $className;format="decap"$(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  def createViewUsingForm = (form: Form[_]) => $className;format="decap"$(frontendAppConfig, form, NormalMode)(fakeRequest, messages)


  "$className$ view" must {

    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithTextFields(createViewUsingForm, messageKeyPrefix, routes.$className$Controller.onSubmit(NormalMode).url, "field1", "field2")
  }
}
