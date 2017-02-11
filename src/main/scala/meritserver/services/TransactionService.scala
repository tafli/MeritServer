package meritserver.services

import java.time.LocalDateTime

import akka.agent.Agent
import meritserver.models.{CreateTransaction, Transaction}
import meritserver.services.TransactionService.Filter
import meritserver.utils.Configuration

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

object TransactionService extends TransactionService with Configuration {
  val transactionsFile: String = meritTransactionsFile

  val transactionAgent: Agent[List[Transaction]] = Agent(List[Transaction]())

  def load(): Unit = transactionAgent.send(DataAccessService.loadTransactions())

  case class Filter(booked: Option[String],
                    from: Option[String],
                    to: Option[String],
                    fromTS: Option[String],
                    toTS: Option[String])
  object NoFilter extends Filter(None, None, None, None, None)
}

trait TransactionService {
  def getTransactions(filter: Filter): List[Transaction] = {
    TransactionService.transactionAgent.get
      .filter(transaction =>
        filter.booked match {
          case Some(booked) => transaction.booked == booked.toBoolean
          case _            => true
      })
      .filter(transaction =>
        filter.from match {
          case Some(from) => transaction.from == from
          case _          => true
      })
      .filter(transaction =>
        filter.to match {
          case Some(to) => transaction.to == to
          case _        => true
      })
      .filter(transaction =>
        filter.fromTS match {
          case Some(fromTS) =>
            transaction.date.compareTo(LocalDateTime.parse(fromTS)) >= 0
          case _ => true
      })
      .filter(transaction =>
        filter.toTS match {
          case Some(toTS) =>
            transaction.date.compareTo(LocalDateTime.parse(toTS)) <= 0
          case _ => true
      })
  }

  def getTransactionById(id: String): Option[Transaction] =
    TransactionService.transactionAgent.get.find(_.id == id)

  def createTransaction(pTransaction: CreateTransaction): Try[Transaction] = {
    isTransactionValid(pTransaction) match {
      case Success(_) =>
        val newTransaction = Transaction(from = pTransaction.from,
                                         to = pTransaction.to,
                                         amount = pTransaction.amount,
                                         reason = pTransaction.reason)
        TransactionService.transactionAgent
          .alter(_ :+ newTransaction)
          .foreach { transactions =>
            DataAccessService.saveTransactions(transactions)
          }
        Success(newTransaction)
      case Failure(e) => Failure(e)
    }
  }

  def deleteTransaction(id: String): Unit = {
    TransactionService.transactionAgent
      .alter(_.filterNot(_.id == id))
      .foreach { transactions =>
        DataAccessService.saveTransactions(transactions)
      }
  }

  private def isTransactionValid(transaction: CreateTransaction): Try[String] = {
    if (transaction.from == transaction.to)
      return Failure(
        new IllegalArgumentException(
          "Sender and Receiver cannot be the same!"))
    if (transaction.amount > TransactionService.meritStartAmount)
      return Failure(
        new IllegalArgumentException(
          "Amount cannot be higher than initial amount!"))
    if (transaction.amount <= 0)
      return Failure(new IllegalArgumentException("Really? Nothing?"))

    val from = UserService.getUserById(transaction.from)
    val to   = UserService.getUserById(transaction.to)

    if (from.isEmpty)
      return Failure(new IllegalArgumentException("Sender not found!"))
    if (to.isEmpty)
      return Failure(new IllegalArgumentException("Receiver not found!"))

    val sentAmount = TransactionService.transactionAgent.get
      .filter(_.from == transaction.from)
      .filterNot(_.booked == true)
      .foldLeft(0) { (acc, t) =>
        acc + t.amount
      }

    if (sentAmount + transaction.amount > TransactionService.meritStartAmount)
      return Failure(new IllegalArgumentException(
        s"Available amount [${TransactionService.meritStartAmount - sentAmount}] too low!"))

    Success("Everything is fine!")
  }
}
