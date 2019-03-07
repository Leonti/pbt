package example

import org.scalatest._
import org.scalatest.prop.Checkers

class FuzzingSpec extends FlatSpec with Matchers with Checkers {

  def compress(bytes: Array[Byte]): Array[Byte] = Array()

  it should "make byte array smaller" in {
    check { bytes: Array[Byte] =>
      compress(bytes).length <= bytes.length
    }
  }
}

