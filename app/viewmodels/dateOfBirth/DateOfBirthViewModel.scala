package viewmodels.dateOfBirth

import play.api.mvc.Call

case class DateOfBirthViewModel(
                                 postCall: Call,
                                 srn: Option[String] = None,
                                 token: String
                               )
