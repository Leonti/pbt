import org.scalacheck.Shrink

val intShrink = implicitly[Shrink[Int]]

intShrink.shrink(100).toList
intShrink.shrink(50).toList
intShrink.shrink(25).toList

