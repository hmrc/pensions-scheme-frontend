/*
 * Copyright 2018 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package navigators

import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.company._
import models.{CheckMode, CompanyDetails, NormalMode}
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.Json
import utils.UserAnswers
import config.FrontendAppConfig
import identifiers.register.SchemeDetailsId
import identifiers.register.establishers.company.director.DirectorDetailsId
import models.register.{SchemeDetails, SchemeType}
import models.register.establishers.company.director.DirectorDetails
import org.joda.time.LocalDate
import org.scalatestplus.play.guice._
import play.api.inject.Injector

class EstablishersCompanyNavigatorSpec extends WordSpec with MustMatchers with PropertyChecks with GuiceOneAppPerSuite with OptionValues {

  def injector: Injector = app.injector

  def frontendAppConfig: FrontendAppConfig = injector.instanceOf[FrontendAppConfig]

  val navigator = new EstablishersCompanyNavigator(frontendAppConfig)
  val emptyAnswers = UserAnswers(Json.obj())

  private val companyName = "MyCo Ltd"

  private val johnDoe = DirectorDetails("John", None, "Doe", new LocalDate(1862, 6, 9))

  private val maxNoOfDirectors = frontendAppConfig.maxDirectors

  private def validData(directors: DirectorDetails*) = {
    Json.obj(
      EstablishersId.toString -> Json.arr(
        Json.obj(
          CompanyDetailsId.toString -> CompanyDetails(companyName, None, None),
          "director" -> directors.map(d => Json.obj(DirectorDetailsId.toString -> Json.toJson(d)))
        )
      )
    )
  }

  "NormalMode" when {

    ".nextPage(CompanyDetails)" must {
      "return a `Call` to `CompanyRegistrationNumber` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(CompanyDetailsId(index), NormalMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.company.routes.CompanyRegistrationNumberController.onPageLoad(NormalMode, index)
        }
      }
    }
    ".nextPage(CompanyRegistrationNumber)" must {
      "return a 'Call' to 'UTR' page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(CompanyRegistrationNumberId(index), NormalMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.company.routes.CompanyUniqueTaxReferenceController.onPageLoad(NormalMode, index)
        }
      }
    }
    ".nextPage(CompanyUniqueTaxReference)" must {
      "return a 'Call' to 'Address Post Code' page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(CompanyUniqueTaxReferenceId(index), NormalMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.company.routes.CompanyPostCodeLookupController.onPageLoad(NormalMode, index)
        }
      }
    }
    ".nextPage(CompanyAddressPostCode)" must {
      "return a 'Call' to 'Address Picker' page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(CompanyPostCodeLookupId(index), NormalMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.company.routes.CompanyAddressListController.onPageLoad(NormalMode, index)
        }
      }
    }
    ".nextPage(CompanyAddressList)" must {
      "return a 'Call' to 'Address Manual' page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(CompanyAddressListId(index), NormalMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.company.routes.CompanyAddressController.onPageLoad(NormalMode, index)
        }
      }
    }
    ".nextPage(CompanyAddress)" must {
      "return a 'Call' to 'Address Years' page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(CompanyAddressId(index), NormalMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.company.routes.CompanyAddressYearsController.onPageLoad(NormalMode, index)
        }
      }
    }
    ".nextPage(CompanyAddressYears)" must {
      "return a 'Call' to 'Previous Address Postcode' page when 'CompanyAddressYears' is 'LessThanOneYear'" in {
        val answers = UserAnswers(Json.obj(
          EstablishersId.toString -> Json.arr(
            Json.obj(
              CompanyAddressYearsId.toString ->
                "under_a_year"
            )
          )
        ))
        val result = navigator.nextPage(CompanyAddressYearsId(0), NormalMode)(answers)
        result mustEqual controllers.register.establishers.company.routes.CompanyPreviousAddressPostcodeLookupController.onPageLoad(NormalMode, 0)
      }

      "return a 'Call' to 'Company Contact Details' page when 'AddressYears' is 'MoreThanOneYear'" in {
        val answers = UserAnswers(Json.obj(
          EstablishersId.toString -> Json.arr(
            Json.obj(
              CompanyAddressYearsId.toString ->
                "over_a_year"
            )
          )
        ))
        val result = navigator.nextPage(CompanyAddressYearsId(0), NormalMode)(answers)
        result mustEqual controllers.register.establishers.company.routes.CompanyContactDetailsController.onPageLoad(NormalMode, 0)
      }
    }
    ".nextPage(CompanyPreviousAddressPostCode)" must {
      "return a 'Call' to 'Previous Address Picker' page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(CompanyPreviousAddressPostcodeLookupId(index), NormalMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.company.routes.CompanyPreviousAddressListController.onPageLoad(NormalMode, index)
        }
      }
    }
    ".nextPage(CompanyPreviousAddressPicker)" must {
      "return a 'Call' to 'Previous Address Manual' page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(CompanyPreviousAddressListId(index), NormalMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.company.routes.CompanyPreviousAddressController.onPageLoad(NormalMode, index)
        }
      }
    }
    ".nextPage(CompanyPreviousAddressManual)" must {
      "return a 'Call' to 'Company Contact Details" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(CompanyPreviousAddressId(index), NormalMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.company.routes.CompanyContactDetailsController.onPageLoad(NormalMode, index)
        }
      }
    }
    ".nextPage(CompanyContactDetails)" must {
      "return a 'Call' to 'Company Check Your Answers" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(CompanyContactDetailsId(index), NormalMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.company.routes.CheckYourAnswersController.onPageLoad(index)
        }
      }
    }

    ".next Page(AddCompanyDirectors)" must {
      "return a call to Director Details when there are no directors" in {
        val result = navigator.nextPage(AddCompanyDirectorsId(0), NormalMode)(emptyAnswers)
        result mustEqual controllers.register.establishers.company.director.routes.DirectorDetailsController.onPageLoad(NormalMode, 0, 0)
      }

      "return a call to Director Details when no of Directors is less than max and AddCompanyDirectorsId is true" in {
        val userAnswers = UserAnswers(validData(johnDoe))
          .set(AddCompanyDirectorsId(0))(true)
          .asOpt
          .value

        val result = navigator.nextPage(AddCompanyDirectorsId(0), NormalMode)(userAnswers)
        result mustEqual controllers.register.establishers.company.director.routes.DirectorDetailsController.onPageLoad(NormalMode, 0, 1)
      }

      "return a call to Company Review when no of Directors is less than max and AddCompanyDirectorsId is false" in {
        val userAnswers = UserAnswers(validData(johnDoe))
          .set(AddCompanyDirectorsId(0))(false)
          .asOpt
          .value

        val result = navigator.nextPage(AddCompanyDirectorsId(0), NormalMode)(userAnswers)
        result mustEqual controllers.register.establishers.company.routes.CompanyReviewController.onPageLoad(0)
      }

      "return a call to Session Expired when no of Directors is less than max and no answer to AddCompanyDirectorsId" in {
        val userAnswers = UserAnswers(validData(johnDoe))
        val result = navigator.nextPage(AddCompanyDirectorsId(0), NormalMode)(userAnswers)
        result mustEqual controllers.routes.SessionExpiredController.onPageLoad()
      }

      "return a call to Other Directors when no of Directors at Max" in {
        val directors = Seq.fill(maxNoOfDirectors)(johnDoe)
        val result = navigator.nextPage(AddCompanyDirectorsId(0), NormalMode)(UserAnswers(validData(directors: _*)))
        result mustEqual controllers.register.establishers.company.routes.OtherDirectorsController.onPageLoad(NormalMode, 0)
      }
    }

    ".next Page(OtherCompanyDirectors)" must {
      "return a call to Company Review" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(OtherDirectorsId(index), NormalMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.company.routes.CompanyReviewController.onPageLoad(index)
        }
      }
    }

    ".nextPage(CompanyReviewId)" must {
      "return a `Call` to `HaveAnyTrusteesController`" in {
        val answers = UserAnswers().set(SchemeDetailsId)(SchemeDetails("test-scheme-name", SchemeType.BodyCorporate)).asOpt.value
        val result = navigator.nextPage(CompanyReviewId(0), NormalMode)(answers)
        result mustEqual controllers.register.trustees.routes.HaveAnyTrusteesController.onPageLoad(NormalMode)
      }
    }

  }


  "CheckMode" when {

    ".nextPage(CompanyDetails)" must {
      "return a `Call` to `CheckYourAnswers` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(CompanyDetailsId(index), CheckMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.company.routes.CheckYourAnswersController.onPageLoad(index)
        }
      }
    }

    ".nextPage(CompanyRegistrationNumber)" must {
      "return a `Call` to `CheckYourAnswers` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(CompanyRegistrationNumberId(index), CheckMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.company.routes.CheckYourAnswersController.onPageLoad(index)
        }
      }
    }

    ".nextPage(CompanyUniqueTaxReference)" must {
      "return a `Call` to `CheckYourAnswers` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(CompanyUniqueTaxReferenceId(index), CheckMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.company.routes.CheckYourAnswersController.onPageLoad(index)
        }
      }
    }

    ".nextPage(CompanyPostCodeLookupId)" must {
      "return a `Call` to `CompanyAddressList` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(CompanyPostCodeLookupId(index), CheckMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.company.routes.CompanyAddressListController.onPageLoad(CheckMode, index)
        }
      }
    }

    ".nextPage(CompanyAddressListId)" must {
      "return a `Call` to `CompanyAddress` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(CompanyAddressListId(index), CheckMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.company.routes.CompanyAddressController.onPageLoad(CheckMode, index)
        }
      }
    }

    ".nextPage(CompanyAddressId)" must {
      "return a `Call` to `CheckYourAnswers` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(CompanyAddressId(index), CheckMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.company.routes.CheckYourAnswersController.onPageLoad(index)
        }
      }
    }

    ".nextPage(CompanyAddressYears)" must {

      "return a `Call` to `CompanyPreviousPostCodeLookup` page when `CompanyAddressYears` is `UnderAYear`" in {
        val answers = UserAnswers(Json.obj(
          EstablishersId.toString -> Json.arr(
            Json.obj(
              CompanyAddressYearsId.toString ->
                "under_a_year"
            )
          )
        ))
        val result = navigator.nextPage(CompanyAddressYearsId(0), CheckMode)(answers)
        result mustEqual controllers.register.establishers.company.routes.CompanyPreviousAddressPostcodeLookupController.onPageLoad(CheckMode, 0)
      }

      "return a `Call` to `CheckYourAnswersPage` page when `CompanyAddressYears` is `OverAYear`" in {
        val answers = UserAnswers(Json.obj(
          EstablishersId.toString -> Json.arr(
            Json.obj(
              CompanyAddressYearsId.toString ->
                "over_a_year"
            )
          )
        ))
        val result = navigator.nextPage(CompanyAddressYearsId(0), CheckMode)(answers)
        result mustEqual controllers.register.establishers.company.routes.CheckYourAnswersController.onPageLoad(0)
      }

      "return a `Call` to `SessionExpired` page when `CompanyAddressYears` is undefined" in {
        val result = navigator.nextPage(CompanyAddressYearsId(0), CheckMode)(emptyAnswers)
        result mustEqual controllers.routes.SessionExpiredController.onPageLoad()
      }
    }

    ".nextPage(CompanyPreviousAddressPostCodeLookup)" must {
      "return a `Call` to `CompanyPreviousAddressList`" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(CompanyPreviousAddressPostcodeLookupId(index), CheckMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.company.routes.CompanyPreviousAddressListController.onPageLoad(CheckMode, index)
        }
      }
    }

    ".nextPage(CompanyPreviousAddressList)" must {
      "return a `Call` to `CompanyPreviousAddress`" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(CompanyPreviousAddressListId(index), CheckMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.company.routes.CompanyPreviousAddressController.onPageLoad(CheckMode, index)
        }
      }
    }

    ".nextPage(CompanyPreviousAddress)" must {
      "return a `Call` to `CheckYourAnswers`" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(CompanyPreviousAddressId(index), CheckMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.company.routes.CheckYourAnswersController.onPageLoad(index)
        }
      }
    }

    ".nextPage(CompanyContactDetails)" must {
      "return a `Call` to `CheckYourAnswers`" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(CompanyContactDetailsId(index), CheckMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.company.routes.CheckYourAnswersController.onPageLoad(index)
        }
      }
    }

    ".next Page(AddCompanyDirectors)" must {
      "return a call to Director Details when there are no directors" in {
        val result = navigator.nextPage(AddCompanyDirectorsId(0), CheckMode)(emptyAnswers)
        result mustEqual controllers.register.establishers.company.director.routes.DirectorDetailsController.onPageLoad(CheckMode, 0, 0)
      }

      "return a call to Director Details when no of Directors is less than max and AddCompanyDirectorsId is true" in {
        val userAnswers = UserAnswers(validData(johnDoe))
          .set(AddCompanyDirectorsId(0))(true)
          .asOpt
          .value

        val result = navigator.nextPage(AddCompanyDirectorsId(0), CheckMode)(userAnswers)
        result mustEqual controllers.register.establishers.company.director.routes.DirectorDetailsController.onPageLoad(CheckMode, 0, 1)
      }

      "return a call to Company Review when no of Directors is less than max and AddCompanyDirectorsId is false" in {
        val userAnswers = UserAnswers(validData(johnDoe))
          .set(AddCompanyDirectorsId(0))(false)
          .asOpt
          .value

        val result = navigator.nextPage(AddCompanyDirectorsId(0), CheckMode)(userAnswers)
        result mustEqual controllers.register.establishers.company.routes.CompanyReviewController.onPageLoad(0)
      }

      "return a call to Session Expired when no of Directors is less than max and no answer to AddCompanyDirectorsId" in {
        val userAnswers = UserAnswers(validData(johnDoe))
        val result = navigator.nextPage(AddCompanyDirectorsId(0), CheckMode)(userAnswers)
        result mustEqual controllers.routes.SessionExpiredController.onPageLoad()
      }

      "return a call to Other Directors when no of Directors at Max" in {
        val directors = Seq.fill(maxNoOfDirectors)(johnDoe)
        val result = navigator.nextPage(AddCompanyDirectorsId(0), CheckMode)(UserAnswers(validData(directors: _*)))
        result mustEqual controllers.register.establishers.company.routes.OtherDirectorsController.onPageLoad(CheckMode, 0)
      }
    }

  }

}
