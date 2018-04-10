#!/bin/bash

echo "Applying migration DeclarationDuties"

echo "Adding routes to conf/register.routes"

echo "" >> ../conf/app.routes
echo "GET        /declarationDuties               controllers.register.DeclarationDutiesController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/register.routes
echo "POST       /declarationDuties               controllers.register.DeclarationDutiesController.onSubmit(mode: Mode = NormalMode)" >> ../conf/register.routes

echo "GET        /changeDeclarationDuties               controllers.register.DeclarationDutiesController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/register.routes
echo "POST       /changeDeclarationDuties               controllers.register.DeclarationDutiesController.onSubmit(mode: Mode = CheckMode)" >> ../conf/register.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "messages__declarationDuties__title = declarationDuties" >> ../conf/messages.en
echo "messages__declarationDuties__heading = declarationDuties" >> ../conf/messages.en
echo "messages__declarationDuties__option1 = declarationDuties" Option 1 >> ../conf/messages.en
echo "messages__declarationDuties__option2 = declarationDuties" Option 2 >> ../conf/messages.en
echo "messages__declarationDuties__checkYourAnswersLabel = declarationDuties" >> ../conf/messages.en
echo "messages__declarationDuties__error__required = Please give an answer for declarationDuties" >> ../conf/messages.en

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def declarationDuties: Option[AnswerRow] = userAnswers.get(identifiers.register.DeclarationDutiesId) map {";\
     print "    x => AnswerRow(\"messages__declarationDuties__checkYourAnswersLabel\", Seq(s\"declarationDuties.$x\"), true, controllers.register.routes.DeclarationDutiesController.onPageLoad(CheckMode).url)";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration DeclarationDuties completed"
