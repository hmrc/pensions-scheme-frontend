package forms.$routeFile;format="packaged"$

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms._
import models.$routeFile$.$className$

class $className$FormProvider @Inject() extends Mappings {

   def apply(): Form[$className$] = Form(
     mapping(
      "field1" -> text("messages__$className;format="decap"$__error__field1_required"),
      "field2" -> text("messages__$className;format="decap"$__error__field2_required")
    )($className$.apply)($className$.unapply)
   )
 }
