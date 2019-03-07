import org.scalacheck.Arbitrary

val intArbitrary = implicitly[Arbitrary[Int]]
val gen = intArbitrary.arbitrary
gen.sample