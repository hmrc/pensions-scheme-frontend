package views.register

import play.api.data.Form
import controllers.register.routes
import forms.register.InvestmentRegulatedFormProvider
import views.behaviours.YesNoViewBehaviours
import models.NormalMode
import views.html.register.investmentRegulated

class InvestmentRegulatedViewSpec extends YesNoViewBehaviours {

  val messageKeyPrefix = "investmentRegulated"

  val form = new InvestmentRegulatedFormProvider()()

  def createView = () => investmentRegulated(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  def createViewUsingForm = (form: Form[_]) => investmentRegulated(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  "InvestmentRegulated view" must {

    behave like normalPage(createView, messageKeyPrefix)

    behave like yesNoPage(createViewUsingForm, messageKeyPrefix, routes.InvestmentRegulatedController.onSubmit(NormalMode).url)
  }
}
