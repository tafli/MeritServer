package meritserver.services

import akka.util.Timeout
import meritserver.actors.{TransactionActor, UserActor}
import meritserver.models.{CreateTransaction, Transaction, User}
import meritserver.utils.{Configuration, TryAwait}

import scala.util.{Failure, Success}

object TransactionsService extends TransactionsService with Configuration {
  private val transactionsFile = meritTransactionsFile

  def load(): Option[String] = FileAccessService.readFromFile(transactionsFile)
  def save(data: String): Unit = FileAccessService.writeToFile(transactionsFile, data)
}

trait TransactionsService extends TryAwait {
  import akka.pattern.ask

  import scala.concurrent.duration._
  import scala.util.Try

  implicit val timeout = Timeout(1 seconds)

  def getTransactions: Try[List[Transaction]] = tryAwait((TransactionActor.actor ? TransactionActor.GetTransactions).mapTo[List[Transaction]])

  def getTransactionById(id: String): Try[Option[Transaction]] = tryAwait((TransactionActor.actor ? TransactionActor.GetTransaction(id)).mapTo[Option[Transaction]])

  def createTransaction(transaction: CreateTransaction): Try[Transaction] = {
    isTransactionValid(transaction) match {
      case Success(_) =>
        tryAwait((TransactionActor.actor ? TransactionActor.CreateTransaction(Transaction(from = transaction.from, to = transaction.to, amount = transaction.amount, reason = transaction.reason))).mapTo[Transaction])
      case Failure(e) => Failure(e)
    }
  }

  def deleteTransaction(id: String): Unit = TransactionActor.actor ! TransactionActor.DeleteTransaction(id)

  private def isTransactionValid(transaction: CreateTransaction): Try[String] = {
    if(transaction.from == transaction.to) return Failure(new IllegalArgumentException("From and To cannot be the same!"))
    if(transaction.amount > TransactionsService.meritStartAmount) return Failure(new IllegalArgumentException("Amount cannot be higher than initial amount!"))
    if(transaction.amount <= 0) return Failure(new IllegalArgumentException("Amount must be positive!"))

    val from = tryAwait((UserActor.actor ? UserActor.GetUser(transaction.from)).mapTo[Option[User]])
    val to = tryAwait((UserActor.actor ? UserActor.GetUser(transaction.to)).mapTo[Option[User]])

    if(from.isFailure || to.isFailure) return Failure(new IllegalArgumentException("Error retrieving users!"))
    if(from.get.isEmpty || to.get.isEmpty) return Failure(new IllegalArgumentException("From or To not found!"))

    val sentAmount = tryAwait((TransactionActor.actor ? TransactionActor.GetTransactionsBySender(from.get.get.id)).mapTo[List[Transaction]])
      .getOrElse(List()).foldLeft(0){(acc, t) => acc + t.amount}

    if(sentAmount + transaction.amount > TransactionsService.meritStartAmount)
      return Failure(new IllegalArgumentException(s"Balance of [${TransactionsService.meritStartAmount-sentAmount}] too low!"))

    Success("Everything is fine!")
  }
}