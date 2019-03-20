package utils

import identifiers.IsBeforeYouStartCompleteId
import models.{Mode, NormalMode}
import viewmodels.{Link, SchemeDetailsTaskList, SchemeDetailsTaskListSection}

trait TaskList {

  def beforeYouStartSection(userAnswers: UserAnswers): SchemeDetailsTaskListSection = {
    val link = userAnswers.get(IsBeforeYouStartCompleteId) match {
      case Some(true) => Link("", controllers.routes.CheckYourAnswersBeforeYouStartController.onPageLoad().url)
      case _ => Link("", controllers.routes.SchemeNameController.onPageLoad(NormalMode).url)
    }
    SchemeDetailsTaskListSection(userAnswers.get(IsBeforeYouStartCompleteId), link, None)
  }

  def getTaskList: SchemeDetailsTaskList

  def apply(mode: Mode): TaskList = {
    if(mode == NormalMode){
      new NormalTaskListImpl
    } else {
      new UpdateTaskListImpl
    }
  }
}

class NormalTaskListImpl extends TaskList {
  override def getTaskList: SchemeDetailsTaskList = {
    beforeYouStartSection(UserAnswers())
    ???
  }
}

class UpdateTaskListImpl extends TaskList {
  override def getTaskList: SchemeDetailsTaskList = ???
}
