import cats.effect.{ExitCode, IO, IOApp}

import scala.concurrent.{ExecutionContext, Future}
import cats.implicits.*

def traverseFuture[A, B](list: List[A])(f: A => Future[B])(implicit ec: ExecutionContext): Future[List[B]] = {
  list match {
    case Nil => Future.successful(Nil)
    case head :: tail => val futureHead: Future[B] = f(head)
    val futureTail: Future[List[B]] = traverseFuture(tail)(f)
      futureHead.flatMap { b =>
        futureTail.map { listB =>
          b :: listB
        }
      }
  }
}
def traverse[A, B](list: List[A])(f: A => Future[B])(implicit ec: ExecutionContext): Future[List[B]] = {
  list match {
    case Nil => Future.successful(Nil)
    case head :: tail => for {
      h <- f(head)
      t <- traverse(tail)(f)
    } yield (h :: t)
  }
}
def travIO[A,B](ls:List[A])(f:A=>IO[B]):IO[List[B]] = {
  ls match {
    case Nil => IO.pure(Nil)
    case a :: b => for {
      h <- f(a)
      t <- travIO(b)(f)
    }yield h::t
  }
}

def seqIO[A](ls:List[IO[A]]):IO[List[A]] ={
  ls match {
    case Nil => IO.pure(Nil)
    case a::b => for{
      h <- a
      t <- seqIO(b)
    }yield h::t
  }
}
//object someobj extends IOApp{
//  val ls: List[IO[Int]] = List(IO(1), IO(2), IO(3))
//  val lst = ls.traverse(i => for{
//    a <- i
//  }yield a * a+1
//  )
//
//
//  def run: IO[Unit] ={
//    for
//      a <- lst
//      _ <- IO.println(lst)
//    yield()
//  }
//
//}


//val f1 = Fork(1)
//val f2 = Fork(2)
//val f3 = Fork(3)
//val f4 = Fork(4)
//val f5 = Fork(5)

//def loop(p1: Phy, p2: Phy, p3: Phy, p4: Phy, p5: Phy): IO[Unit] = {
//
//  def check(ph: Phy) = ph.state match {
//    case Eat => false
//    case Think => true
//  }
//
//  def checkNs(ph: Phy) = ph.fd match {
//    case 1 => (check(p2), check(p5))
//    case 2 => (check(p1), check(p3))
//    case 3 => (check(p2), check(p4))
//    case 4 => (check(p3), check(p5))
//    case 5 => (check(p1), check(p4))
//  }
//
//  def server(ph: Phy) = checkNs(ph) match {
//    case (true, true) => ph.copy(state = Eat)
//    case _            => ph.copy(state = Think)
//  }
//
//  for {
//    _ <- IO.println(s"Статус: 1:${p1.state}, 2:${p2.state}, 3:${p3.state}...")
//    _ <- IO.sleep(1.second)
//

//    nextP1 = server(p1)
//    nextP2 = server(p2)
//    nextP3 = server(p3)
//    nextP4 = server(p4)
//    nextP5 = server(p5)
//
//
//    _ <- loop(nextP1, nextP2, nextP3, nextP4, nextP5)
//  } yield ()
//}




