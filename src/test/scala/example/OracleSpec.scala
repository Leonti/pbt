package example

import java.util.Base64

import org.scalatest._
import org.scalatest.prop.Checkers
import sun.misc.BASE64Encoder

class OracleSpec extends FlatSpec with Matchers with Checkers {

  def javaBase64(src: Array[Byte]) = Base64.getEncoder.encodeToString(src)
  def myBase64(src: Array[Byte]) = new BASE64Encoder().encode(src)

  it should "generate the same base64 as the system 'base64' util" in {
    check { bytes: Array[Byte] =>
      javaBase64(bytes) == myBase64(bytes)
    }
  }
}

