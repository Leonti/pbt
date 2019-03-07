package example

import org.scalacheck.Gen
import cats.data.Validated.{Invalid, Valid}
import cats.implicits._
import cats.data.ValidatedNel
import org.scalatest._
import org.scalatest.prop.Checkers
import org.scalacheck.Prop.forAll

import scala.util.Try

class FormValidationSpec extends FlatSpec with Matchers with Checkers {
  implicit override val generatorDrivenConfig: PropertyCheckConfiguration =
    PropertyCheckConfig(minSuccessful = 2000)

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


  def validateTitle(title: Option[String]): ValidatedNel[String, String] =
     title.map(t => if (t.length >= 5 && t.length <= 500) t.validNel else
       "Title should be between 5 and 500 characters long".invalidNel)
      .getOrElse("Title is a required field".invalidNel)

  def validateContent(content: Option[String]): ValidatedNel[String, String] =
    content.map(t => if (t.length >= 100 && t.length <= 20000) t.validNel else
      "Content length should be between 100 and 20000 characters".invalidNel)
      .getOrElse("Content is a required field".invalidNel)

  def validateSalaryLow(salary: Option[String]): ValidatedNel[String, BigDecimal] =
    salary.map(s => Try(BigDecimal(s)).toEither.bimap(_ => "Lower salary bound is not a valid number", identity).toValidatedNel)
      .getOrElse("Lower salary bound is a required field".invalidNel)
    .andThen(s => if (s > 0) s.validNel else "Low salary bound should be bigger than zero".invalidNel)

  def validateSalaryHigh(salary: Option[String]): ValidatedNel[String, BigDecimal] =
    salary.map(s => Try(BigDecimal(s)).toEither.bimap(_ => "Upper salary bound is not a valid number", identity).toValidatedNel)
      .getOrElse("Upper salary bound is a required field".invalidNel)
      .andThen(s => if (s > 0) s.validNel else "Upper salary bound should be bigger than zero".invalidNel)

  def toValidatedAd(form: AdForm): ValidatedNel[String, Ad] = (
    validateTitle(form.title),
    validateContent(form.content),
    validateSalaryLow(form.salaryLow),
    validateSalaryHigh(form.salaryHigh)
  ).mapN(Ad)


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


  def sizedStringGen(lower: Int, upper: Int): Gen[String] = {
    for {
      size <- Gen.chooseNum(lower, upper)
      title <- Gen.listOfN(size, Gen.alphaNumChar).map(_.mkString)
    } yield title
  }
  val validSalaryGen: Gen[String] = Gen.posNum[Double].map(_.toString)
  val validTitleGen: Gen[String] = sizedStringGen(5, 500)
  val validContentGen: Gen[String] = sizedStringGen(100, 20000)

  val invalidSalaryGen: Gen[String] = Gen.oneOf(Gen.negNum[Double].map(_.toString), Gen.alphaStr)
  val invalidTitleGen: Gen[String] = sizedStringGen(0, 4)
  val invalidContentGen: Gen[String] = sizedStringGen(0, 99)

  sealed trait FieldType
  case object ValidField extends FieldType
  case object InvalidField extends FieldType
  case object MissingField extends FieldType

  val fieldTypesGen: Gen[Seq[FieldType]] =
    Gen.listOfN(4, Gen.oneOf(ValidField, InvalidField, MissingField))
    .suchThat(fields => fields.contains(InvalidField) || fields.contains(MissingField))

  def fieldGen[T](fieldType: FieldType, validGen: Gen[T], invalidGen: Gen[T]): Gen[Option[T]] = fieldType match {
    case ValidField => validGen.map(Some(_))
    case InvalidField => invalidGen.map(Some(_))
    case MissingField => Gen.const(None)
  }

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

  val validFormGen: Gen[AdForm] = for {
    title <- validTitleGen
    content <- validContentGen
    lowSalary <- validSalaryGen
    highSalary <- validSalaryGen
  } yield AdForm(Some(title), Some(content), Some(lowSalary), Some(highSalary))

  it should "invalidate form" in {
    check(forAll(invalidFormGen) { invalidForm: InvalidForm =>
      toValidatedAd(invalidForm.form) match {
        case Invalid(errors) => errors.length == invalidForm.invalidFields
        case Valid(_) => false
      }
    })
  }

  it should "accept valid form" in {
    check(forAll(validFormGen) { form: AdForm =>
      toValidatedAd(form).isValid
    })
  }

}

