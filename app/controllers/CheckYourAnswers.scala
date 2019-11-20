package controllers
import models.Mode
import play.api.i18n.Messages
import viewmodels.Message

trait CheckYourAnswers {
  def titleCompanyDetails(mode:Mode)(implicit messages:Messages):Message =
    if (mode.isSubscription) Message("checkYourAnswers.hs.title") else Message("messages__detailsFor", Message("messages__theCompany").resolve)

  def titleCompanyContactDetails(mode:Mode)(implicit messages:Messages):Message =
    if (mode.isSubscription) Message("checkYourAnswers.hs.title") else Message("messages__contactDetailsFor", Message("messages__theCompany").resolve)

  def titleCompanyAddressDetails(mode:Mode)(implicit messages:Messages):Message =
    if (mode.isSubscription) Message("checkYourAnswers.hs.title") else Message("messages__addressFor", Message("messages__theCompany").resolve)

  def headingDetails(mode:Mode, name: => String)(implicit messages:Messages):Message = {
    if (mode.isSubscription) Message("checkYourAnswers.hs.heading") else Message("messages__detailsFor", name)
  }

  def headingAddressDetails(mode:Mode, name: => String)(implicit messages:Messages):Message = {
    if (mode.isSubscription) Message("checkYourAnswers.hs.heading") else Message("messages__addressFor", name)
  }

  def headingContactDetails(mode:Mode, name: => String)(implicit messages:Messages):Message = {
    if (mode.isSubscription) Message("checkYourAnswers.hs.heading") else Message("messages__contactDetailsFor", name)
  }
}
