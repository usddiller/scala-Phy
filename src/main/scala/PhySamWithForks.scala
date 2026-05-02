import cats.effect._
import cats.syntax.all._
import scala.concurrent.duration._

object DiningPhilosophersHierarchy extends IOApp.Simple {

  case class Fork(id: Int, isTaken: Ref[IO, Boolean])

  case class Philosopher(id: Int, firstFork: Fork, secondFork: Fork, count: Ref[IO, Int]) {

    def tryToEat: IO[Boolean] = {
      for {
        firstTaken <- firstFork.isTaken.modify {
          case false => (true, true)
          case true => (true, false)
        }
        res <- if (firstTaken) {
          secondFork.isTaken.modify {
            case false => (true, true)
            case true => (true, false)
          }.flatMap {
            case true => IO.pure(true)
            case false => firstFork.isTaken.set(false).as(false)
          }
        } else IO.pure(false)
      } yield res
    }

    def eat: IO[Unit] = {
      for {
        canEat <- tryToEat
        _ <- if (canEat) {
          for {
            _ <- count.update(_ + 1)
            c <- count.get
            _ <- IO.println(s"$id ЕСТ ($c раз)")
            _ <- IO.sleep(1.second)
            _ <- firstFork.isTaken.set(false)
            _ <- secondFork.isTaken.set(false)
            _ <- IO.println(s"$id закончил есть")
          } yield ()
        } else {
          IO.sleep(50.millis) >> eat
        }
      } yield ()
    }

    def live: IO[Unit] = {
      for {
        _ <- IO.println(s"$id думает...")
        _ <- IO.sleep(500.millis)
        _ <- eat
        _ <- live
      } yield ()
    }
  }

  override def run: IO[Unit] =
    for {

      f1 <- Ref.of[IO, Boolean](false); f2 <- Ref.of[IO, Boolean](false)
      f3 <- Ref.of[IO, Boolean](false); f4 <- Ref.of[IO, Boolean](false)
      f5 <- Ref.of[IO, Boolean](false)

      c1 <- Ref.of[IO, Int](0); c2 <- Ref.of[IO, Int](0)
      c3 <- Ref.of[IO, Int](0); c4 <- Ref.of[IO, Int](0)
      c5 <- Ref.of[IO, Int](0)


      fork1 = Fork(1, f1); fork2 = Fork(2, f2); fork3 = Fork(3, f3)
      fork4 = Fork(4, f4); fork5 = Fork(5, f5)

      philosophers = List(
        Philosopher(1, fork1, fork2, c1),
        Philosopher(2, fork2, fork3, c2),
        Philosopher(3, fork3, fork4, c3),
        Philosopher(4, fork4, fork5, c4),
        Philosopher(5, fork1, fork5, c5)
      )

      _ <- philosophers.traverse(_.live.start)


      _ <- (for {
        _ <- IO.sleep(15.seconds)
        _ <- IO.println("")
        _ <- philosophers.traverse { p =>
          for {
            c <- p.count.get
            _ <- IO.println(s"${p.id} поел $c раз")
          } yield ()
        }

      } yield ()).foreverM.start
      _ <- IO.never
    } yield ()
}