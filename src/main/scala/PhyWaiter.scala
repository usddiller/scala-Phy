import cats.effect._
import cats.syntax.all._
import scala.concurrent.duration._
import cats.effect.std.Semaphore

object PhyWaiter extends IOApp.Simple {

  case class Fork(id: Int, isTaken: Ref[IO, Boolean])

  case class Waiter(places: Semaphore[IO]) //если честно я не особо разобралс что такое семафор
  // со слов ии это просто счетчик ограничивающий количество философов пытающихся обращаться
  // к нему, но я так и не понял как мне самостоятельно его написать
  // не разобрался с атомарностью  и семантической блокировкой
  // а именно не понял как сделать чтоб там и очеред была, и паралельно оно выполнялось, чтоб 
  //философы одновременно не уменьшили счетчик 

  case class Philosopher(id: Int, leftFork: Fork, rightFork: Fork, count: Ref[IO, Int], waiter: Waiter) {
    def tryToEat: IO[Boolean] = {
      for {
        leftTaken <- leftFork.isTaken.modify {
          case false => (true, true)
          case true => (true, false)
        }
        res <- if (leftTaken) {
          rightFork.isTaken.modify {
            case false => (true, true)
            case true => (true, false)
          }.flatMap {
            case true => IO.pure(true)
            case false => leftFork.isTaken.set(false).as(false)
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
            _ <- leftFork.isTaken.set(false)
            _ <- rightFork.isTaken.set(false)
            _ <- IO.println(s"$id закончил есть")
          } yield ()
        } else {
          IO.sleep(50.millis) >> eat
        }
      } yield ()
    }


    def live: IO[Unit] = {
      for {
        _ <- IO.println(s"Философ $id думает...")
        _ <- IO.sleep(500.millis)
        _ <- waiter.places.acquire
        _ <- eat
        _ <- waiter.places.release
        _ <- live
      } yield ()
    }
  }

  override def run: IO[Unit] = {
    for {
      sem <- Semaphore[IO](4)
      waiter = Waiter(sem)

      f1 <- Ref.of[IO, Boolean](false); f2 <- Ref.of[IO, Boolean](false)
      f3 <- Ref.of[IO, Boolean](false); f4 <- Ref.of[IO, Boolean](false)
      f5 <- Ref.of[IO, Boolean](false)

      c1 <- Ref.of[IO, Int](0); c2 <- Ref.of[IO, Int](0)
      c3 <- Ref.of[IO, Int](0); c4 <- Ref.of[IO, Int](0)
      c5 <- Ref.of[IO, Int](0)

      philosophers = List(
        Philosopher(1, Fork(1, f1), Fork(2, f2), c1, waiter),
        Philosopher(2, Fork(2, f2), Fork(3, f3), c2, waiter),
        Philosopher(3, Fork(3, f3), Fork(4, f4), c3, waiter),
        Philosopher(4, Fork(4, f4), Fork(5, f5), c4, waiter),
        Philosopher(5, Fork(5, f5), Fork(1, f1), c5, waiter)
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
    } yield ()  }
}