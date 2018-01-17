package views.register.establishers.company

import play.api.data.Form
import controllers.register.establishers.company.routes
import forms.register.establishers.company.CompanyContactDetailsFormProvider
import models.NormalMode
import models.CompanyContactDetails
import views.behaviours.QuestionViewBehaviours
import views.html.register.establishers.company.companyContactDetails

class CompanyContactDetailsViewSpec extends QuestionViewBehaviours[CompanyContactDetails] {

  val messageKeyPrefix = "companyContactDetails"

  override val form = new CompanyContactDetailsFormProvider()()

  def createView = () => companyContactDetails(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  def createViewUsingForm = (form: Form[_]) => companyContactDetails(frontendAppConfig, form, NormalMode)(fakeRequest, messages)


  "CompanyContactDetails view" must {

    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithTextFields(createViewUsingForm, messageKeyPrefix, routes.CompanyContactDetailsController.onSubmit(NormalMode).url, "field1", "field2")
  }
}
