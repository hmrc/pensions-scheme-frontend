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

package utils

import org.scalacheck.Gen
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json._

class JsLensSpec extends WordSpec with MustMatchers with PropertyChecks with OptionValues {

  val jsLeafGen: Gen[JsValue] = {
    Gen.frequency(
      10 -> Gen.alphaNumStr.map(JsString),
      10 -> Gen.chooseNum(1, 9999).map(JsNumber(_)),
      3  -> Gen.oneOf(true, false).map(JsBoolean)
    )
  }

  "#atKey (get)" must {
    "return a lens" which {

      "gets an inner value at `key` when it is available" in {

        forAll(Gen.alphaNumStr, jsLeafGen) {
          (key, value) =>

            val json = Json.obj(
              key -> value
            )

            val lens = JsLens.atKey(key)
            lens.get(json).asOpt.value mustEqual value
        }
      }

      "gets an error when the key is undefined" in {

        forAll(Gen.alphaNumStr) {
          key =>
            val json = Json.obj()
            val lens = JsLens.atKey(key)
            lens.get(json).isError mustBe true
        }
      }

      "gets an error when the `JsValue` is a non object" in {

        val gen = Gen.frequency(
          10 -> jsLeafGen,
          2  -> Gen.const(JsNull)
        )

        forAll(Gen.alphaNumStr, gen) {
          (key, json) =>

            val lens = JsLens.atKey(key)
            lens.get(json).isError mustBe true
        }
      }
    }
  }

  "#atKey (put)" must {
    "return a lens" which {

      "replaces an existing key on an object" in {

        forAll(Gen.alphaNumStr, jsLeafGen, jsLeafGen) {
          (key, oldValue, newValue) =>

            val json = Json.obj(
              key -> oldValue
            )

            val lens = JsLens.atKey(key)
            lens.put(json, newValue).asOpt.value mustEqual Json.obj(key -> newValue)
        }
      }

      "assigns a new key on an object" in {

        forAll(Gen.alphaNumStr, jsLeafGen) {
          (key, value) =>
            val json = Json.obj()
            val lens = JsLens.atKey(key)
            lens.put(json, value).asOpt.value mustEqual Json.obj(key -> value)
        }
      }

      "assigns a new key to a new object when the outer object is null" in {

        forAll(Gen.alphaNumStr, jsLeafGen) {
          (key, value) =>
            val json = JsNull
            val lens = JsLens.atKey(key)
            lens.put(json, value).asOpt.value mustEqual Json.obj(key -> value)
        }
      }

      "fails to add a key to a non-object" in {

        forAll(Gen.alphaNumStr, jsLeafGen, jsLeafGen) {
          (key, json, newValue) =>
            val lens = JsLens.atKey(key)
            lens.put(json, newValue).isError mustBe true
        }
      }
    }
  }

  "#atIndex (get)" must {
    "return a lens" which {

      "gets an element from an array when it exists at the given index" in {

        val gen: Gen[(Int, JsArray)] = for {
          idx <- Gen.chooseNum(0, 50)
          arr <- Gen.listOfN(idx + 1, jsLeafGen).map(JsArray)
        } yield (idx, arr)

        forAll(gen) {
          case (idx, arr) =>
            val lens = JsLens.atIndex(idx)
            lens.get(arr).asOpt.value mustEqual arr(idx).get
        }
      }

      "gets an error when the index doesn't exist in the array" in {

        forAll(Gen.chooseNum(0, 50)) {
          idx =>
            val lens = JsLens.atIndex(idx)
            lens.get(Json.arr()).isError mustEqual true
        }
      }

      "gets an error when the JsValue is not an array" in {

        forAll(Gen.chooseNum(0, 50), jsLeafGen) {
          (idx, json) =>
            val lens = JsLens.atIndex(idx)
            lens.get(json).isError mustEqual true
        }
      }
    }
  }

  "#atIndex (put)" must {
    "return a lens" which {

      "replaces the value at the given index" in {

        val gen: Gen[(Int, JsArray)] = for {
          idx <- Gen.chooseNum(0, 50)
          arr <- Gen.listOfN(idx + 1, jsLeafGen).map(JsArray)
        } yield (idx, arr)

        forAll(gen, jsLeafGen) {
          case ((idx, arr), newValue) =>
            val lens = JsLens.atIndex(idx)
            lens.put(arr, newValue).asOpt.value(idx).get mustEqual newValue
        }
      }

      "adds the value to an empty array" in {

        forAll(jsLeafGen) {
          value =>
            val arr = Json.arr()
            val lens = JsLens.atIndex(0)
            lens.put(arr, value).asOpt.value mustEqual JsArray(Seq(value))
        }
      }

      "adds the value to an array when the index is one more than the defined indices" in {

        val gen: Gen[(Int, JsArray)] = for {
          idx <- Gen.chooseNum(0, 50)
          arr <- Gen.listOfN(idx + 1, jsLeafGen).map(JsArray)
        } yield (idx, arr)

        forAll(gen, jsLeafGen) {
          case ((idx, arr), newValue) =>
            val lens = JsLens.atIndex(idx + 1)
            lens.put(arr, newValue).asOpt.value mustEqual JsArray(arr.value :+ newValue)
        }
      }

      "creates a new array when the JsValue is null" in {

        forAll(jsLeafGen) {
          value =>
            val lens = JsLens.atIndex(0)
            lens.put(Json.arr(), value).asOpt.value mustEqual JsArray(Seq(value))
        }
      }

      "fail to add an index to a non-array" in {

        forAll(Gen.chooseNum(0, 50), jsLeafGen, jsLeafGen) {
          (idx, oldValue, newValue) =>
            val lens = JsLens.atIndex(idx)
            lens.put(oldValue, newValue).isError mustEqual true
        }
      }

      "fail to add an index greater than the size of the array" in {

        val gen: Gen[(Int, JsArray)] = for {
          idx <- Gen.chooseNum(0, 50)
          arr <- Gen.listOfN(idx + 1, jsLeafGen).map(JsArray)
        } yield (idx, arr)

        forAll(gen, jsLeafGen) {
          case ((idx, arr), newValue) =>
            val lens = JsLens.atIndex(idx + 2)
            lens.put(arr, newValue).isError mustEqual true
        }
      }

      "fail to add an index less than 0" in {

        forAll(Gen.chooseNum(-50, -1)) {
          idx =>
            an[IllegalArgumentException] mustBe thrownBy {
              JsLens.atIndex(idx)
            }
        }
      }
    }

    ".andThen" must {

      "apply lenses in order (get)" in {

        val json = Json.obj(
          "abc" -> Json.arr("d", "e", "f")
        )
        val lens = JsLens.atKey("abc") andThen JsLens.atIndex(1)

        lens.get(json).asOpt.value mustEqual JsString("e")
      }

      "apply lenses in order (put)" in {

        val json = Json.obj(
          "abc" -> Json.arr("d", "e", "f")
        )
        val lens = JsLens.atKey("abc") andThen JsLens.atIndex(1)

        lens.put(json, JsString("foo")).asOpt.value mustEqual Json.obj(
          "abc" -> Json.arr("d", "foo", "f")
        )
      }

      "insert when paths don't exist" in {

        val json = Json.obj()
        val lens = JsLens.atKey("abc") andThen JsLens.atKey("def") andThen JsLens.atIndex(0) andThen JsLens.atIndex(0)

        lens.put(json, JsString("foo")).asOpt.value mustEqual Json.obj(
          "abc" -> Json.obj(
            "def" -> Json.arr(Json.arr("foo"))
          )
        )
      }

      "fail to insert into a new array when the index is greater than 0" in {

        val json = Json.obj()
        val lens = JsLens.atKey("abc") andThen JsLens.atIndex(1)

        lens.put(json, JsString("foo")).isError mustEqual true
      }
    }
  }
}

