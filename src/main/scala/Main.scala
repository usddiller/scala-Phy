//import cats.effect.{IO, IOApp}
//
//object Main extends IOApp.Simple {
//  case class Wallet(balance: Int)
//
//  // 2. СТАКАН (Тара)
//  // Теперь он может содержать в себе весь объект рецепта (Option[Recipe])
//  case class Cup(
//                  maxvolume: Int,
//                  liquidvolume: Int,
//                  content: Option[Recipe] = None
//                )
//
//  // 3. РЕЦЕПТ (Техкарта напитка)
//  // Включает в себя тип стакана, в который он должен быть налит
//  case class Recipe(
//                     name: String,
//                     water: Int,
//                     milk: Int,
//                     coffee: Int,
//                     sugar: Int,
//                     price: Int,
//                     cupType: Cup
//                   )
//

//  case class Cofemachine(
//                          smallCups: Int, // Запас стаканов S (150ml)
//                          mediumCups: Int, // Запас стаканов M (300ml)
//                          largeCups: Int, // Запас стаканов L (450ml)
//                          sugar: Int, // Запас сахара (г)
//                          water: Int, // Запас воды (мл)
//                          milk: Int, // Запас молока (мл)
//                          coffee: Int, // Запас кофе (г)
//                          balance: Int, // Общая касса (деньги внутри сейфа машины)
//                          inserted: Int, // Внесенные юзером деньги (висят на табло)
//                          power: Boolean // Состояние питания
//                        )

//  val smallCup = Cup(maxvolume = 150, liquidvolume = 0)
//  val mediumCup = Cup(maxvolume = 300, liquidvolume = 0)
//  val bigCup = Cup(maxvolume = 450, liquidvolume = 0)

//  val smallEspresso = Recipe("Эспрессо S", 30, 0, 7, 0, 100, smallCup)
//  val mediumEspresso = Recipe("Эспрессо M", 60, 0, 14, 0, 180, mediumCup)
//

//  val mediumCappuccino = Recipe("Капучино M", 30, 150, 7, 5, 250, mediumCup)
//  val bigCappuccino = Recipe("Капучино L", 50, 250, 10, 7, 350, bigCup)
//

//  val mediumLatte = Recipe("Латте M", 30, 200, 7, 5, 280, mediumCup)
//  val bigLatte = Recipe("Латте L", 50, 300, 10, 7, 380, bigCup)
//

//  val bigRaf = Recipe("Раф L", 30, 350, 10, 15, 450, bigCup)
//
//  def prepareCoffee(m: Cofemachine, r: Recipe): (Cofemachine, Cup) = {

//    val hasCup = r.cupType.maxvolume match {
//      case 150 => m.smallCups > 0
//      case 300 => m.mediumCups > 0
//      case 450 => m.largeCups > 0
//      case _ => false
//    }
//
//    val hasResources =
//      m.water >= r.water &&
//        m.milk >= r.milk &&
//        m.coffee >= r.coffee &&
//        m.sugar >= r.sugar &&
//        m.inserted >= r.price
//
//    if (m.power && hasCup && hasResources) {
//      val mAfterCup = r.cupType.maxvolume match {
//        case 150 => m.copy(smallCups = m.smallCups - 1)
//        case 300 => m.copy(mediumCups = m.mediumCups - 1)
//        case 450 => m.copy(largeCups = m.largeCups - 1)
//        case _ => m
//      }
//

//      val finalMachine = mAfterCup.copy(
//        water = mAfterCup.water - r.water,
//        milk = mAfterCup.milk - r.milk,
//        coffee = mAfterCup.coffee - r.coffee,
//        sugar = mAfterCup.sugar - r.sugar,
//        inserted = mAfterCup.inserted - r.price, // Ушло с табло
//        balance = mAfterCup.balance + r.price // Ушло в кассу
//      )
//

//      val finishedCup = r.cupType.copy(
//        liquidvolume = r.water + r.milk,
//        content = Some(r)
//      )
//
//      (finalMachine, finishedCup)
//    } else {
//      (m, Cup(r.cupType.maxvolume, 0, None))
//    }
//  }
//
//  def askAmount: IO[Int] =
//    for {
//      _ <- IO.println("Сколько денег закидываем?")
//      input <- IO.readLine
//      amount = input.toIntOption.getOrElse(0)
//    } yield amount
//    
//  def insertMoney(m: Cofemachine, w: Wallet, amount: Int): (Cofemachine, Wallet) = {
//    println(s"\n[ЛОГ] Чувак вносит бабки: $amount")
//    if (w.balance >= amount) {
//      val updatedMachine = m.copy(inserted = m.inserted + amount)
//      val updatedWallet = w.copy(balance = w.balance - amount)
//
//      println(s"✅ Успех! Машина теперь видит: ${updatedMachine.inserted}. Кошелек похудел до: ${updatedWallet.balance}")
//      (updatedMachine, updatedWallet)
//    } else {
//      println(s"❌ Ошибка: В кошельке всего ${w.balance}, не хватает на взнос!")
//      (m, w)
//    }
//  }
//

//  def buyCoffee(m: Cofemachine, r: Recipe): (Cofemachine, Cup) = {
//    println(s"\n[ЛОГ] Выбрано кофе: ${r.name} за ${r.price}")
//
//    if (m.inserted >= r.price) {

//      val updatedCupsMachine = r.cupType.maxvolume match {
//        case 150 => m.copy(smallCups = m.smallCups - 1)
//        case 300 => m.copy(mediumCups = m.mediumCups - 1)
//        case 450 => m.copy(largeCups = m.largeCups - 1)
//        case _ => m
//      }
//
//      val finalMachine = updatedCupsMachine.copy(
//        water = m.water - r.water,
//        milk = m.milk - r.milk,
//        coffee = m.coffee - r.coffee,
//        sugar = m.sugar - r.sugar,
//        inserted = m.inserted - r.price,
//        balance = m.balance + r.price // ДЕНЬГИ ПЕРЕШЛИ НА ОСНОВНОЙ БАЛИК
//      )
//
//      println(s"💰 Бабки перешли на балик кофемашины! Касса (balance): ${finalMachine.balance}")
//      println(s"📉 Ресурсы списаны. На табло осталось: ${finalMachine.inserted}")
//
//      val resultCup = r.cupType.copy(
//        liquidvolume = r.water + r.milk,
//        content = Some(r)
//      )
//
//      (finalMachine, resultCup)
//    } else {
//      println(s"❌ Мало бабок на табло! Нужно ${r.price}, а есть только ${m.inserted}")
//      (m, Cup(r.cupType.maxvolume, 0, None))
//    }
//  }
//

//  def giveChange(m: Cofemachine, w: Wallet): (Cofemachine, Wallet) = {
//    println(s"\n[ЛОГ] Возврат сдачи: ${m.inserted}")
//    val newWallet = w.copy(balance = w.balance + m.inserted)
//    val newMachine = m.copy(inserted = 0)
//    println(s"👛 Кошелек теперь: ${newWallet.balance}. На табло машины: 0")
//    (newMachine, newWallet)
//  }
//
//
//  override def run: IO[Unit] = ???
//}
//
