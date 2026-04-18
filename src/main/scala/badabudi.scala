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