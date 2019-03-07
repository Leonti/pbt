package example

import org.scalacheck.{Gen, Prop, Shrink}
import org.scalacheck.commands.Commands
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.prop.Checkers
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen.oneOf

import scala.util.{Success, Try}

case class Counter(private var n: Int = 0) {
  def increment(): Int = {
    n = n + 1
    n
  }
  def get: Int = n
}

object StatefulCommands extends Commands {
  case class State(count: Long)

  type Sut = Counter

  def canCreateNewSut(newState: State, initSuts: Traversable[State], runningSuts: Traversable[Sut]): Boolean = true

  def newSut(state: State): Sut = Counter(state.count.toInt)

  def destroySut(sut: Sut): Unit = ()

  def initialPreCondition(state: State): Boolean = true

  def genInitialState: Gen[State] = arbitrary[Int].map(n => State.apply(n.toLong))

  def genCommand(state: State): Gen[Command] = oneOf(Increment, Get)

  case object Increment extends Command {
    type Result = Int

    def run(counter: Counter): Result = counter.increment

    def nextState(state: State): State = state.copy(count = state.count + 1)

    def preCondition(state: State): Boolean = true

    def postCondition(state: State, result: Try[Result]): Prop = result == Success(state.count + 1)
  }

  case object Get extends Command {
    type Result = Int

    def run(counter: Counter): Result = counter.get

    def nextState(state: State): State = state

    def preCondition(state: State): Boolean = true

    def postCondition(state: State, result: Try[Result]): Prop =
      result == Success(state.count)
  }

}

class StatefulSpec extends FlatSpec with Matchers with Checkers {

  it should "successfully evaluate commands" in {
    check(StatefulCommands.property())
  }
}
