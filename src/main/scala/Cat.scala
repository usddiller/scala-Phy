import cats.effect.{IO, IOApp}

object SimpleParallelBar extends IOApp.Simple {

  def serveGuest(name: String): IO[Unit] = for {
    _     <- IO.println(name + ": Возраст?")
    input <- IO.readLine
    age   =  input.toInt
    drink =  if (age >= 18) "Beer" else "Juice"
    _     <- IO.println(name + ": На твой " + drink)
  } yield ()


  def answerforgemini: IO[Unit] = for {
    _ <- IO.println("и че это все?")
    input <- IO.readLine
    somequestion = if (input == "нет") "так а херли ты остановился" else "ок"
    _ <- IO.println(somequestion)
  }yield()

  def run: IO[Unit] = for {
    // ЗАПУСКАЕМ ВСЕХ ЧЕТВЕРЫХ СРАЗУ.
    // Теперь они все вчетвером вывалят "Возраст?" в консоль ОДНОВРЕМЕННО.
    f1 <- serveGuest("Первый").start
    f2 <- serveGuest("Второй").start
    f3 <- serveGuest("Третий").start
    f4 <- serveGuest("Четвертый").start

    // А теперь ЖЕСТКО говорим программе, в каком порядке принимать ответы:
    _ <- f1.join
    _ <- f2.join // Пока Второму не ответишь — код дальше не пойдет
    _ <- f3.join // Потом Третьему
    _ <- f4.join // Потом Четвертому

  } yield ()
}