package example

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.prop.GeneratorDrivenPropertyChecks

case class Person(name: String, age: Int)
case class FullName(firstName: String, lastName: String)

class PersonSpec extends FlatSpec with Matchers with GeneratorDrivenPropertyChecks {

  val fullName: Person => FullName = person => {
    val split = person.name.split(" ")
    FullName(split.head, split(1))
  }

  val genPerson: Gen[Person] = for {
    name <- arbitrary[String]
    age <- Gen.choose(0,200)
  } yield Person(name, age)

  forAll(genPerson) { p: Person =>
    val full = fullName(p)

    p.name should contain(full.firstName)
    p.name should contain(full.lastName)
  }

}
