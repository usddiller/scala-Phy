import cats.effect._
import cats.effect.std.Mutex
import cats.syntax.all._
import java.util.UUID
import scala.concurrent.duration._
import scala.util.Random


case class Phil(id: UUID, name: String)
case class Table(forks: Vector[Mutex[IO]], counts: Vector[Ref[IO, Int]])

object Philosophers extends IOApp.Simple {
  
  def eat(p: Phil, seatIndex: Int, table: Table): IO[Unit] = {
    val leftIdx = seatIndex
    val rightIdx = (seatIndex + 1) % table.forks.size
    val (first, second) = if (leftIdx < rightIdx) (leftIdx, rightIdx) else (rightIdx, leftIdx)

    table.forks(first).lock.use { _ =>
      table.forks(second).lock.use { _ =>
        for {
          c <- table.counts(seatIndex).updateAndGet(_ + 1)
          _ <- IO.println(s"${p.name} ЕСТ ($c раз)")
          _ <- IO.sleep(1.second)
        } yield ()
      }
    }
  }

  def live(p: Phil, seatIndex: Int, table: Table): IO[Unit] = {
    for {
      wait <- IO(Random.nextInt(600) + 200)
      _ <- IO.println(s"${p.name} думает...")
      _ <- IO.sleep(wait.millis)
      _ <- eat(p, seatIndex, table)
      _ <- live(p, seatIndex, table)
    } yield ()
  }

  override def run: IO[Unit] =
    for {
      f1 <- Mutex[IO]
      f2 <- Mutex[IO]
      f3 <- Mutex[IO]
      f4 <- Mutex[IO]
      f5 <- Mutex[IO]
      c1 <- Ref.of[IO, Int](0)
      c2 <- Ref.of[IO, Int](0)
      c3 <- Ref.of[IO, Int](0)
      c4 <- Ref.of[IO, Int](0)
      c5 <- Ref.of[IO, Int](0)
      table = Table(Vector(f1, f2, f3, f4, f5), Vector(c1, c2, c3, c4, c5))

      philosophers = List(
        Phil(UUID.randomUUID(), "Философ 1"),
        Phil(UUID.randomUUID(), "Философ 2"),
        Phil(UUID.randomUUID(), "Философ 3"),
        Phil(UUID.randomUUID(), "философ 4"),
        Phil(UUID.randomUUID(), "философ 5")
      )

      _ <- philosophers.zipWithIndex.traverse{case (p, idx) => live(p, idx, table).start}
      _ <- (for {
        _ <- IO.sleep(15.seconds)
        _ <- IO.println("")
        _ <- philosophers.zipWithIndex.traverse { case (p, idx) =>
          for {
            c <- table.counts(idx).get
            _ <- IO.println(s"${p.name} поел $c раз")
          } yield ()
        }
      } yield ()).foreverM.start
      _ <- IO.never

    } yield ()
}