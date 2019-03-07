package example

import org.scalatest._
import org.scalatest.prop.Checkers

class InvarianceSpec extends FlatSpec with Matchers with Checkers {
  implicit override val generatorDrivenConfig =
    PropertyCheckConfig(minSize = 1000, maxSize = 50000)

  it should "preserve string length after lowercasing" in {
    check { text: String =>
      text.toLowerCase.length == text.length
    }
  }
}

