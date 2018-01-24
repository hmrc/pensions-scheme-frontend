#!/bin/bash

echo "Applying migration $className;format="snake"$"

echo "Adding routes to $routeFile$.routes"

echo "" >> ../conf/$routeFile$.routes
echo "GET        /$className;format="decap"$                       controllers.$routeFile$.$className$Controller.onPageLoad(mode: Mode = NormalMode)" >> ../conf/$routeFile$.routes
echo "POST       /$className;format="decap"$                       controllers.$routeFile$.$className$Controller.onSubmit(mode: Mode = NormalMode)" >> ../conf/$routeFile$.routes

echo "GET        /change$className$                       controllers.$routeFile$.$className$Controller.onPageLoad(mode: Mode = CheckMode)" >> ../conf/$routeFile$.routes
echo "POST       /change$className$                       controllers.$routeFile$.$className$Controller.onSubmit(mode: Mode = CheckMode)" >> ../conf/$routeFile$.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo messages__"$className;format="decap"$__title = $className;format="decap"$" >> ../conf/messages.en
echo messages__"$className;format="decap"$__heading = $className;format="decap"$" >> ../conf/messages.en
echo messages__"$className;format="decap"$_field1 = Field 1" >> ../conf/messages.en
echo messages__"$className;format="decap"$_field2 = Field 2" >> ../conf/messages.en
echo messages__"$className;format="decap"$_checkYourAnswersLabel = $className;format="decap"$" >> ../conf/messages.en
echo messages__error__"$className;format="decap"$_field1_required = Please give an answer for field1" >> ../conf/messages.en
echo messages__error__"$className;format="decap"$_field2_required = Please give an answer for field2" >> ../conf/messages.en

echo "Adding helper line into UserAnswers"
awk '/class/ {\
     print;\
     print "  def $className;format="decap"$: Option[$className$] = cacheMap.getEntry[$className$]($className$Id.toString)";\
     print "";\
     next }1' ../app/utils/UserAnswers.scala > tmp && mv tmp ../app/utils/UserAnswers.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def $className;format="decap"$: Option[AnswerRow] = userAnswers.$className;format="decap"$ map {";\
     print "    x => AnswerRow(\"$className;format="decap"$.checkYourAnswersLabel\", s\"\${x.field1} \${x.field2}\", false, routes.$className$Controller.onPageLoad(CheckMode).url)";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration $className;format="snake"$ completed"
