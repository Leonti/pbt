package example

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDate, LocalDateTime, ZoneId}
import java.time.temporal.TemporalAccessor

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest._
import org.scalatest.prop.Checkers

class CircularSpec extends FlatSpec with Matchers with Checkers {

  val genLocalDate: Gen[LocalDateTime] = Gen.choose(0, 1551244783743l)
    .map(ts => Instant.ofEpochMilli(ts).atZone(ZoneId.of("UTC")).toLocalDateTime)

  implicit val arbDateTime = Arbitrary(genLocalDate)

  val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

  it should "format date and be able to parse it" in {
    check { dateTime: LocalDateTime =>
      dateTime == LocalDateTime.parse(dateTime.format(formatter), formatter)
    }
  }
}

