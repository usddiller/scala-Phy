import cats.effect.{IO, IOApp, Ref}
import cats.syntax.all.*
import scala.concurrent.duration.DurationInt
object PhySam extends IOApp.Simple {
  trait PhyState
  object Eat extends PhyState
  object Think extends PhyState
  case class Phy(id:Int,state:PhyState)
  
  val p1 = Phy(1,Think)
  val p2 = Phy(2,Think)
  val p3 = Phy(3,Think)
  val p4 = Phy(4,Think)
  val p5 = Phy(5,Think)
  
  val tablePhy = Ref.of[IO, List[Phy]](List(p1, p2, p3, p4, p5))
  def canEat(phy: Phy,table:List[Phy]):Boolean = {
    val fd = phy.id
    val leftId = if (fd == 1) 5 else fd - 1
    val rightId = if (fd == 5) 1 else fd + 1
    val leftN = table.find(_.id == leftId).get
    val rightN = table.find(_.id == rightId).get
    rightN.state != Eat && leftN.state != Eat
  }
  
  def tryToEat(phy:Phy,table: Ref[IO,List[Phy]]) = {
    val fd = phy.id
    table.modify (list =>
      val isAllowed = canEat(phy, list)
      val newList = if (isAllowed)
        list.map(p => if p.id == fd then p.copy(state = Eat) else p)
      else list
      (newList ,isAllowed)
    )
  }

  def stopEat(phy:Phy, table:Ref[IO,List[Phy]]) = {
    table.modify (list=>
      val newList = list.map(p =>if p.id == phy.id then p.copy(state = Think)else p)
      (newList,())
    )
  }

  def step(phy:Phy, table: Ref[IO,List[Phy]]):IO[Unit] ={
    for {
      _ <- IO.println(s"${phy.id} думает")
      _ <- IO.sleep(1.seconds)
      canIEat <- tryToEat(phy, table)
      _ <-
        if canIEat then for{
          _ <- IO.println(s"${phy.id} est")
          _ <- IO.sleep(2.seconds)
          _ <- stopEat(phy, table)
        }yield()
        else {
          IO.println(s"${phy.id} ждет вилки")
        }
      _ <- step(phy, table)
    }yield()
  }


  override def run: IO[Unit] =
    for {
      table <- tablePhy
      list <- table.get
      _ <- list.traverse(a => step(a,table).start)
      _ <-IO.never
    }yield()

}

