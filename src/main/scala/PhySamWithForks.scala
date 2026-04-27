import cats.effect.{IO, IOApp, Ref}
import cats.syntax.all.*
import scala.concurrent.duration.DurationInt
object PhySamWithForks extends IOApp.Simple {
  trait ForkState
  case object OnTable extends ForkState
  case class InHand(philosopherId: Int) extends ForkState
  case class Fork(id: Int, state: ForkState)

  trait PhyState
  object Eat extends PhyState
  object Think extends PhyState
  case class Phy(id: Int, state: PhyState)


  case class TableState(philosophers: Vector[Phy], forks: Vector[Fork])

  object Waiter {

    def tryEat(stateRef: Ref[IO, TableState], phyId: Int): IO[Boolean] =
      stateRef.modify { s =>

        val leftIdx = phyId - 1
        val rightIdx = phyId % 5
        val leftFork = s.forks(leftIdx)
        val rightFork = s.forks(rightIdx)
        
        if (leftFork.state == OnTable && rightFork.state == OnTable) {
          val updatedForks = s.forks
            .updated(leftIdx, leftFork.copy(state = InHand(phyId)))
            .updated(rightIdx, rightFork.copy(state = InHand(phyId)))

          val updatedPhys = s.philosophers.updated(phyId - 1, Phy(phyId, Eat))

          (s.copy(philosophers = updatedPhys, forks = updatedForks), true)
        } else {
          (s, false)
        }
      }

    def backToThink(stateRef: Ref[IO, TableState], phyId: Int): IO[Unit] =
      stateRef.update { s =>
        val leftIdx = phyId - 1
        val rightIdx = phyId % 5

        val updatedForks = s.forks
          .updated(leftIdx, s.forks(leftIdx).copy(state = OnTable))
          .updated(rightIdx, s.forks(rightIdx).copy(state = OnTable))

        val updatedPhys = s.philosophers.updated(phyId - 1, Phy(phyId, Think))

        s.copy(philosophers = updatedPhys, forks = updatedForks)
      }
  }
  
  def tableNow(phys: List[Phy]): IO[Unit] = {
    IO.println("--- Состояние стола на данный момент ---") >>
      phys.traverse { p =>
        val status = p.state match {
          case Eat => "ест"
          case Think => "думает"
        }
        IO.println(s"Философ ${p.id} - $status")
      }.void >>
      IO.println("---------------------------------------")
  }


  override def run: IO[Unit] = ???

}