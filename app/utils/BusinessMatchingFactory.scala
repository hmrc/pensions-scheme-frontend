package utils

import javax.inject.Inject

import identifiers.register.SchemeDetailsId
import models.register.SchemeDetails
import models.requests.DataRequest
import play.api.mvc.AnyContent

import scala.concurrent.Future

class BusinessMatchingFactory @Inject()(

                                       ){

  def retrieveSchemeName(implicit request: DataRequest[AnyContent]): Option[SchemeDetails] =
    request.userAnswers.get(SchemeDetailsId)

  def retrieveSchemeAdministrator(implicit request: DataRequest[AnyContent]): Future[Option[String]] = {

    request.externalId

    ???
  }

}
