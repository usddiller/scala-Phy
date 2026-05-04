import cats.effect.*
import cats.syntax.all._
import cats.effect.std.Semaphore

object Philosophers {


  case class Philosopher(id: Int)
  case class Fork(id: Int)

  case class Table(
                    forks: Vector[Ref[IO, Boolean]],
                    counts: Vector[Ref[IO, Int]],
                    waiter: Semaphore[IO]
                  )
}
