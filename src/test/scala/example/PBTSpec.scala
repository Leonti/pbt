package example

import org.scalatest._
import org.scalatest.prop.{Checkers, GeneratorDrivenPropertyChecks}

class PBTSpec extends FlatSpec with Matchers with Checkers {
  implicit override val generatorDrivenConfig =
    PropertyCheckConfig(minSize = 0, maxSize = 100)

  "List" should "be the same after round-trip reversal" in {
    check { list: List[Int] =>
      list.reverse.reverse == list
    }
  }
}
