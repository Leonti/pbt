package example

import java.net.URLEncoder

import org.scalatest._
import org.scalatest.prop.Checkers

class IdempotentSpec extends FlatSpec with Matchers with Checkers {
  implicit override val generatorDrivenConfig =
    PropertyCheckConfig(minSize = 1000, maxSize = 20000)

  def normalise(input: String) = input
    .replaceAll("\\s+", " ")
    .replaceAll("\\n", "")
    .trim

  it should "not change string if it's already normalised" in {
    check { text: String =>
      val normalised = normalise(text)
      normalised == normalise(normalised)
    }
  }
}

