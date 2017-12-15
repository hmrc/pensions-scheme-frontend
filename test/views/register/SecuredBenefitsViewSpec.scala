package views.register

import play.api.data.Form
import controllers.register.routes
import forms.register.SecuredBenefitsFormProvider
import views.behaviours.YesNoViewBehaviours
import models.NormalMode
import views.html.register.securedBenefits

class SecuredBenefitsViewSpec extends YesNoViewBehaviours {

  val messageKeyPrefix = "securedBenefits"

  val form = new SecuredBenefitsFormProvider()()

  def createView = () => securedBenefits(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  def createViewUsingForm = (form: Form[_]) => securedBenefits(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  "SecuredBenefits view" must {

    behave like normalPage(createView, messageKeyPrefix)

    behave like yesNoPage(createViewUsingForm, messageKeyPrefix, routes.SecuredBenefitsController.onSubmit(NormalMode).url)
  }
}
