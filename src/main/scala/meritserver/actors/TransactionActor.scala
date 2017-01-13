package meritserver.actors

import akka.actor.{Actor, ActorRef, Props}
import meritserver.models.Transaction
import meritserver.services.DataAccessService

object TransactionActor {
  val actor: ActorRef = RootActor.system.actorOf(Props[TransactionActor], "TransactionActor")

  object GetTransactions

  case class GetTransaction(id: String)

  case class GetTransactionsBySender(userId: String)

  case class CreateTransaction(transaction: Transaction)

  case class DeleteTransaction(id: String)

  object LoadData
}

class TransactionActor extends Actor {

  import meritserver.actors.TransactionActor._

  var transactions: List[Transaction] = List[Transaction]()

  override def receive: Receive = {
    case GetTransactions => sender ! transactions
    case GetTransaction(id: String) => sender ! transactions.find(_.id == id)
    case GetTransactionsBySender(userId: String) => sender ! transactions.filter(_.from == userId)
    case CreateTransaction(transaction) =>
      transactions = transactions :+ transaction
      sender ! transaction
      saveTransactions()
    case DeleteTransaction(id) => transactions = transactions.filterNot(_.id == id)
    case LoadData => transactions = DataAccessService.loadTransactions()
  }

  private def saveTransactions() = DataAccessService.saveTransactions(transactions)
}