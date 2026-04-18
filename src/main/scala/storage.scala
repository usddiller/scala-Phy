
import cats.effect.{ExitCode, IO, IOApp, Ref}
import cats.implicits.*

import scala.concurrent.duration.DurationInt

object Dinner extends IOApp.Simple {

  sealed trait PhyState

  case object Eat extends PhyState

  case object Think extends PhyState

  case class Phy(fd: Int, state: PhyState)

  val p1 = Phy(0, Think)
  val p2 = Phy(1, Think)
  val p3 = Phy(2, Think)
  val p4 = Phy(3, Think)
  val p5 = Phy(4, Think)

  val tableStateIO: IO[Ref[IO, List[Phy]]] =
    Ref.of[IO, List[Phy]](List(p1, p2, p3, p4, p5))


  def canEat(ph: Phy, list: List[Phy]): Boolean = {
    val id = ph.fd
    val size = list.size
    val leftIdx = (id - 1 + size) % size
    val rightIdx = (id + 1) % size
    val leftNeighbor = list(leftIdx)
    val rightNeighbor = list(rightIdx)
    list(leftIdx).state != Eat && list(rightIdx).state != Eat
  }

  def tryToEat(ph: Phy, tableState: Ref[IO, List[Phy]]): IO[Boolean] = {
    tableState.modify { list =>
      val id = ph.fd
      val isAllowed = canEat(ph, list)

      val newList = if (isAllowed) {
        list.updated(id, list(id).copy(state = Eat))
      } else {
        list
      }
      (newList, isAllowed)
    }
  }

  def stopEating(ph: Phy, tableState: Ref[IO, List[Phy]]): IO[Unit] = {
    tableState.modify { list =>
      val newList = list.updated(ph.fd, ph.copy(state = Think))
      (newList, ())
    }
  }

  def step(p: Phy, table: Ref[IO, List[Phy]]): IO[Unit] = {
    for {
      _ <- IO.println(s"${p.fd} думает")
      _ <- IO.sleep(1.second)

      canIEat <- tryToEat(p, table)
      _ <- if canIEat then {
        for {
          _ <- IO.println(s"${p.fd} ЕСТ")
          _ <- IO.sleep(1.second)
          _ <- stopEating(p, table)
        } yield ()
      } else {
        IO.println(s"${p.fd} ждет вилки")
      }
      _ <- step(p, table)
    } yield ()
  }


  def run: IO[Unit] =
    for {
      table <- tableStateIO
      _ <- step(p1, table).start
      _ <- step(p2, table).start
      _ <- step(p3, table).start
      _ <- step(p4, table).start
      _ <- step(p5, table).start
      _ <- IO.never
    } yield ()


}

//честно говоря с Ref я бы вообще сам не додумался, и со сборкой run мне помогала нейронка,
//в голову вообще шел примерный план действий но какие именно инструменты использовать
//подсказывал чат гпт

