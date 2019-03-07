class: center, middle

# Property-based Testing

---

# Example-based testing

- Create predefined input
- Execute code
- Check output

```scala
  "String" should "should be the same after round-trip case change" in {
    val text = "hello"
    text.toUpperCase.toLowerCase shouldBe text
  }
```
---
# Property-based testing

- Define a boolean property
- Let the framework do testing with random values

```scala
  it should "should be the same after round-trip case change 2" in {
    check { text: String =>
      text.toUpperCase.toLowerCase == text
    }
  }
```
---
class: middle

```scala
  it should "should be the same after round-trip case change 2" in {
    check { text: String =>
      text.toUpperCase.toLowerCase == text
    }
  }
```

```
  Falsified after 3 successful property evaluations.
  Location: (StringSpec.scala:16)
  Occurred when passed generated values (
    arg0 = "B" // 7 shrinks
  )
```

---
class: center, middle

# Patterns  

---
# Fuzzing

## Function should return a sensible value

```scala
  it should "make byte array smaller" in {
    check { bytes: Array[Byte] =>
      compress(bytes).length <= bytes.length
    }
  }
```

---

# Circular code  
## There and back again

- serialisation/deserialisation
- encryption/decryption
- read/write
- formatting/parsing

---

class: middle

```scala
  val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

  it should "format date and be able to parse it" in {
    check { dateTime: LocalDateTime =>
      dateTime == LocalDateTime.parse(dateTime.format(formatter), formatter)
    }
  }
```

---

# Idempotent functions
## Applying output to input should be a no-op

- cleaning data
- normalisation
- escaping

---

class: middle

```scala
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
```

---

# Invariants
## Properties that don't change
 - transformations
 
---

class: middle

```scala
  it should "preserve string length after lowercasing" in {
    check { text: String =>
      text.toLowerCase.length == text.length
    }
  }
```

```
  Falsified after 7 successful property evaluations.
  Location: (InvarianceSpec.scala:11)
  Occurred when passed generated values (
    arg0 = "İ" // 14 shrinks
  )
```

---

# Oracle code
## Verifying our output against existing code

- older systems
- rewriting algorithms 
  - rewriting low-performant code
  - making sense of high-performant code

---

class: middle

```scala
  def javaBase64(src: Array[Byte]) = Base64.getEncoder.encodeToString(src)
  def myBase64(src: Array[Byte]) = new BASE64Encoder().encode(src)

  it should "generate the same base64 as the system 'base64' util" in {
    check { bytes: Array[Byte] =>
      javaBase64(bytes) == myBase64(bytes)
    }
  }
```

---

# Stateful property testing
## Test sequences of operations

- Generate a list of random commands
- Evaluate a post-condition for each command

---

class: middle

```scala
case class Counter(private var n: Int = 0) {
  def increment(): Int = {
    n = n + 1
    n
  }
  def get: Int = n
}
```

---

class: middle

```scala
  case object Increment extends Command {
    type Result = Int

    def run(counter: Counter): Result = counter.increment

    def nextState(state: State): State = state.copy(count = state.count + 1)

    def preCondition(state: State): Boolean = true

    def postCondition(state: State, result: Try[Result]): Prop = 
      result == Success(state.count + 1)
  }
```

---

class: middle

```scala
  def newSut(state: State): Sut = Counter(state.count.toInt)

  def destroySut(sut: Sut): Unit = ()

  def initialPreCondition(state: State): Boolean = true

  def genInitialState: Gen[State] = 
    arbitrary[Int].map(n => State.apply(n.toLong))

  def genCommand(state: State): Gen[Command] = oneOf(Increment, Get)

```

---

class: middle

```scala
  it should "successfully evaluate commands" in {
    check(StatefulCommands.property())
  }
```

```
  Falsified after 6 successful property evaluations.
  Location: (StatefulSpec.scala:67)
  Occurred when passed generated values (
    arg0 = Actions(State(2147483647),List(Get, Get, Get, Get, Increment, Get, Increment, Get, Increment, Get, Increment, Increment, Increment, Increment, Get, Increment, Get, Get, Increment, Get, Increment, Get, Increment, Increment, Increment, Increment, Increment, Get, Increment, Get, Get, Increment, Get, Get, Get, Increment, Get, Increment, Increment, Increment, Increment, Increment, Increment, Increment, Increment, Increment, Get, Get, Get, Get, Get, Increment, Increment, Increment, Get, Get, Increment, Get, Get, Get),List())
  )
  Label of failing property:
    initialstate = State(2147483647)
seqcmds = (Get => 2147483647; Get => 2147483647; Get => 2147483647; Get => 2147483647; Increment => -2147483648; Get => -214...
```

---

class: center, middle

# Shrinking

---

class: middle

```scala
import org.scalacheck.Shrink

val intShrink = implicitly[Shrink[Int]]

intShrink.shrink(100).toList
intShrink.shrink(50).toList
intShrink.shrink(25).toList
```

```scala
res0: List[Int] = List(50, -50, 25, -25, 12, -12, 6, -6, 3, -3, 1, -1, 0)
res1: List[Int] = List(25, -25, 12, -12, 6, -6, 3, -3, 1, -1, 0)
res2: List[Int] = List(12, -12, 6, -6, 3, -3, 1, -1, 0)
```

---
class: center, middle

# Generators

---

class: middle

```scala
import org.scalacheck.Arbitrary

val intArbitrary = implicitly[Arbitrary[Int]]
val gen = intArbitrary.arbitrary
gen.sample
```

```scala
import org.scalacheck.Arbitrary

intArbitrary: org.scalacheck.Arbitrary[Int] = org.scalacheck.ArbitraryLowPriority$$anon$1@745059c6
gen: org.scalacheck.Gen[Int] = org.scalacheck.Gen$$anon$1@47ab9ee1
res0: Option[Int] = Some(400598836)
```

---

class: center, middle

# Form Validation Example

---

class: middle

```scala
  case class AdForm(
                   title: Option[String],
                   content: Option[String],
                   salaryLow: Option[String],
                   salaryHigh: Option[String]
                   )

  case class Ad(
                 title: String,
                 content: String,
                 salaryLow: BigDecimal,
                 salaryHigh: BigDecimal
               )
               
  def toValidatedAd(form: AdForm): ValidatedNel[String, Ad] = ???
```

---

class: middle

```scala
  val validTitle = "Long enough"
  val validContent = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Mauris ut odio vitae nisi porttitor interdum eget ac cras amet."
  val validSalaryLow = "20000"
  val validSalaryHigh = "30000"

  it should "validate form" in {
    val form = AdForm(Some(validTitle), Some(validContent), Some(validSalaryLow), Some(validSalaryHigh))
    toValidatedAd(form).isValid should be(true)
  }

  it should "invalidate title if it's shorter than 5 characters" in {
    val form = AdForm(Some("s"), Some(validContent), Some(validSalaryLow), Some(validSalaryHigh))
    toValidatedAd(form).isInvalid should be(true)
  }

  it should "invalidate content if it's shorter than 100 characters" in {
    val form = AdForm(Some(validTitle), Some("s"), Some(validSalaryLow), Some(validSalaryHigh))
    toValidatedAd(form).isInvalid should be(true)
  }
  
  it should "...
```

---

class: middle

* 4 fields  
* Multiple permutations  
* Corner cases  

---

class: middle

Title field:  

* at lower bound
* at upper bound
* less than lower bound
* more than upper bound
* in between


20 tests for the form to cover the basics

---

class: middle, center

# Property testing approach

---

class: middle

```scala
  def sizedStringGen(lower: Int, upper: Int): Gen[String] = {
    for {
      size <- Gen.chooseNum(lower, upper)
      title <- Gen.listOfN(size, Gen.alphaChar).map(_.mkString)
    } yield title
  }
  
  val validSalaryGen: Gen[String] = Gen.posNum[Double].map(_.toString)
  val validTitleGen: Gen[String] = sizedStringGen(5, 500)
  val validContentGen: Gen[String] = sizedStringGen(100, 20000)

  val invalidSalaryGen: Gen[String] = 
    Gen.oneOf(Gen.negNum[Double].map(_.toString), Gen.alphaStr)
  val invalidTitleGen: Gen[String] = sizedStringGen(0, 4)
  val invalidContentGen: Gen[String] = sizedStringGen(0, 99)
```

---

class: middle, center
 
narrow input, broad expectations  
  
VS
    
broad input, specific expectations

---

class: middle

```scala
val validTitleGen: Gen[String] = sizedStringGen(5, 500)
val invalidTitleGen: Gen[String] = sizedStringGen(0, 4)
```

---

class: middle

```scala
  case class InvalidForm(form: AdForm, invalidFields: Int)

  val invalidFormGen: Gen[InvalidForm] = for {
    fieldTypes <- fieldTypesGen
    title <- fieldGen(fieldTypes(0), validTitleGen, invalidTitleGen)
    content <- fieldGen(fieldTypes(1), validContentGen, invalidContentGen)
    lowSalary <- fieldGen(fieldTypes(2), validSalaryGen, invalidSalaryGen)
    highSalary <- fieldGen(fieldTypes(3), validSalaryGen, invalidSalaryGen)
  } yield InvalidForm(
    AdForm(title, content, lowSalary, highSalary),
    fieldTypes.count(_ != ValidField)
  )
```

---

class: middle

```scala
  sealed trait FieldType
  case object ValidField extends FieldType
  case object InvalidField extends FieldType
  case object MissingField extends FieldType

  val fieldTypesGen: Gen[Seq[FieldType]] =
    Gen.listOfN(4, Gen.oneOf(ValidField, InvalidField, MissingField))
    .suchThat(fields => 
    fields.contains(InvalidField) || fields.contains(MissingField))
```

---

class: middle

```scala
  it should "invalidate form" in {
    check(forAll(invalidFormGen) { invalidForm: InvalidForm =>
      toValidatedAd(invalidForm.form) match {
        case Invalid(errors) => errors.length == invalidForm.invalidFields
        case Valid(_) => false
      }
    })
  }
```

---

class: middle

```scala
  val validFormGen: Gen[AdForm] = for {
    title <- validTitleGen
    content <- validContentGen
    lowSalary <- validSalaryGen
    highSalary <- validSalaryGen
  } yield AdForm(Some(title), Some(content), Some(lowSalary), Some(highSalary))
  
  it should "accept valid form" in {
    check(forAll(validFormGen) { form: AdForm =>
      toValidatedAd(form).isValid
    })
  }
```

---

class: middle

```scala
  implicit override val generatorDrivenConfig: PropertyCheckConfiguration =
    PropertyCheckConfig(minSuccessful = 2000)
```

---

class: middle, center

Thank you!