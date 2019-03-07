import org.scalacheck._
import Arbitrary._

case class Person(name: String, age: Int)

val genPerson: Gen[Person] = for {
  name <- arbitrary[String]
  age <- Gen.choose(0,200)
} yield Person(name, age)

genPerson.sample

