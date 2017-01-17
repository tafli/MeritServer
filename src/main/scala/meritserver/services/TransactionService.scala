package meritserver.services

import akka.agent.Agent
import meritserver.models.{CreateTransaction, Transaction}
import meritserver.utils.Configuration

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

object TransactionService extends TransactionService with Configuration {
  val transactionsFile: String = meritTransactionsFile

  val transactionAgent: Agent[List[Transaction]] = Agent(List[Transaction]())

  def load(): Option[String] = FileAccessService.readFromFile(transactionsFile)

  def save(data: String): Unit = FileAccessService.writeToFile(transactionsFile, data)
}

trait TransactionService {
  def getTransactions: List[Transaction] = TransactionService.transactionAgent.get

  def getTransactionById(id: String): Option[Transaction] = TransactionService.transactionAgent.get.find(_.id == id)

  def createTransaction(pTransaction: CreateTransaction): Try[Transaction] = {
    isTransactionValid(pTransaction) match {
      case Success(_) =>
        val newTransaction = Transaction(from = pTransaction.from, to = pTransaction.to, amount = pTransaction.amount, reason = pTransaction.reason)
        TransactionService.transactionAgent.alter(_ :+ newTransaction).onSuccess {
          case transactions => DataAccessService.saveTransactions(transactions)
        }
        Success(newTransaction)
      case Failure(e) => Failure(e)
    }
  }

  def deleteTransaction(id: String): Unit = {
    TransactionService.transactionAgent.alter(_.filterNot(_.id == id)).onSuccess {
      case transactions => DataAccessService.saveTransactions(transactions)
    }
  }

  private def isTransactionValid(transaction: CreateTransaction): Try[String] = {
    if (transaction.from == transaction.to) return Failure(new IllegalArgumentException("Sender and Receiver cannot be the same!"))
    if (transaction.amount > TransactionService.meritStartAmount) return Failure(new IllegalArgumentException("Amount cannot be higher than initial amount!"))
    if (transaction.amount <= 0) return Failure(new IllegalArgumentException("Really? Nothing?"))

    val from = UserService.getUserById(transaction.from)
    val to = UserService.getUserById(transaction.to)

    if (from.isEmpty) return Failure(new IllegalArgumentException("Sender not found!"))
    if (to.isEmpty) return Failure(new IllegalArgumentException("Receiver not found!"))

    val sentAmount = TransactionService.transactionAgent.get.filter(_.from == transaction.from).filterNot(_.booked == true).foldLeft(0) { (acc, t) => acc + t.amount }

    if (sentAmount + transaction.amount > TransactionService.meritStartAmount)
      return Failure(new IllegalArgumentException(s"Available amount [${TransactionService.meritStartAmount - sentAmount}] too low!"))

    Success("Everything is fine!")
  }
}