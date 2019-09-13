package controllers.dateOfBirth

import config.FrontendAppConfig
import controllers.Retrievals
import identifiers.TypedIdentifier
import models.Mode
import models.person.PersonName
import models.requests.DataRequest
import navigators.Navigator
import org.joda.time.LocalDate
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{AnyContent, Result}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import viewmodels.dateOfBirth.DateOfBirthViewModel
import views.html.register.DOB

import scala.concurrent.{ExecutionContext, Future}

trait DateOfBirthController extends FrontendController with Retrievals with I18nSupport {
  protected implicit def ec: ExecutionContext

  protected def appConfig: FrontendAppConfig

  protected def userAnswersService: UserAnswersService

  protected def navigator: Navigator

  protected val form: Form[LocalDate]

  protected def get(dobId: TypedIdentifier[LocalDate],
                    personNameId: TypedIdentifier[PersonName],
                    viewModel: DateOfBirthViewModel,
                    mode: Mode)
                   (implicit request: DataRequest[AnyContent]): Future[Result] = {

    val preparedForm = request.userAnswers.get(dobId) match {
      case Some(value) => form.fill(value)
      case None => form
    }

    personNameId.retrieve.right.map {
      personName =>
        Future.successful(Ok(
          DOB(appConfig, preparedForm, mode, existingSchemeName, personName.fullName, viewModel))
        )
    }
  }

}
