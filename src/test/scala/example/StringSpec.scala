package example

import org.scalatest._
import org.scalatest.prop.Checkers

class StringSpec extends FlatSpec with Matchers with Checkers {
  implicit override val generatorDrivenConfig =
    PropertyCheckConfig(minSize = 0, maxSize = 100)

  "String" should "be the same after round-trip case change" in {
    val text = "hello"
    text.toUpperCase.toLowerCase shouldBe text
  }

  it should "be the same after round-trip case change 2" in {
    check { text: String =>
      text.toUpperCase.toLowerCase == text
    }
  }
}

