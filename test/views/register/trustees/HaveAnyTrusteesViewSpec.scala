package views.register.trustees

import play.api.data.Form
import controllers.register.trustees.routes
import forms.register.trustees.HaveAnyTrusteesFormProvider
import views.behaviours.YesNoViewBehaviours
import models.NormalMode
import views.html.register.trustees.haveAnyTrustees

class HaveAnyTrusteesViewSpec extends YesNoViewBehaviours {

  val messageKeyPrefix = "haveAnyTrustees"

  val form = new HaveAnyTrusteesFormProvider()()

  def createView = () => haveAnyTrustees(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  def createViewUsingForm = (form: Form[_]) => haveAnyTrustees(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  "HaveAnyTrustees view" must {

    behave like normalPage(createView, messageKeyPrefix, messages("messages__haveAnyTrustees__heading"))

    behave like pageWithBackLink(createView)

    behave like yesNoPage(createViewUsingForm, messageKeyPrefix, routes.HaveAnyTrusteesController.onSubmit(NormalMode).url)
  }
}
