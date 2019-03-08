package example

import org.scalacheck.Gen
import org.scalacheck.Prop.forAll
import org.scalatest._
import org.scalatest.prop.Checkers

class BasicSpec extends FlatSpec with Matchers with Checkers {

  def applyTax(salary: BigDecimal): BigDecimal = salary - salary * BigDecimal("0.37")

  it should "apply tax" in {
    applyTax(20000) shouldBe BigDecimal(12600)
  }

  it should "calculate sane tax" in {
    check(forAll(Gen.posNum[Double]) { salary: Double =>
      applyTax(BigDecimal(salary)) < salary
    })
  }
}

